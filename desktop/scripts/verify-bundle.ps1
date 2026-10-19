# ============================================================
# verify-bundle.ps1 - Pre-pack integrity gate
# Checks: 44 jars, services.json, proxy-rules.json, gz dumps + sha256
# Exit 0 = pass, 1 = fail
# ============================================================
$ErrorActionPreference = 'Stop'
$Desk    = 'd:\EMgtv3\desktop'
$Vendor  = Join-Path $Desk 'vendor'
$CfgDir  = Join-Path $Desk 'src\config'

$fail = 0
function Pass($name) { Write-Host "[PASS] $name" -ForegroundColor Green }
function Fail($name, $why) { Write-Host "[FAIL] $name : $why" -ForegroundColor Red; $script:fail++ }

# 1. jars
$jars = @(Get-ChildItem (Join-Path $Vendor 'jars\*.jar') -ErrorAction SilentlyContinue)
if ($jars.Count -eq 44) { Pass "jars count = 44" } else { Fail 'jars' "count=$($jars.Count)" }
$small = @($jars | Where-Object { $_.Length -lt 10MB })
if ($small.Count -eq 0) { Pass 'jars all >= 10MB' } else { Fail 'jars' ("suspicious small: " + ($small.Name -join ',')) }

# 2. services.json
try {
  $services = Get-Content (Join-Path $CfgDir 'services.json') -Raw | ConvertFrom-Json
  if ($services.Count -eq 44) { Pass 'services.json count = 44' } else { Fail 'services.json' "count=$($services.Count)" }
  $up = @($services | ForEach-Object { $_.port } | Sort-Object -Unique)
  if ($up.Count -eq $services.Count) { Pass 'services ports unique' } else { Fail 'services.json' 'duplicate ports' }
  $badJar = @($services | Where-Object { -not (Test-Path (Join-Path $Vendor $_.jar)) })
  if ($badJar.Count -eq 0) { Pass 'all jar paths exist' } else { Fail 'services.json' ("missing jars: " + ($badJar.name -join ',')) }
} catch { Fail 'services.json' $_.Exception.Message }

# 3. proxy-rules.json
try {
  $rules = Get-Content (Join-Path $CfgDir 'proxy-rules.json') -Raw | ConvertFrom-Json
  if ($rules.Count -ge 50) { Pass "proxy-rules count = $($rules.Count)" } else { Fail 'proxy-rules' "count=$($rules.Count)" }
  $regexCount = @($rules | Where-Object { $_.type -eq 'regex' }).Count
  if ($regexCount -ge 8) { Pass "regex rules = $regexCount" } else { Fail 'proxy-rules' "regex=$regexCount expected >=8" }
} catch { Fail 'proxy-rules.json' $_.Exception.Message }

# 4. db dumps
$manifestFile = Join-Path $Vendor 'db-dump\manifest.json'
if (-not (Test-Path $manifestFile)) { Fail 'manifest' 'manifest.json missing' }
else {
  $manifest = Get-Content $manifestFile -Raw | ConvertFrom-Json
  if ($manifest.databases.Count -ge 40) { Pass "dump count = $($manifest.databases.Count)" } else { Fail 'manifest' "dbs=$($manifest.databases.Count)" }
  foreach ($d in $manifest.databases) {
    $f = Join-Path $Vendor ('db-dump\' + $d.file)
    if (-not (Test-Path $f)) { Fail 'dump' "missing $($d.file)"; continue }
    $sha = (Get-FileHash $f -Algorithm SHA256).Hash.ToLower()
    if ($sha -ne $d.sha256) { Fail 'dump' "sha256 mismatch $($d.file)"; continue }
    # gzip readable + contains CREATE DATABASE
    try {
      $fsIn = [System.IO.File]::OpenRead($f)
      $gz = New-Object System.IO.Compression.GzipStream($fsIn, [System.IO.Compression.CompressionMode]::Decompress)
      $sr = New-Object System.IO.StreamReader($gz)
      $head = $sr.ReadLine() + $sr.ReadLine() + $sr.ReadLine() + $sr.ReadLine() + $sr.ReadLine()
      $sr.Close(); $gz.Close(); $fsIn.Close()
    } catch { Fail 'dump' "gunzip failed $($d.file)" }
  }
  Pass 'dump gz files readable + sha256 ok'
  # spot check first dump contains CREATE DATABASE
  $first = Join-Path $Vendor ('db-dump\' + $manifest.databases[0].file)
  $fsIn = [System.IO.File]::OpenRead($first)
  $gz = New-Object System.IO.Compression.GzipStream($fsIn, [System.IO.Compression.CompressionMode]::Decompress)
  $sr = New-Object System.IO.StreamReader($gz)
  $found = $false
  for ($i = 0; $i -lt 60; $i++) { $l = $sr.ReadLine(); if ($l -match 'CREATE DATABASE') { $found = $true; break } }
  $sr.Close(); $gz.Close(); $fsIn.Close()
  if ($found) { Pass 'dump contains CREATE DATABASE' } else { Fail 'dump' 'no CREATE DATABASE in first 60 lines' }
}

# 5. frontend dist
if (Test-Path (Join-Path $Vendor 'frontend\index.html')) { Pass 'frontend dist present' } else { Fail 'frontend' 'index.html missing' }

Write-Host ''
if ($fail -eq 0) { Write-Host 'VERIFY_BUNDLE_PASS' -ForegroundColor Green; exit 0 }
else { Write-Host "VERIFY_BUNDLE_FAIL ($fail failures)" -ForegroundColor Red; exit 1 }
