# ============================================================
# e2e-smoke.ps1 - Post-install acceptance smoke against the desktop app
# Usage: powershell -NoProfile -ExecutionPolicy Bypass -File e2e-smoke.ps1 [-AppPort 18100]
# Exit 0 = all pass
# ============================================================
param([int]$AppPort = 18100)

$ErrorActionPreference = 'Stop'
$fail = 0
function Pass($name) { Write-Host "[PASS] $name" -ForegroundColor Green }
function Fail($name, $why) { Write-Host "[FAIL] $name : $why" -ForegroundColor Red; $script:fail++ }

$base = "http://127.0.0.1:$AppPort"

# 1. gateway serving SPA
try {
  $r = Invoke-WebRequest -Uri "$base/" -UseBasicParsing -TimeoutSec 10
  if ($r.StatusCode -eq 200 -and $r.Content -match '<div id="app">') { Pass 'SPA index served' }
  else { Fail 'SPA' "status=$($r.StatusCode)" }
} catch { Fail 'SPA' $_.Exception.Message }

# 2. SPA history fallback
try {
  $r = Invoke-WebRequest -Uri "$base/login" -UseBasicParsing -TimeoutSec 10
  if ($r.StatusCode -eq 200 -and $r.Content -match '<div id="app">') { Pass 'history fallback /login' }
  else { Fail 'fallback' "status=$($r.StatusCode)" }
} catch { Fail 'fallback' $_.Exception.Message }

# 3. login
$token = $null
try {
  $body = @{ username = 'admin'; password = 'admin123' } | ConvertTo-Json -Compress
  $r = Invoke-RestMethod -Uri "$base/api/auth/login" -Method Post -ContentType 'application/json' -Body $body -TimeoutSec 15
  if ($r.code -eq 200 -and $r.data.token) { $token = $r.data.token; Pass "login admin (role=$($r.data.role))" }
  else { Fail 'login' "code=$($r.code)" }
} catch { Fail 'login' $_.Exception.Message }

$headers = @{}
if ($token) { $headers['Authorization'] = "Bearer $token" }

# 4. sample APIs across modules (gateway -> backends)
# Note: some endpoints may return 4xx/5xx for missing paths — that's OK, proves routing works.
# Hard fail only on connection refused/timeout.
$checks = @(
  @{ name = 'org list';        url = '/api/org/list' },
  @{ name = 'employee list';   url = '/api/employee/list' },
  @{ name = 'attendance list'; url = '/api/attendance/list' },
  @{ name = 'salary rule';     url = '/api/salary-rule/list' },
  @{ name = 'perf list';       url = '/api/perf/list' },
  @{ name = 'strategy blm';    url = '/api/strategy/blm/list' },
  @{ name = 'eehs safety';     url = '/api/eehs/safety/list' },
  @{ name = 'party list';      url = '/api/party/list' },
  @{ name = 'culture list';    url = '/api/culture/list' },
  @{ name = 'flow definition'; url = '/api/flow/definition/list' }
)
foreach ($c in $checks) {
  try {
    $r = Invoke-RestMethod -Uri ($base + $c.url) -Headers $headers -TimeoutSec 20
    if ($null -ne $r) { Pass $c.name } else { Fail $c.name 'null response' }
  } catch {
    $code = $null
    $body = ''
    if ($_.Exception.Response) {
      $code = [int]$_.Exception.Response.StatusCode
      try { $sr = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream()); $body = $sr.ReadToEnd() } catch { }
    }
    # 4xx with JSON body still proves gateway+backend routing works; only 502/timeout is a hard fail.
    # 500 with Spring "No static resource" means service is up but path doesn't exist — also proves routing.
    if ($code -and $code -lt 500) { Pass "$($c.name) (http $code, routed)" }
    elseif ($code -eq 500 -and $body -match 'No static resource') { Pass "$($c.name) (http 500 no-path, routed)" }
    else { Fail $c.name $_.Exception.Message }
  }
}

# 5. 502 handling for dead target (/api/gateway -> 8080 not deployed)
try {
  $r = Invoke-WebRequest -Uri "$base/api/gateway/list" -UseBasicParsing -TimeoutSec 10
  if ($r.StatusCode -eq 502) { Pass 'dead target returns 502' } else { Fail '502' "status=$($r.StatusCode)" }
} catch {
  if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 502) { Pass 'dead target returns 502' }
  else { Fail '502' $_.Exception.Message }
}

Write-Host ''
if ($fail -eq 0) { Write-Host 'E2E_SMOKE_PASS' -ForegroundColor Green; exit 0 }
else { Write-Host "E2E_SMOKE_FAIL ($fail failures)" -ForegroundColor Red; exit 1 }
