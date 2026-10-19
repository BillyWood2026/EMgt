$os = Get-CimInstance Win32_OperatingSystem
$freeGB = [math]::Round($os.FreePhysicalMemory/1MB, 2)
$totalGB = [math]::Round($os.TotalVisibleMemorySize/1MB, 2)
Write-Host "Free: $freeGB GB / Total: $totalGB GB"
