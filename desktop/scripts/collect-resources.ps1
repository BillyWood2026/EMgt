# ============================================================
# collect-resources.ps1 - Collect all bundle assets into desktop/vendor
# PS 5.1 compatible, ASCII only, save as UTF-8 with BOM
# Steps: frontend build -> jars -> services.json -> proxy-rules.json -> mysqldump -> manifest
# ============================================================
param(
  [switch]$SkipFrontend,
  [switch]$SkipDump
)

$ErrorActionPreference = 'Stop'
$Root    = 'd:\EMgtv3'
$Desk    = Join-Path $Root 'desktop'
$Vendor  = Join-Path $Desk 'vendor'
$JarDir  = Join-Path $Vendor 'jars'
$FeDir   = Join-Path $Vendor 'frontend'
$DumpDir = Join-Path $Vendor 'db-dump'
$CfgDir  = Join-Path $Desk 'src\config'

$MysqlExe  = 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe'
$DumpExe   = 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysqldump.exe'
$MysqlPwd  = 'Sun-0126King-0325'

# ---- 44 services: name, wave, xmx (authoritative wave table) ----
$WaveDefs = @(
  @('IAM',1,96), @('module-config-center',1,96), @('SysMgt',1,96), @('SysMonitor',1,96),
  @('OrgMgt',2,128), @('EmployeeMgt',2,128), @('AttendanceMgt',2,128), @('RecruitmentMgt',2,128), @('PerfMgt',2,128),
  @('TrainingMgt',3,128), @('QualificationMgt',3,128), @('SalaryMgt',3,128), @('SkillCertMgt',3,128), @('TalentMgt',3,128),
  @('ProjectMgt',4,128), @('HRPlanningMgt',4,128), @('HolidayMgt',4,128), @('TalentReviewMgt',4,128), @('SuccessionPlanMgt',4,128),
  @('TalentDevMgt',5,128), @('OnlineLearnMgt',5,128), @('OnlineExamMgt',5,128), @('AuditMgt',5,128), @('DisciplineMgt',5,128),
  @('PartyAffairsMgt',6,128), @('CultureMgt',6,128), @('ProcessMgt',6,128), @('ImproveMgt',6,128), @('FlowEngineMgt',6,128),
  @('StrategyBlmMgt',7,96), @('StrategyDsteMgt',7,96), @('StrategySixsMgt',7,96), @('StrategyBudgetMgt',7,96), @('StrategyWorkplanMgt',7,96),
  @('EnvironMgt',8,96), @('SafetyMgt',8,96), @('OccHealthMgt',8,96), @('EnergyMgt',8,96),
  @('DataMgt',9,72), @('FileMgt',9,72), @('MessageMgt',9,72), @('IotMgt',9,72), @('BiMgt',9,72), @('ScreenMgt',9,72)
)

function Die($msg) { Write-Host "[FATAL] $msg" -ForegroundColor Red; exit 1 }
function Ok($msg)   { Write-Host "[OK] $msg" -ForegroundColor Green }
function Info($msg) { Write-Host "[..] $msg" -ForegroundColor Cyan }

foreach ($d in @($Vendor, $JarDir, $FeDir, $DumpDir, $CfgDir)) {
  if (-not (Test-Path $d)) { New-Item -ItemType Directory -Force -Path $d | Out-Null }
}

# ================= 1. Frontend build =================
if (-not $SkipFrontend) {
  Info 'Building frontend (vite)...'
  $fe = Join-Path $Root 'frontend'
  $npm = 'C:\Program Files\nodejs\npm.cmd'
  if (-not (Test-Path $npm)) { $npm = 'npm.cmd' }
  if (-not (Test-Path (Join-Path $fe 'node_modules'))) {
    Info 'node_modules missing, running npm ci...'
    Push-Location $fe; & $npm ci; if ($LASTEXITCODE -ne 0) { Pop-Location; Die 'npm ci failed' }; Pop-Location
  }
  Push-Location $fe
  & $npm run build
  $buildRc = $LASTEXITCODE
  Pop-Location
  if ($buildRc -ne 0) { Die 'frontend build failed' }
  $dist = Join-Path $fe 'dist'
  if (-not (Test-Path (Join-Path $dist 'index.html'))) { Die 'dist/index.html missing after build' }
  if (Test-Path $FeDir) { Remove-Item $FeDir -Recurse -Force }
  New-Item -ItemType Directory -Force -Path $FeDir | Out-Null
  Copy-Item "$dist\*" $FeDir -Recurse -Force
  $feSize = [math]::Round(((Get-ChildItem $FeDir -Recurse -File | Measure-Object Length -Sum).Sum / 1MB), 1)
  Ok "frontend dist copied ($feSize MB)"
} else {
  Info 'Skip frontend build'
}

# ================= 2. Collect jars =================
Info 'Collecting jars...'
$jarCount = 0
$missing = @()
foreach ($def in $WaveDefs) {
  $name = $def[0]
  $jar = Get-ChildItem (Join-Path $Root "$name\target\*-1.0.0.jar") -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notlike '*-sources*' -and $_.Name -notlike '*-javadoc*' } |
    Select-Object -First 1
  if (-not $jar) { $missing += $name; continue }
  Copy-Item $jar.FullName (Join-Path $JarDir "$name.jar") -Force
  $jarCount++
}
if ($missing.Count -gt 0) { Die ("missing jars: " + ($missing -join ', ')) }
if ($jarCount -ne 44) { Die "jar count=$jarCount, expected 44" }
Ok "44 jars collected"

# ================= 3. services.json =================
Info 'Generating services.json...'
$services = @()
foreach ($def in $WaveDefs) {
  $name = $def[0]; $wave = $def[1]; $xmx = $def[2]
  $yml = Join-Path $Root "$name\src\main\resources\application.yml"
  if (-not (Test-Path $yml)) { Die "application.yml missing for $name" }
  $txt = Get-Content $yml -Raw
  $port = 0
  if ($txt -match '(?m)^\s*port:\s*(\d+)') { $port = [int]$Matches[1] }
  if ($port -eq 0) { Die "port not found for $name" }
  $db = $null
  $m = [regex]::Match($txt, 'jdbc:mysql://[^/]+/(\w+)')
  if ($m.Success -and $m.Groups[1].Value -notmatch '\$') { $db = $m.Groups[1].Value }
  if (-not $db) {
    $m2 = [regex]::Match($txt, '\$\{DB_NAME:([\w-]+)\}')
    if ($m2.Success) { $db = $m2.Groups[1].Value }
  }
  if (-not $db) { Die "db name not found for $name" }
  $services += [ordered]@{
    name = $name; port = $port; db = $db; wave = $wave; xmx = $xmx
    jar  = "jars/$name.jar"
    tier = $(if ($wave -le 3) { 'core' } else { 'full' })
  }
}
$ports = $services | ForEach-Object { $_.port }
if ((@($ports | Sort-Object -Unique)).Count -ne $ports.Count) { Die 'duplicate ports in services.json' }
$json = $services | ConvertTo-Json -Depth 4
# PS5.1 emits ASCII-safe JSON already; ensure array form even for single item
if ($services.Count -eq 1) { $json = "[$json]" }
[System.IO.File]::WriteAllText((Join-Path $CfgDir 'services.json'), $json, (New-Object System.Text.UTF8Encoding($false)))
Ok "services.json written ($($services.Count) services)"

# DB list (unique) + optional extras
$dbs = @($services | ForEach-Object { $_.db } | Sort-Object -Unique)
$extraFile = Join-Path $Vendor 'extra-databases.txt'
if (Test-Path $extraFile) {
  $extras = @(Get-Content $extraFile | Where-Object { $_ -match '\S' } | ForEach-Object { $_.Trim() })
  $dbs = @($dbs + $extras | Sort-Object -Unique)
}
Write-Host ''
Write-Host '===== DATABASE LIST TO EXPORT =====' -ForegroundColor Yellow
$dbs | ForEach-Object { Write-Host "  $_" }
Write-Host "total: $($dbs.Count)" -ForegroundColor Yellow
Write-Host ''

# ================= 4. proxy-rules.json =================
Info 'Generating proxy-rules.json from vite.config.js...'
$viteCfg = Join-Path $Root 'frontend\vite.config.js'
$lines = Get-Content $viteCfg
$rules = New-Object System.Collections.ArrayList
foreach ($line in $lines) {
  $m = [regex]::Match($line, "^\s*'([^']+)':\s*\{\s*target:\s*'http://localhost:(\d+)'")
  if ($m.Success) {
    $key = $m.Groups[1].Value
    $port = $m.Groups[2].Value
    $type = $(if ($key.StartsWith('^')) { 'regex' } else { 'prefix' })
    [void]$rules.Add([ordered]@{ match = $key; type = $type; target = "http://127.0.0.1:$port" })
  }
}
if ($rules.Count -lt 50) { Die "only $($rules.Count) proxy rules extracted, expected ~90" }
$json = $rules | ConvertTo-Json -Depth 4
if ($rules.Count -eq 1) { $json = "[$json]" }
[System.IO.File]::WriteAllText((Join-Path $CfgDir 'proxy-rules.json'), $json, (New-Object System.Text.UTF8Encoding($false)))
Ok "proxy-rules.json written ($($rules.Count) rules)"

# ================= 5. mysqldump =================
$manifest = @{ mysqlSourceVersion = ''; exportedAt = (Get-Date).ToString('s'); databases = @() }
if (-not $SkipDump) {
  if (-not (Test-Path $DumpExe)) { Die "mysqldump not found: $DumpExe" }
  $verOut = cmd /c "`"$DumpExe`" --version 2>nul"
  $manifest.mysqlSourceVersion = "$verOut"
  Info "source: $verOut"
  foreach ($db in $dbs) {
    # verify db exists
    $chk = cmd /c "`"$MysqlExe`" -h127.0.0.1 -uroot -p$MysqlPwd -N -e `"SHOW DATABASES LIKE '$db'`" 2>nul"
    if ("$chk".Trim() -ne $db) { Write-Host "[WARN] database missing on server, skipped: $db" -ForegroundColor Yellow; continue }
    $sqlFile = Join-Path $DumpDir "$db.sql"
    $gzFile  = Join-Path $DumpDir "$db.sql.gz"
    Info "dumping $db ..."
    cmd /c "`"$DumpExe`" -h127.0.0.1 -uroot -p$MysqlPwd --single-transaction --routines --triggers --events --set-gtid-purged=OFF --column-statistics=0 --no-tablespaces --hex-blob --default-character-set=utf8mb4 --databases $db --result-file=`"$sqlFile`" 2>nul"
    if ($LASTEXITCODE -ne 0) { Die "mysqldump failed for $db (exit=$LASTEXITCODE)" }
    if (-not (Test-Path $sqlFile) -or (Get-Item $sqlFile).Length -lt 100) { Die "dump too small for $db" }
    # gzip via .NET
    $fsIn  = [System.IO.File]::OpenRead($sqlFile)
    $fsOut = [System.IO.File]::Create($gzFile)
    $gz    = New-Object System.IO.Compression.GzipStream($fsOut, [System.IO.Compression.CompressionMode]::Compress)
    $fsIn.CopyTo($gz)
    $gz.Close(); $fsOut.Close(); $fsIn.Close()
    $bytes = (Get-Item $gzFile).Length
    $sha   = (Get-FileHash $gzFile -Algorithm SHA256).Hash.ToLower()
    Remove-Item $sqlFile -Force
    $manifest.databases += @{ db = $db; file = "$db.sql.gz"; bytes = $bytes; sha256 = $sha }
    Ok "  $db -> $([math]::Round($bytes/1KB)) KB"
  }
  $mjson = $manifest | ConvertTo-Json -Depth 5
  [System.IO.File]::WriteAllText((Join-Path $DumpDir 'manifest.json'), $mjson, (New-Object System.Text.UTF8Encoding($false)))
  Ok "manifest.json written ($($manifest.databases.Count) databases)"
} else {
  Info 'Skip mysqldump'
}

Write-Host ''
Ok 'COLLECT_RESOURCES_DONE'
