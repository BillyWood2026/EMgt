param(
    [string[]]$Platforms = @('win','linux','darwin-x64','darwin-arm64')
)

$ErrorActionPreference = 'Stop'

$runtimeDir = [System.IO.Path]::GetFullPath((Join-Path (Join-Path $PSScriptRoot '..') 'runtime'))

function Get-SizeBytes($path) {
    $bytes = (Get-ChildItem -Path $path -Recurse -File -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum
    if ($null -eq $bytes) { return 0 }
    return $bytes
}

function Format-Size($bytes) {
    return "{0} MB" -f [math]::Round($bytes / 1MB, 2)
}

foreach ($plat in $Platforms) {
    $mysqlDir = Join-Path $runtimeDir "$plat\mysql"
    if (-not (Test-Path $mysqlDir)) {
        Write-Host "MySQL directory not found for platform $plat, skipping: $mysqlDir"
        continue
    }

    $before = Get-SizeBytes $mysqlDir
    Write-Host "`n=== Strip MySQL: $plat (before: $(Format-Size $before)) ==="

    # Build keep-set of full paths (case-insensitive)
    $keep = New-Object 'System.Collections.Generic.HashSet[string]' ([System.StringComparer]::OrdinalIgnoreCase)

    # bin: only specific executables, plus all *.dll on Windows
    $binDir = Join-Path $mysqlDir 'bin'
    if (Test-Path $binDir) {
        $binNames = @(
            'mysqld.exe','mysql.exe','mysqladmin.exe','mysqldump.exe',
            'mysqlcheck.exe','mysql_config_editor.exe','my_print_defaults.exe',
            'mysql_ssl_rsa_setup.exe','mysql_tzinfo_to_sql.exe','libmysql.dll',
            'mysqld','mysql','mysqladmin','mysqldump',
            'mysqlcheck','mysql_config_editor','my_print_defaults',
            'mysql_ssl_rsa_setup','mysql_tzinfo_to_sql'
        )
        foreach ($name in $binNames) {
            $p = Join-Path $binDir $name
            if (Test-Path $p) { [void]$keep.Add($p) }
        }
        if ($plat -eq 'win') {
            @(Get-ChildItem -Path $binDir -Filter '*.dll' -File -ErrorAction SilentlyContinue) | ForEach-Object {
                [void]$keep.Add($_.FullName)
            }
        }
    }

    # lib: *.dll, *.so*, *.dylib and everything under plugin\
    $libDir = Join-Path $mysqlDir 'lib'
    if (Test-Path $libDir) {
        @(Get-ChildItem -Path $libDir -Recurse -File -ErrorAction SilentlyContinue) | ForEach-Object {
            $f = $_.FullName
            $n = $_.Name
            $ext = $_.Extension.ToLowerInvariant()
            $isShared = ($ext -eq '.dll') -or ($ext -eq '.dylib') -or ($n -match '\.so(\.|$)')
            $inPlugin = ($f -replace '/', '\') -match '\\plugin\\'
            if ($isShared -or $inPlugin) { [void]$keep.Add($f) }
        }
    }

    # share: english\errmsg.sys and charsets\*
    $shareDir = Join-Path $mysqlDir 'share'
    if (Test-Path $shareDir) {
        $errmsg = Join-Path (Join-Path $shareDir 'english') 'errmsg.sys'
        if (Test-Path $errmsg) { [void]$keep.Add($errmsg) }
        $charsetsDir = Join-Path $shareDir 'charsets'
        if (Test-Path $charsetsDir) {
            @(Get-ChildItem -Path $charsetsDir -Recurse -File -ErrorAction SilentlyContinue) | ForEach-Object {
                [void]$keep.Add($_.FullName)
            }
        }
    }

    # Delete everything not in keep-set
    $allFiles = @(Get-ChildItem -Path $mysqlDir -Recurse -File -ErrorAction SilentlyContinue)
    $removed = 0
    foreach ($file in $allFiles) {
        if (-not $keep.Contains($file.FullName)) {
            Remove-Item -Path $file.FullName -Force -ErrorAction SilentlyContinue
            $removed++
        }
    }

    # Remove now-empty directories (repeat until none left)
    do {
        $emptyDirs = @(Get-ChildItem -Path $mysqlDir -Recurse -Directory -ErrorAction SilentlyContinue | Where-Object {
            @(Get-ChildItem -Path $_.FullName -Force -ErrorAction SilentlyContinue).Count -eq 0
        })
        foreach ($d in $emptyDirs) {
            Remove-Item -Path $d.FullName -Force -ErrorAction SilentlyContinue
        }
    } while ($emptyDirs.Count -gt 0)

    $after = Get-SizeBytes $mysqlDir
    Write-Host "Removed $removed files. After: $(Format-Size $after) (saved $(Format-Size ($before - $after)))"

    # Verify mysqld still runs on Windows
    if ($plat -eq 'win') {
        $mysqld = Join-Path $mysqlDir 'bin\mysqld.exe'
        if (Test-Path $mysqld) {
            $prevEap = $ErrorActionPreference
            $ErrorActionPreference = 'Continue'
            $ver = & cmd /c "`"$mysqld`" --version 2>nul"
            $ErrorActionPreference = $prevEap
            if ([string]::IsNullOrWhiteSpace($ver)) {
                Write-Host "WARNING: mysqld.exe --version produced no output after strip."
            } else {
                Write-Host "Verification: $ver"
            }
        } else {
            Write-Host "WARNING: mysqld.exe not found after strip."
        }
    }
}

Write-Host "`nStrip complete."
