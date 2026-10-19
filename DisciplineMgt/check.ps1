Start-Sleep -Seconds 25
$up = Test-NetConnection -ComputerName localhost -Port 8196 -WarningAction SilentlyContinue
Write-Output ('Port8196=' + $up.TcpTestSucceeded)
