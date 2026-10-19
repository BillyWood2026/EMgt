# ============================================================
# HRM 全平台一键启动 (ASCII only, PowerShell 5.1 compatible)
# 覆盖：MySQL + 44 个微服务 + Vite 前端，共 9 波内存门禁
# 用法： powershell -NoProfile -ExecutionPolicy Bypass -File start-all.ps1
# 幂等：已在线的端口自动跳过，可重复执行
# 退出码：0=全部在线 1=有端口离线
# ============================================================
param([switch]$SkipVite, [switch]$SkipMySql)

$ErrorActionPreference = 'Continue'
$Root       = 'd:\EMgtv3'
$LogDir     = Join-Path $Root 'tmp\recovery'
$Java       = 'C:\Program Files\Java\jdk-17.0.18\bin\java.exe'
$Node       = 'C:\Program Files\nodejs\node.exe'
$MysqlExe   = 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe'
$MysqlPwd   = 'Sun-0126King-0325'
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

# ---- 0. MySQL 预检/自动拉起 ----
if (-not $SkipMySql) {
  if (-not (Get-Process mysqld -ErrorAction SilentlyContinue)) {
    Write-Host '[MySQL] not running, auto-starting...' -ForegroundColor Yellow
    $svc = Get-Service -Name '*mysql*' -ErrorAction SilentlyContinue | Where-Object { $_.Status -ne 'Running' } | Select-Object -First 1
    if ($svc) { try { Start-Service $svc.Name -ErrorAction Stop; Write-Host "[MySQL] service '$($svc.Name)' started" } catch { Write-Host "[MySQL] cannot start service: $_" -ForegroundColor Red } }
    Start-Sleep -Seconds 3
  }
  if (Get-Process mysqld -ErrorAction SilentlyContinue) {
    $test = & $MysqlExe -uroot "-p$MysqlPwd" -N -e "SELECT 1" 2>&1
    if ($LASTEXITCODE -eq 0) { Write-Host '[MySQL] UP (auth ok)' -ForegroundColor Green }
    else { Write-Host '[MySQL] UP but auth failed, check password' -ForegroundColor Red; exit 1 }
  } else {
    Write-Host '[FATAL] MySQL not available' -ForegroundColor Red; exit 1
  }
}

# ---- 波次定义：(服务名, 堆内存MB) ----
$Waves = @(
  # W1 基础设施 4
  @(@('IAM',96),@('module-config-center',96),@('SysMgt',96),@('SysMonitor',96)),
  # W2 核心HRM 5
  @(@('OrgMgt',128),@('EmployeeMgt',128),@('AttendanceMgt',128),@('RecruitmentMgt',128),@('PerfMgt',128)),
  # W3 HR续 5
  @(@('TrainingMgt',128),@('QualificationMgt',128),@('SalaryMgt',128),@('SkillCertMgt',128),@('TalentMgt',128)),
  # W4 人才/项目 5
  @(@('ProjectMgt',128),@('HRPlanningMgt',128),@('HolidayMgt',128),@('TalentReviewMgt',128),@('SuccessionPlanMgt',128)),
  # W5 在线/审计 5
  @(@('TalentDevMgt',128),@('OnlineLearnMgt',128),@('OnlineExamMgt',128),@('AuditMgt',128),@('DisciplineMgt',128)),
  # W6 文化/流程/改善 5
  @(@('PartyAffairsMgt',128),@('CultureMgt',128),@('ProcessMgt',128),@('ImproveMgt',128),@('FlowEngineMgt',128)),
  # W7 战略 5
  @(@('StrategyBlmMgt',96),@('StrategyDsteMgt',96),@('StrategySixsMgt',96),@('StrategyBudgetMgt',96),@('StrategyWorkplanMgt',96)),
  # W8 EEHS 4
  @(@('EnvironMgt',96),@('SafetyMgt',96),@('OccHealthMgt',96),@('EnergyMgt',96)),
  # W9 中台 6
  @(@('DataMgt',72),@('FileMgt',72),@('MessageMgt',72),@('IotMgt',72),@('BiMgt',72),@('ScreenMgt',72))
)

function Get-Port([string]$svc) {
  $yml = Join-Path $Root "$svc\src\main\resources\application.yml"
  if (-not (Test-Path $yml)) { return 0 }
  $txt = Get-Content $yml -Raw
  if ($txt -match '(?m)^\s*port:\s*(\d+)') { return [int]$Matches[1] }
  return 0
}

function Test-PortUp([int]$port) {
  return [bool](Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)
}

function Start-Svc([string]$svc, [int]$xmx) {
  $port = Get-Port $svc
  if ($port -eq 0) { Write-Host "  [$svc] port unknown, SKIP"; return 0 }
  if (Test-PortUp $port) { Write-Host "  [$svc :$port] already UP, skip"; return $port }
  $jar = Get-ChildItem (Join-Path $Root "$svc\target\*.jar") |
    Where-Object { $_.Name -like '*-1.0.0.jar' -and $_.Name -notlike '*-sources*' -and $_.Name -notlike '*-javadoc*' } |
    Select-Object -First 1
  if (-not $jar) { Write-Host "  [$svc] jar not found, SKIP"; return 0 }
  $out = Join-Path $LogDir "$svc.out.log"
  $err = Join-Path $LogDir "$svc.err.log"
  $jargs = @("-Xmx${xmx}m",'-Xms32m','-XX:+UseSerialGC','-XX:TieredStopAtLevel=1',
             '-XX:MaxMetaspaceSize=72m','-XX:ReservedCodeCacheSize=48m','-Xss512k',
             '-Dfile.encoding=UTF-8',
             '-jar',$jar.FullName,'--spring.sql.init.mode=never')
  Start-Process -FilePath $Java -ArgumentList $jargs -WindowStyle Hidden `
    -WorkingDirectory (Join-Path $Root $svc) -RedirectStandardOutput $out -RedirectStandardError $err | Out-Null
  Write-Host "  [$svc :$port] launched (Xmx${xmx}m)"
  return $port
}

function Wait-Ports([int[]]$ports, [int]$timeoutSec) {
  $deadline = (Get-Date).AddSeconds($timeoutSec)
  $pending = @($ports | Where-Object { $_ -ne 0 })
  while ((Get-Date) -lt $deadline -and $pending.Count -gt 0) {
    Start-Sleep -Seconds 5
    $pending = @($pending | Where-Object { -not (Test-PortUp $_) })
  }
}

# ---- 逐波启动 ----
$wi = 0
foreach ($wave in $Waves) {
  $wi++
  Write-Host "`n=== WAVE $wi ===" -ForegroundColor Cyan
  $ports = @()
  foreach ($pair in $wave) {
    $svc = $pair[0]; $xmx = $pair[1]
    $ports += (Start-Svc $svc $xmx)
  }
  Wait-Ports $ports 150
  foreach ($p in $ports) {
    if ($p -eq 0) { continue }
    $st = if (Test-PortUp $p) { 'UP' } else { 'DOWN' }
    Write-Host ("  port {0} : {1}" -f $p, $st)
  }
}

# ---- Vite 前端 5174 ----
if (-not $SkipVite) {
  Write-Host "`n=== VITE 5174 ===" -ForegroundColor Cyan
  if (-not (Test-PortUp 5174)) {
    $fe = Join-Path $Root 'frontend'
    $vOut = Join-Path $LogDir 'vite.out.log'
    $vErr = Join-Path $LogDir 'vite.err.log'
    Start-Process -FilePath $Node -ArgumentList 'node_modules/vite/bin/vite.js','--host','0.0.0.0' `
      -WorkingDirectory $fe -WindowStyle Hidden -RedirectStandardOutput $vOut -RedirectStandardError $vErr | Out-Null
    Wait-Ports @(5174) 90
  }
  Write-Host ("  vite 5174 : {0}" -f $(if (Test-PortUp 5174) { 'UP' } else { 'DOWN' }))

  # ---- 前端登录 + 打开浏览器 ----
  if (Test-PortUp 5174) {
    $loginOk = $false
    try {
      $body = @{username='admin';password='admin123'} | ConvertTo-Json -Compress
      $resp = Invoke-RestMethod -Uri 'http://localhost:8081/api/auth/login' -Method Post -ContentType 'application/json' -Body $body
      if ($resp.code -eq 200 -and $resp.data.token) {
        $token = $resp.data.token
        $lUser = $resp.data.username
        $lRole = $resp.data.role
        Write-Host "  [LOGIN] $lUser/$lRole OK" -ForegroundColor Green
        $html = @"
<!DOCTYPE html><html><head><meta charset='utf-8'><title>Auto Login</title></head>
<body><p>Logging in...</p>
<script>
localStorage.setItem('token','$token');
localStorage.setItem('username','$lUser');
localStorage.setItem('role','$lRole');
location.href='http://localhost:5174/';
</script></body></html>
"@
        $autoLogin = Join-Path $LogDir 'auto-login.html'
        Set-Content -Path $autoLogin -Value $html -Encoding UTF8
        Start-Process $autoLogin
        Write-Host '  [BROWSER] auto-login opened (token injected, redirect to 5174)' -ForegroundColor Green
        $loginOk = $true
      } else {
        Write-Host "  [LOGIN] API code=$($resp.code)" -ForegroundColor Red
      }
    } catch {
      Write-Host "  [LOGIN] error: $($_.Exception.Message)" -ForegroundColor Red
    }
    if (-not $loginOk) {
      Start-Process 'http://localhost:5174'
      Write-Host '  [BROWSER] opened http://localhost:5174 (manual login needed)' -ForegroundColor Yellow
    }
  }
}

# ---- 汇总 ----
Write-Host "`n========== FINAL SUMMARY ==========" -ForegroundColor Yellow
$allSvcs = @()
foreach ($w in $Waves) { foreach ($p in $w) { $allSvcs += $p[0] } }
$up = @(); $down = @()
foreach ($svc in $allSvcs) {
  $p = Get-Port $svc
  if ($p -eq 0) { continue }
  if (Test-PortUp $p) { $up += $p } else { $down += "$svc`:$p" }
}
if (-not $SkipVite) {
  if (Test-PortUp 5174) { $up += 5174 } else { $down += 'vite:5174' }
}
Write-Host ("BACKEND UP ({0}): {1}" -f $up.Count, ($up -join ','))
if ($down.Count -gt 0) {
  Write-Host ("DOWN ({0}): {1}" -f $down.Count, ($down -join ', ')) -ForegroundColor Red
} else {
  Write-Host 'ALL UP - zero down' -ForegroundColor Green
}
Write-Host 'START_ALL_DONE'
if ($down.Count -gt 0) { exit 1 } else { exit 0 }
