# ============================================================
# compat-gate.ps1 - Verify dev dumps import cleanly into bundled MySQL 8.4
# Spins up portable 8.4 on port 3307 with a temp datadir, imports all
# gz dumps, compares table counts with the dev server (3306), then
# boots IAM against 3307 on port 18081 and smoke-tests login.
# Usage: powershell -NoProfile -ExecutionPolicy Bypass -File compat-gate.ps1 [-SkipIamSmoke]
# Exit 0 = pass
# ============================================================
param([switch]$SkipIamSmoke)

$ErrorActionPreference = 'Stop'
$Desk   = 'd:\EMgtv3\desktop'
$Vendor = Join-Path $Desk 'vendor'
$DumpDir = Join-Path $Vendor 'db-dump'
$Tmp    = Join-Path $Desk 'tmp\compat'
$DataDir = Join-Path $Tmp 'data'
$Cnf    = Join-Path $Tmp 'my84.cnf'
$MysqlHome = Join-Path $Desk 'runtime\win\mysql'
$Mysqld = Join-Path $MysqlHome 'bin\mysqld.exe'
$Mysql  = Join-Path $MysqlHome 'bin\mysql.exe'
$MysqlAdmin = Join-Path $MysqlHome 'bin\mysqladmin.exe'
$Java   = 'C:\Program Files\Java\jdk-17.0.18\bin\java.exe'
$SrcMysql = 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe'
$Pwd    = 'Sun-0126King-0325'
$TestPort = 3307
$IamPort  = 18081

$fail = 0
function Pass($n) { Write-Host "[PASS] $n" -ForegroundColor Green }
function Fail($n, $w) { Write-Host "[FAIL] $n : $w" -ForegroundColor Red; $script:fail++ }
function Info($n) { Write-Host "[..] $n" -ForegroundColor Cyan }

function ToCnf($p) { return ($p -replace '\\', '/') }

# PS 5.1 + EAP=Stop: native commands writing to stderr while redirected throw
# NativeCommandError and abort the script. Run them in a child scope with
# EAP=Continue; check the returned exit code at the call site.
function Run-Native {
  param([string]$Exe, [string[]]$CmdArgs)
  $ErrorActionPreference = 'Continue'
  & $Exe @CmdArgs 2>$null | Out-Null
  return $LASTEXITCODE
}
function Run-NativeOut {
  param([string]$Exe, [string[]]$CmdArgs)
  $ErrorActionPreference = 'Continue'
  $out = & $Exe @CmdArgs 2>$null
  return $out
}

if (-not (Test-Path $Mysqld)) { Write-Host '[FATAL] portable mysqld not found, run download-runtime.ps1 first' -ForegroundColor Red; exit 1 }
if (-not (Test-Path (Join-Path $DumpDir 'manifest.json'))) { Write-Host '[FATAL] db-dump manifest missing, run collect-resources.ps1 first' -ForegroundColor Red; exit 1 }

# dev server (source of truth for table counts) must be up; degrade gracefully if we lack admin
$skipSrc = $false
$devSvc = Get-Service MySQL267 -ErrorAction SilentlyContinue
if ($devSvc -and $devSvc.Status -ne 'Running') {
  Write-Host '[..] attempting to start dev MySQL267 service...' -ForegroundColor Cyan
  try {
    Start-Service MySQL267 -ErrorAction Stop
    $ok = $false
    for ($i = 0; $i -lt 30; $i++) {
      Start-Sleep -Seconds 1
      if ((Get-Service MySQL267).Status -eq 'Running') { $ok = $true; break }
    }
    if (-not $ok) { $skipSrc = $true; Write-Host '[WARN] MySQL267 start timeout, skipping src comparison' -ForegroundColor Yellow }
  } catch {
    $skipSrc = $true
    Write-Host "[WARN] cannot start MySQL267 (need admin), skipping src comparison: $($_.Exception.Message)" -ForegroundColor Yellow
  }
} elseif (-not $devSvc) {
  $skipSrc = $true
  Write-Host '[WARN] MySQL267 service not found, skipping src comparison' -ForegroundColor Yellow
}

# cleanup previous
if (Get-Process mysqld84test -ErrorAction SilentlyContinue) { Stop-Process -Name mysqld84test -Force -ErrorAction SilentlyContinue }
$existing = Get-NetTCPConnection -LocalPort $TestPort -State Listen -ErrorAction SilentlyContinue
if ($existing) { Write-Host "[FATAL] port $TestPort already in use" -ForegroundColor Red; exit 1 }
if (Test-Path $Tmp) { Remove-Item $Tmp -Recurse -Force }
New-Item -ItemType Directory -Force -Path $DataDir | Out-Null

# my.cnf for 8.4 test instance (named pipe needed because skip-name-resolve blocks TCP root@localhost)
$PipeName = 'MySQLHRMTST'
$cnfBody = @"
[mysqld]
basedir=$(ToCnf $MysqlHome)
datadir=$(ToCnf $DataDir)
port=$TestPort
bind-address=127.0.0.1
skip-name-resolve=ON
lower_case_table_names=1
character-set-server=utf8mb4
collation-server=utf8mb4_0900_ai_ci
mysql_native_password=ON
innodb_buffer_pool_size=64M
performance_schema=OFF
mysqlx=OFF
enable-named-pipe=ON
socket=$PipeName
log-error=$(ToCnf $Tmp)/mysql.err
"@
[System.IO.File]::WriteAllText($Cnf, $cnfBody, (New-Object System.Text.UTF8Encoding($false)))

try {
  Info 'initialize-insecure datadir...'
  $rc = Run-Native $Mysqld @("--defaults-file=`"$Cnf`"", '--initialize-insecure')
  if ($rc -ne 0) { throw "initialize failed exit=$rc, see $Tmp\mysql.err" }
  Pass 'datadir initialized'

  Info 'starting mysqld 8.4 on 3307...'
  # socket via CLI arg: my.cnf socket= not reliably honored on Windows
  $proc = Start-Process -FilePath $Mysqld -ArgumentList @("--defaults-file=`"$Cnf`"", "--socket=$PipeName", '--enable-named-pipe') -WindowStyle Hidden -PassThru
  $up = $false
  for ($i = 0; $i -lt 60; $i++) {
    Start-Sleep -Seconds 1
    # Use named pipe (skip-name-resolve blocks TCP for root@localhost)
    $rc = Run-Native $MysqlAdmin @('--pipe', "--socket=$PipeName", '-u', 'root', 'ping')
    if ($rc -eq 0) { $up = $true; break }
  }
  if (-not $up) { throw 'mysqld 8.4 did not come up in 60s' }
  Pass 'mysqld 8.4 up'

  Info 'setting root password via named pipe...'
  $sql = "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$Pwd'; CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY '$Pwd'; GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION; FLUSH PRIVILEGES;"
  $rc = Run-Native $Mysql @('--pipe', "--socket=$PipeName", '-u', 'root', '-e', $sql)
  if ($rc -ne 0) { throw 'set root password failed' }
  Pass 'root password set'

  # import each dump via mysql source
  $manifest = Get-Content (Join-Path $DumpDir 'manifest.json') -Raw | ConvertFrom-Json
  $n = 0
  foreach ($d in $manifest.databases) {
    $n++
    $gz = Join-Path $DumpDir $d.file
    $sqlFile = Join-Path $Tmp ($d.db + '.sql')
    Info "importing $($d.db) ($n/$($manifest.databases.Count))..."
    $fsIn = [System.IO.File]::OpenRead($gz)
    $gzs = New-Object System.IO.Compression.GzipStream($fsIn, [System.IO.Compression.CompressionMode]::Decompress)
    $fsOut = [System.IO.File]::Create($sqlFile)
    $gzs.CopyTo($fsOut)
    $fsOut.Close(); $gzs.Close(); $fsIn.Close()
    $srcPath = ToCnf $sqlFile
    # Import via TCP (root@127.0.0.1 exists now)
    $rc = Run-Native $Mysql @('-h', '127.0.0.1', '-P', "$TestPort", '-u', 'root', "-p$Pwd", '--default-character-set=utf8mb4', '-e', "source $srcPath")
    if ($rc -ne 0) { Fail "import $($d.db)" "mysql exit=$rc" }
    Remove-Item $sqlFile -Force
  }
  if ($fail -eq 0) { Pass "all $($manifest.databases.Count) dumps imported" }

  # compare table counts 3306 vs 3307
  if ($skipSrc) {
    Write-Host '[SKIP] src comparison (dev MySQL unavailable)' -ForegroundColor Yellow
  } else {
    Info 'comparing table counts with dev server...'
    $mismatch = 0
    foreach ($d in $manifest.databases) {
      $db = $d.db
      $srcArg = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$db'"
      $src = Run-NativeOut $SrcMysql @('-h', '127.0.0.1', '-P', '3306', '-u', 'root', "-p$Pwd", '-N', '-e', $srcArg)
      $dst = Run-NativeOut $Mysql @('-h', '127.0.0.1', '-P', "$TestPort", '-u', 'root', "-p$Pwd", '-N', '-e', $srcArg)
      if ("$src".Trim() -ne "$dst".Trim()) { Fail "table-count $db" "src=$src dst=$dst"; $mismatch++ }
    }
    if ($mismatch -eq 0) { Pass 'table counts match dev server' }
  }

  # IAM smoke against 3307
  if (-not $SkipIamSmoke) {
    Info 'booting IAM against 8.4 (port 18081)...'
    $iamJar = Join-Path $Vendor 'jars\IAM.jar'
    $env:DB_PORT = "$TestPort"
    $iamLog = Join-Path $Tmp 'iam.out.log'
    $iamErr = Join-Path $Tmp 'iam.err.log'
    $iamProc = Start-Process -FilePath $Java -ArgumentList @('-Xmx96m','-Xms32m','-XX:+UseSerialGC','-XX:TieredStopAtLevel=1','-XX:MaxMetaspaceSize=72m','-Dfile.encoding=UTF-8','-jar',$iamJar,'--spring.sql.init.mode=never',"--server.port=$IamPort") -WindowStyle Hidden -RedirectStandardOutput $iamLog -RedirectStandardError $iamErr -PassThru
    $iamUp = $false
    for ($i = 0; $i -lt 90; $i++) {
      Start-Sleep -Seconds 1
      if (Get-NetTCPConnection -LocalPort $IamPort -State Listen -ErrorAction SilentlyContinue) { $iamUp = $true; break }
    }
    if (-not $iamUp) { Fail 'IAM boot' 'port 18081 not listening in 90s' }
    else {
      try {
        $body = @{ username = 'admin'; password = 'admin123' } | ConvertTo-Json -Compress
        $r = Invoke-RestMethod -Uri "http://127.0.0.1:$IamPort/api/auth/login" -Method Post -ContentType 'application/json' -Body $body -TimeoutSec 15
        if ($r.code -eq 200 -and $r.data.token) { Pass 'IAM login against MySQL 8.4' }
        else { Fail 'IAM login' "code=$($r.code) msg=$($r.message)" }
      } catch { Fail 'IAM login' $_.Exception.Message }
    }
    Stop-Process -Id $iamProc.Id -Force -ErrorAction SilentlyContinue
    $env:DB_PORT = $null
  }

  # sample row sanity: iam_db has admin user
  $cnt = Run-NativeOut $Mysql @('-h', '127.0.0.1', '-P', "$TestPort", '-u', 'root', "-p$Pwd", '-N', '-e', 'SELECT COUNT(*) FROM iam_db.iam_user')
  if ([int]("$cnt".Trim()) -gt 0) { Pass "iam_db.iam_user rows = $cnt" } else { Fail 'seed rows' "iam_db.iam_user count=$cnt" }
}
catch {
  Fail 'gate' $_.Exception.Message
}
finally {
  Info 'shutting down test instance...'
  # Use named pipe for shutdown (works regardless of TCP auth state)
  Run-Native $MysqlAdmin @('--pipe', "--socket=$PipeName", '-u', 'root', "-p$Pwd", 'shutdown') | Out-Null
  Start-Sleep -Seconds 3
}

Write-Host ''
if ($fail -eq 0) { Write-Host 'COMPAT_GATE_PASS' -ForegroundColor Green; exit 0 }
else { Write-Host "COMPAT_GATE_FAIL ($fail failures)" -ForegroundColor Red; exit 1 }
