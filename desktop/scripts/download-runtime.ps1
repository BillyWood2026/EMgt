# ============================================================
# download-runtime.ps1 - Download portable JRE17 + MySQL 8.4 runtimes
# Mirrors first (TUNA), official as fallback. PS 5.1, ASCII only.
# Usage: powershell -NoProfile -ExecutionPolicy Bypass -File download-runtime.ps1 [-Platforms win,linux,...] [-Force]
# ============================================================
param(
    [string[]]$Platforms = @('win','linux','darwin-x64','darwin-arm64'),
    [switch]$Force
)

$ErrorActionPreference = 'Stop'

$JreVersion   = 'jdk-17.0.20.1+1'
$JreFileVer   = '17.0.20.1_1'
$MysqlVersion = '8.4.6'

$runtimeDir = Join-Path (Join-Path $PSScriptRoot '..') 'runtime'
New-Item -ItemType Directory -Path $runtimeDir -Force | Out-Null

function Get-SizeMB($path) {
    $bytes = (Get-ChildItem -Path $path -Recurse -File -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum
    if ($null -eq $bytes) { return 0 }
    return [math]::Round($bytes / 1MB, 2)
}

function Download-File($url, $outFile) {
    Write-Host "Downloading: $url"
    $prevEap = $ErrorActionPreference
    try {
        $curl = Get-Command curl.exe -ErrorAction SilentlyContinue
        if ($curl) {
            $ErrorActionPreference = 'Continue'
            & curl.exe -L --fail --connect-timeout 30 --retry 2 -o $outFile $url 2>&1 | Out-Null
            $rc = $LASTEXITCODE
            $ErrorActionPreference = $prevEap
            if ($rc -ne 0) { throw "curl exit=$rc" }
        } else {
            Invoke-WebRequest -Uri $url -OutFile $outFile -UseBasicParsing -TimeoutSec 3600
        }
    } finally {
        $ErrorActionPreference = $prevEap
    }
    if (-not (Test-Path $outFile) -or (Get-Item $outFile).Length -lt 1MB) { throw "download too small: $url" }
}

function Extract-StripTop($archive, $destDir) {
    $tempExtract = Join-Path $env:TEMP ([Guid]::NewGuid().ToString())
    New-Item -ItemType Directory -Path $tempExtract -Force | Out-Null
    $prevEap = $ErrorActionPreference
    try {
        if ($archive -match '\.zip$') {
            Expand-Archive -Path $archive -DestinationPath $tempExtract -Force
        } else {
            $ErrorActionPreference = 'Continue'
            & cmd /c "tar -xf `"$archive`" -C `"$tempExtract`" 2>nul" | Out-Null
            if ($LASTEXITCODE -ne 0) { throw "tar extraction failed for $archive" }
            $ErrorActionPreference = $prevEap
        }
        $top = @(Get-ChildItem -Path $tempExtract -Directory)
        $allTop = @(Get-ChildItem -Path $tempExtract)
        if ($top.Count -eq 1 -and $allTop.Count -eq 1) {
            $source = $top[0].FullName
            Get-ChildItem -Path $source -Force | Move-Item -Destination $destDir -Force
        } else {
            Get-ChildItem -Path $tempExtract -Force | Move-Item -Destination $destDir -Force
        }
    } finally {
        $ErrorActionPreference = $prevEap
        Remove-Item -Path $tempExtract -Recurse -Force -ErrorAction SilentlyContinue
    }
}

function Get-JreUrls($plat) {
    switch ($plat) {
        'win'          { return @(
            "https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jre/x64/windows/OpenJDK17U-jre_x64_windows_hotspot_$JreFileVer.zip",
            "https://api.adoptium.net/v3/binary/version/$([Uri]::EscapeDataString($JreVersion))/windows/x64/jre/hotspot/normal/eclipse") }
        'linux'        { return @(
            "https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jre/x64/linux/OpenJDK17U-jre_x64_linux_hotspot_$JreFileVer.tar.gz",
            "https://api.adoptium.net/v3/binary/version/$([Uri]::EscapeDataString($JreVersion))/linux/x64/jre/hotspot/normal/eclipse") }
        'darwin-x64'   { return @(
            "https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jre/x64/mac/OpenJDK17U-jre_x64_mac_hotspot_$JreFileVer.tar.gz",
            "https://api.adoptium.net/v3/binary/version/$([Uri]::EscapeDataString($JreVersion))/mac/x64/jre/hotspot/normal/eclipse") }
        'darwin-arm64' { return @(
            "https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jre/aarch64/mac/OpenJDK17U-jre_aarch64_mac_hotspot_$JreFileVer.tar.gz",
            "https://api.adoptium.net/v3/binary/version/$([Uri]::EscapeDataString($JreVersion))/mac/aarch64/jre/hotspot/normal/eclipse") }
        default        { throw "Unknown platform: $plat" }
    }
}

function Get-MysqlUrls($plat) {
    switch ($plat) {
        'win'          { return @(
            "https://cdn.mysql.com/archives/mysql-8.4/mysql-$MysqlVersion-winx64.zip",
            "https://dev.mysql.com/get/Downloads/MySQL-8.4/mysql-$MysqlVersion-winx64.zip") }
        'linux'        { return @(
            "https://cdn.mysql.com/archives/mysql-8.4/mysql-$MysqlVersion-linux-glibc2.17-x86_64-minimal.tar.xz",
            "https://cdn.mysql.com/archives/mysql-8.4/mysql-$MysqlVersion-linux-glibc2.17-x86_64.tar.xz") }
        'darwin-x64'   { return @(
            "https://cdn.mysql.com/archives/mysql-8.4/mysql-$MysqlVersion-macos15-x86_64.tar.gz") }
        'darwin-arm64' { return @(
            "https://cdn.mysql.com/archives/mysql-8.4/mysql-$MysqlVersion-macos15-arm64.tar.gz") }
        default        { throw "Unknown platform: $plat" }
    }
}

function Fetch-And-Extract($urls, $destDir, $label) {
    $downloaded = $false
    $outFile = $null
    foreach ($url in $urls) {
        $fileName = [System.IO.Path]::GetFileName($url.Split('?')[0])
        if ($fileName -notmatch '\.(zip|tar\.gz|tar\.xz)$') { $fileName = "$label.pkg" }
        $outFile = Join-Path $env:TEMP $fileName
        try {
            Download-File $url $outFile
            $downloaded = $true
            break
        } catch {
            Write-Host "  failed, trying next mirror..."
            Remove-Item $outFile -Force -ErrorAction SilentlyContinue
        }
    }
    if (-not $downloaded) { throw "Download failed for $label" }
    if (Test-Path $destDir) { Remove-Item $destDir -Recurse -Force }
    New-Item -ItemType Directory -Path $destDir -Force | Out-Null
    Extract-StripTop $outFile $destDir
    Remove-Item $outFile -Force -ErrorAction SilentlyContinue
    Write-Host "$label size: $(Get-SizeMB $destDir) MB"
}

foreach ($plat in $Platforms) {
    Write-Host "`n=== Platform: $plat ==="

    $jreDir = Join-Path $runtimeDir "$plat\jre"
    if ((-not $Force) -and (Test-Path (Join-Path $jreDir 'bin'))) {
        Write-Host "JRE ($plat) already exists, skipping. Use -Force to re-download."
    } else {
        Fetch-And-Extract (Get-JreUrls $plat) $jreDir "JRE ($plat)"
    }

    $mysqlDir = Join-Path $runtimeDir "$plat\mysql"
    if ((-not $Force) -and (Test-Path (Join-Path $mysqlDir 'bin'))) {
        Write-Host "MySQL ($plat) already exists, skipping. Use -Force to re-download."
    } else {
        Fetch-And-Extract (Get-MysqlUrls $plat) $mysqlDir "MySQL ($plat)"
    }
}

Write-Host "`nAll done. Runtime directory: $runtimeDir"
