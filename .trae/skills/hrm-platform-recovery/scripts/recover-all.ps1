# HRM platform one-click recovery (ASCII only).
# Brings up all 30 backend JVMs + vite from a fully-stopped state.
# Usage:  powershell -NoProfile -ExecutionPolicy Bypass -File recover-all.ps1 [-SkipVite]
# Idempotent: already-listening ports are skipped; safe to re-run.
param([switch]$SkipVite)

$ErrorActionPreference = 'Continue'
$Root = 'd:\EMgtv3'
$LogDir = Join-Path $Root 'tmp\recovery'
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$Java = 'C:\Program Files\Java\jdk-17.0.18\bin\java.exe'

# ---- preflight: MySQL must be up ----
if (-not (Get-Process mysqld -ErrorAction SilentlyContinue)) {
  Write-Host '[FATAL] mysqld not running. Start MySQL service first.' -ForegroundColor Red
  exit 1
}

# ---- service directory names in 6 startup waves (5 each) ----
$Waves = @(
  @('OrgMgt','EmployeeMgt','AttendanceMgt','RecruitmentMgt','PerfMgt'),
  @('TrainingMgt','QualificationMgt','SalaryMgt','SkillCertMgt','TalentMgt'),
  @('ProjectMgt','HRPlanningMgt','HolidayMgt','TalentReviewMgt','SuccessionPlanMgt'),
  @('TalentDevMgt','OnlineLearnMgt','OnlineExamMgt','AuditMgt','DisciplineMgt'),
  @('PartyAffairsMgt','CultureMgt','ProcessMgt','ImproveMgt','FlowEngineMgt'),
  @('StrategyBlmMgt','StrategyDsteMgt','StrategySixsMgt','StrategyBudgetMgt','StrategyWorkplanMgt')
)

function Get-Port([string]$svc) {
  $yml = Join-Path $Root "$svc\src\main\resources\application.yml"
  $txt = Get-Content $yml -Raw
  if ($txt -match '(?m)^\s*port:\s*(\d+)') { return [int]$Matches[1] }
  return 0
}

function Test-PortUp([int]$port) {
  return [bool](Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)
}

function Start-Svc([string]$svc) {
  $port = Get-Port $svc
  if ($port -eq 0) { Write-Host "[$svc] port unknown, SKIP"; return $port }
  if (Test-PortUp $port) { Write-Host "[$svc :$port] already UP, skip"; return $port }
  $jar = Get-ChildItem (Join-Path $Root "$svc\target\*.jar") |
    Where-Object { $_.Name -like '*-1.0.0.jar' -and $_.Name -notlike '*-sources*' -and $_.Name -notlike '*-javadoc*' } |
    Select-Object -First 1
  if (-not $jar) { Write-Host "[$svc] jar not found, SKIP"; return $port }
  $out = Join-Path $LogDir "$svc.out.log"
  $err = Join-Path $LogDir "$svc.err.log"
  $args = @('-Xmx128m','-XX:+UseSerialGC','-XX:TieredStopAtLevel=1','-Dfile.encoding=UTF-8',
            '-jar',$jar.FullName,'--spring.sql.init.mode=never')
  Start-Process -FilePath $Java -ArgumentList $args -WindowStyle Hidden `
    -WorkingDirectory (Join-Path $Root $svc) -RedirectStandardOutput $out -RedirectStandardError $err | Out-Null
  Write-Host "[$svc :$port] launched"
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

$wi = 0
foreach ($wave in $Waves) {
  $wi++
  Write-Host "=== WAVE $wi : $($wave -join ', ') ==="
  $ports = @()
  foreach ($svc in $wave) { $ports += (Start-Svc $svc) }
  Wait-Ports $ports 150
  foreach ($p in $ports) {
    if ($p -eq 0) { continue }
    Write-Host ("  port {0} : {1}" -f $p, $(if (Test-PortUp $p) { 'UP' } else { 'DOWN' }))
  }
}

# ---- vite frontend 5174 ----
if (-not $SkipVite) {
  Write-Host '=== VITE 5174 ==='
  if (-not (Test-PortUp 5174)) {
    $fe = Join-Path $Root 'frontend'
    Start-Process -FilePath 'C:\Program Files\nodejs\node.exe' `
      -ArgumentList 'node_modules/vite/bin/vite.js' -WorkingDirectory $fe -WindowStyle Hidden | Out-Null
    Wait-Ports @(5174) 90
  }
  Write-Host ("  vite 5174 : {0}" -f $(if (Test-PortUp 5174) { 'UP' } else { 'DOWN' }))
}

# ---- final summary ----
Write-Host '=== FINAL SUMMARY ==='
$AllPorts = @(8082,8083,8084,8085,8086,8087,8090,8091,8092,8093,8094,8095,
              8101,8102,8103,8104,8105,8142,8143,8145,8150,8160,
              8190,8191,8192,8193,8194,8195,8196,8206,5174)
$up = @(); $down = @()
foreach ($p in $AllPorts) { if (Test-PortUp $p) { $up += $p } else { $down += $p } }
Write-Host ("UP ({0}): {1}" -f $up.Count, ($up -join ','))
Write-Host ("DOWN ({0}): {1}" -f $down.Count, ($down -join ','))
Write-Host 'RECOVERY_DONE'
if ($down.Count -gt 0) { exit 1 } else { exit 0 }
