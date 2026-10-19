# Host launcher for the 5 Strategy microservices (8101-8105) and 4 EEHS microservices (8106-8109)
# Stays alive (Wait-Process) so child JVMs are not reaped. Stop with Ctrl+C / kill this process tree.
$ErrorActionPreference = 'Continue'
$ROOT = 'd:\EMgtv3'
$LOG  = "$ROOT\logs\startup"
$JAVA = 'C:\Program Files\Java\jdk-17.0.18\bin\java.exe'
if (-not (Test-Path $JAVA)) { $JAVA = 'java' }
$mem = @('-Xmx128m','-Xms48m','-XX:+UseSerialGC','-XX:MaxMetaspaceSize=96m','-XX:ReservedCodeCacheSize=16m','-Dfile.encoding=UTF-8','-Dsun.jnu.encoding=UTF-8')

$services = @(
  'StrategyBlmMgt','StrategyDsteMgt','StrategySixsMgt','StrategyBudgetMgt','StrategyWorkplanMgt',
  'EnvironMgt','SafetyMgt','OccHealthMgt','EnergyMgt'
)

$pids = @()
foreach ($s in $services) {
  $jar = "$ROOT\$s\target\$s-1.0.0.jar"
  if (-not (Test-Path $jar)) { Write-Output "SKIP $s (jar missing)"; continue }
  $p = Start-Process -FilePath $JAVA -ArgumentList ($mem + @('-jar', $jar)) `
       -RedirectStandardOutput "$LOG\$s-out.log" -RedirectStandardError "$LOG\$s-err.log" `
       -WindowStyle Hidden -PassThru
  $pids += $p
  Write-Output ("START {0,-24} PID={1}" -f $s, $p.Id)
  Start-Sleep -Seconds 3
}
Write-Output "All 9 services launched, holding host..."
Wait-Process -Id $pids.Id -ErrorAction SilentlyContinue
