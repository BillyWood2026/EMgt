$base = 'http://localhost:8196'
$endpoints = @('/api/discipline/case/list','/api/discipline/supervision/list','/api/discipline/integrity/list')
foreach ($e in $endpoints) {
  try {
    $r = Invoke-WebRequest -Uri ($base + $e) -UseBasicParsing -TimeoutSec 10
    Write-Output ($e + ' -> ' + [int]$r.StatusCode + ' | ' + $r.Content.Substring(0, [Math]::Min(120, $r.Content.Length)))
  } catch {
    $code = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode.value__ } else { 'ERR' }
    Write-Output ($e + ' -> ' + $code)
  }
}
