# =====================================================================================
# stop-all.ps1 - Stop all EMgtv2 microservices and Vite frontend
# =====================================================================================
# Usage:
#   powershell -NoProfile -ExecutionPolicy Bypass -File stop-all.ps1
# =====================================================================================

$ErrorActionPreference = 'Continue'

Write-Output '========== Stopping EMgtv2 Platform =========='

# Kill all Java processes (microservices)
$javaProcs = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcs) {
  Write-Output "Stopping $($javaProcs.Count) Java process(es)..."
  $javaProcs | ForEach-Object {
    Write-Output "  Killing PID=$($_.Id) Name=$($_.ProcessName)"
    Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
  }
  Start-Sleep -Seconds 2
  $remain = Get-Process java -ErrorAction SilentlyContinue
  Write-Output "Java processes remaining: $(if($remain){$remain.Count}else{0})"
} else {
  Write-Output 'No Java processes found.'
}

# Kill Vite/Node frontend (port 5174)
$vitePort = 5174
$viteConn = Get-NetTCPConnection -State Listen -LocalPort $vitePort -ErrorAction SilentlyContinue
if ($viteConn) {
  $vitePid = $viteConn.OwningProcess | Select-Object -First 1
  Write-Output "Stopping Vite frontend (PID=$vitePid, port=$vitePort)..."
  Stop-Process -Id $vitePid -Force -ErrorAction SilentlyContinue
  Start-Sleep -Seconds 1
} else {
  Write-Output "Vite not running on port $vitePort."
}

# Also kill any stray node processes running vite
$nodeProcs = Get-Process node -ErrorAction SilentlyContinue
if ($nodeProcs) {
  foreach ($np in $nodeProcs) {
    # Check if this node process is listening on 5174
    $listening = Get-NetTCPConnection -State Listen -OwningProcess $np.Id -ErrorAction SilentlyContinue
    if ($listening.LocalPort -contains $vitePort) {
      Write-Output "Stopping Vite node process (PID=$($np.Id))..."
      Stop-Process -Id $np.Id -Force -ErrorAction SilentlyContinue
    }
  }
}

# Verify ports are free
$allPorts = @(8080,8081,8082,8083,8084,8085,8086,8087,8088,8089,8090,8091,8092,8093,8094,8095,8101,8102,8103,8111,8112,8142,8848,8849,9000,5174)
$stillUp = @()
foreach ($port in $allPorts) {
  if (Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue) {
    $stillUp += $port
  }
}

Write-Output ''
if ($stillUp.Count -eq 0) {
  Write-Output 'All services stopped successfully.'
} else {
  Write-Output "WARNING: Ports still in use: $($stillUp -join ', ')"
  Write-Output 'You may need to manually kill processes or wait a few seconds.'
}

Write-Output 'Done.'
