# Stop SkillCertMgt on port 8093
$conn = Get-NetTCPConnection -State Listen -LocalPort 8093 -ErrorAction SilentlyContinue
if ($conn) {
    Stop-Process -Id $conn.OwningProcess -Force
    Write-Host "Stopped PID $($conn.OwningProcess) on port 8093"
    Start-Sleep -Seconds 3
} else {
    Write-Host "Port 8093 not in use"
}

# Rebuild
$mvn = 'C:\Users\A\AppData\Local\apache-maven-3.9.9\bin\mvn.cmd'
& $mvn -f D:\EMgtv2\SkillCertMgt\pom.xml package -DskipTests -q
Write-Host "Build exit code: $LASTEXITCODE"

# Restart
$java = 'C:\Program Files\Java\jdk-17.0.18\bin\java.exe'
$jar = 'D:\EMgtv2\SkillCertMgt\target\SkillCertMgt-1.0.0.jar'
$nacosOff = @(
    '--spring.cloud.nacos.config.enabled=false',
    '--spring.cloud.nacos.discovery.enabled=false',
    '--spring.cloud.nacos.config.import-check.enabled=false',
    '--spring.cloud.bootstrap.enabled=false',
    '--spring.cloud.nacos.discovery.register-enabled=false',
    '--spring.sql.init.mode=never',
    '--spring.datasource.hikari.maximum-pool-size=5',
    '--spring.datasource.hikari.minimum-idle=1'
)
$argList = [System.Collections.Generic.List[string]]::new()
$argList.Add('-Xmx160m')
$argList.Add('-Xms48m')
$argList.Add('-XX:+UseSerialGC')
$argList.Add('-XX:MaxMetaspaceSize=96m')
$argList.Add('-XX:ReservedCodeCacheSize=16m')
$argList.Add('-Dfile.encoding=UTF-8')
$argList.Add('-Dsun.jnu.encoding=UTF-8')
$argList.Add('-jar')
$argList.Add($jar)
$argList.Add('--server.port=8093')
foreach ($f in $nacosOff) { $argList.Add($f) }

$proc = Start-Process -FilePath $java -ArgumentList $argList `
    -RedirectStandardOutput 'D:\EMgtv2\logs\startup\SkillCertMgt-stdout.log' `
    -RedirectStandardError 'D:\EMgtv2\logs\startup\SkillCertMgt-stderr.log' `
    -WindowStyle Hidden -PassThru

Write-Host "Started SkillCertMgt PID=$($proc.Id)"
Start-Sleep -Seconds 15

# Test
try {
    $resp = Invoke-WebRequest -Uri 'http://localhost:8093/api/skill-cert/standard/list' -UseBasicParsing -TimeoutSec 10
    Write-Host "API test: $($resp.StatusCode) - $($resp.Content.Substring(0, [Math]::Min(200, $resp.Content.Length)))"
} catch {
    Write-Host "API test FAILED: $($_.Exception.Message)"
}
