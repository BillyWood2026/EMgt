# =====================================================================================
# start-all.ps1 - EMgtv2 Platform One-Click Startup Script
# =====================================================================================
# Starts: MySQL check -> Maven (auto-download) -> Build -> 15 microservices -> Frontend
#
# Usage:
#   powershell -NoProfile -ExecutionPolicy Bypass -File start-all.ps1              # Full build + start
#   powershell -NoProfile -ExecutionPolicy Bypass -File start-all.ps1 -SkipBuild  # Start with existing JARs
#   powershell -NoProfile -ExecutionPolicy Bypass -File start-all.ps1 -BuildOnly   # Only build, don't start
#   powershell -NoProfile -ExecutionPolicy Bypass -File start-all.ps1 -SkipFrontend # Skip Vite
#
# Press Ctrl+C in this window to stop all services.
# =====================================================================================

param(
  [switch]$SkipBuild,
  [switch]$SkipFrontend,
  [switch]$BuildOnly,
  [switch]$SkipMavenDownload
)

$ErrorActionPreference = 'Continue'

# ============================== CONFIGURATION ==============================

$PROJECT_ROOT       = 'D:\EMgtv3'
$JAVA_HOME          = 'C:\Program Files\Java\jdk-17.0.18'
$JAVA               = "$JAVA_HOME\bin\java.exe"
$NPM                = 'C:\Program Files\nodejs\npm.cmd'
$MYSQL              = 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe'
$MYSQL_ROOT_PW      = 'Sun-0126King-0325'
$MAVEN_VERSION      = '3.9.9'
$MAVEN_URLS        = @(
  "https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip",
  "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip"
)
$LOG_DIR            = "$PROJECT_ROOT\logs\startup"
$VITE_PORT          = 5174

# Service registry: Name, Port, SourceDir (relative to PROJECT_ROOT), JAR name, Wave
#   Wave 1 = Infrastructure | Wave 2 = Core | Wave 3 = Business
$svcDefs = @(
  @{N='ServiceRegistry';  P=8848; D='Infrastructure\ServiceRegistry';  J='ServiceRegistry-1.0.0.jar';       W=1}
  @{N='ConfigServer';     P=8849; D='Infrastructure\ConfigServer';     J='ConfigServer-1.0.0.jar';          W=1}
  @{N='APIGateway';       P=8080; D='Infrastructure\APIGateway';       J='APIGateway-1.0.0.jar';            W=1}
  @{N='IAM';              P=8081; D='IAM';                            J='IAM-1.0.0.jar';                   W=2}
  @{N='OrgMgt';           P=8082; D='OrgMgt';                         J='OrgMgt-1.0.0.jar';                W=2}
  @{N='ModuleConfig';     P=9000; D='module-config-center';           J='module-config-center-1.0.0.jar';  W=2}
  @{N='SysMgt';           P=8111; D='SysMgt';                         J='SysMgt-1.0.0.jar';                W=2}
  @{N='SysMonitor';       P=8112; D='SysMonitor';                    J='SysMonitor-1.0.0.jar';            W=2}
  @{N='ProcessMgt';       P=8089; D='ProcessMgt';                    J='ProcessMgt-1.0.0.jar';            W=3}
  @{N='ProjectMgt';       P=8091; D='ProjectMgt';                    J='ProjectMgt-1.0.0.jar';            W=3}
  @{N='ImproveMgt';       P=8142; D='ImproveMgt';                    J='ImproveMgt-1.0.0.jar';            W=3}
  # 纪检审计板块：纪检监督 DisciplineMgt 8196（审计 AuditMgt 8145 独立运行）
  @{N='DisciplineMgt';    P=8196; D='DisciplineMgt';                 J='DisciplineMgt-1.0.0.jar';        W=3}
  # 战略管理板块：5 个微服务（前端栏目仍为一个“战略管理”板块）
  @{N='StrategyBlmMgt';       P=8101; D='StrategyBlmMgt';       J='StrategyBlmMgt-1.0.0.jar';       W=3}
  @{N='StrategyDsteMgt';      P=8102; D='StrategyDsteMgt';      J='StrategyDsteMgt-1.0.0.jar';      W=3}
  @{N='StrategySixsMgt';      P=8103; D='StrategySixsMgt';      J='StrategySixsMgt-1.0.0.jar';      W=3}
  @{N='StrategyBudgetMgt';    P=8104; D='StrategyBudgetMgt';    J='StrategyBudgetMgt-1.0.0.jar';    W=3}
  @{N='StrategyWorkplanMgt';  P=8105; D='StrategyWorkplanMgt';  J='StrategyWorkplanMgt-1.0.0.jar';  W=3}
  # EEHS 板块：环境/安全/职业健康/能源 4 个微服务（前端栏目仍为一个“EEHS”板块）
  @{N='EnvironMgt';       P=8106; D='EnvironMgt';       J='EnvironMgt-1.0.0.jar';       W=3}
  @{N='SafetyMgt';        P=8107; D='SafetyMgt';        J='SafetyMgt-1.0.0.jar';        W=3}
  @{N='OccHealthMgt';     P=8108; D='OccHealthMgt';     J='OccHealthMgt-1.0.0.jar';     W=3}
  @{N='EnergyMgt';        P=8109; D='EnergyMgt';        J='EnergyMgt-1.0.0.jar';        W=3}
  # 统一中台板块：数据/文件/消息/IOT/BI/大屏 6 个微服务（前端栏目“统一中台”）
  @{N='DataMgt';          P=8200; D='DataMgt';          J='DataMgt-1.0.0.jar';          W=4}
  @{N='FileMgt';          P=8201; D='FileMgt';          J='FileMgt-1.0.0.jar';          W=4}
  @{N='MessageMgt';       P=8202; D='MessageMgt';       J='MessageMgt-1.0.0.jar';       W=4}
  @{N='IotMgt';           P=8203; D='IotMgt';           J='IotMgt-1.0.0.jar';           W=4}
  @{N='BiMgt';            P=8204; D='BiMgt';            J='BiMgt-1.0.0.jar';            W=4}
  @{N='ScreenMgt';        P=8205; D='ScreenMgt';        J='ScreenMgt-1.0.0.jar';        W=4}
  @{N='FlowEngineMgt';    P=8206; D='FlowEngineMgt';    J='FlowEngineMgt-1.0.0.jar';    W=4}
  # Wave 4: HRM Modules
  @{N='RecruitmentMgt';   P=8086; D='RecruitmentMgt';                J='RecruitmentMgt-1.0.0.jar';          W=4}
  @{N='EmployeeMgt';      P=8083; D='EmployeeMgt';                   J='EmployeeMgt-1.0.0.jar';            W=4}
  @{N='PerfMgt';           P=8085; D='PerfMgt';                        J='PerfMgt-1.0.0.jar';                 W=4}
  @{N='AttendanceMgt';    P=8084; D='AttendanceMgt';                 J='AttendanceMgt-1.0.0.jar';           W=4}
  # 考勤假期拆分：假期管理 8190
  @{N='HolidayMgt';        P=8190; D='HolidayMgt';                    J='HolidayMgt-1.0.0.jar';              W=4}
  @{N='QualificationMgt'; P=8090; D='QualificationMgt';              J='QualificationMgt-1.0.0.jar';        W=4}
  @{N='TrainingMgt';       P=8087; D='TrainingMgt';                    J='TrainingMgt-1.0.0.jar';             W=4}
  # 培训学考拆分：在线学习 8194 / 在线考试 8195
  @{N='OnlineLearnMgt';    P=8194; D='OnlineLearnMgt';                 J='OnlineLearnMgt-1.0.0.jar';          W=4}
  @{N='OnlineExamMgt';     P=8195; D='OnlineExamMgt';                  J='OnlineExamMgt-1.0.0.jar';           W=4}
  @{N='TalentMgt';         P=8094; D='TalentMgt';                      J='TalentMgt-1.0.0.jar';               W=4}
  # 人才发展拆分：人才评审 8191 / 继任者计划 8192 / 人才培养工程 8193
  @{N='TalentReviewMgt';   P=8191; D='TalentReviewMgt';                J='TalentReviewMgt-1.0.0.jar';         W=4}
  @{N='SuccessionPlanMgt'; P=8192; D='SuccessionPlanMgt';              J='SuccessionPlanMgt-1.0.0.jar';       W=4}
  @{N='TalentDevMgt';      P=8193; D='TalentDevMgt';                   J='TalentDevMgt-1.0.0.jar';            W=4}
  @{N='SkillCertMgt';      P=8093; D='SkillCertMgt';                  J='SkillCertMgt-1.0.0.jar';            W=4}
  @{N='SalaryMgt';         P=8092; D='SalaryMgt';                      J='SalaryMgt-1.0.0.jar';               W=4}
  @{N='HRPlanningMgt';     P=8095; D='HRPlanningMgt';                  J='HRPlanningMgt-1.0.0.jar';           W=4}
)

$databases = @(
  'iam_db','org_mgt','sys_mgt','sys_monitor','process_mgt',
  'project_mgt','improve_mgt','module_config',
  # 纪检监督库
  'discipline_mgt',
  # 战略管理 5 库
  'strategy_blm','strategy_dste','strategy_sixs','strategy_budget','strategy_workplan',
  # EEHS 4 库
  'env_mgt','safety_mgt','occ_health_mgt','energy_mgt',
  # 统一中台 6 库
  'data_mgt','file_mgt','message_mgt','iot_mgt','bi_mgt','screen_mgt','flow_engine',
  'recruitment_mgt','employee_mgt','training_mgt','attendance_mgt',
  # 拆分新增库：假期/在线学习/在线考试/人才评审/继任/培养工程
  'holiday_mgt','online_learn_mgt','online_exam_mgt',
  'talent_review_mgt','succession_plan_mgt','talent_dev_mgt',
  'qualification_mgt','skill_cert_mgt','salary_mgt','perf_mgt',
  'talent_mgt','hr_planning_mgt'
)

# Nacos-off flags (disabled by default for reliability; same approach as previous startup scripts)
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

# JVM memory profiles
$memInfra = @('-Xmx256m','-Xms64m','-XX:+UseSerialGC','-XX:MaxMetaspaceSize=128m','-Dfile.encoding=UTF-8','-Dsun.jnu.encoding=UTF-8')
$memCore  = @('-Xmx192m','-Xms48m','-XX:+UseSerialGC','-XX:MaxMetaspaceSize=128m','-XX:ReservedCodeCacheSize=20m','-Dfile.encoding=UTF-8','-Dsun.jnu.encoding=UTF-8')
$memBiz   = @('-Xmx160m','-Xms48m','-XX:+UseSerialGC','-XX:MaxMetaspaceSize=96m','-XX:ReservedCodeCacheSize=16m','-Dfile.encoding=UTF-8','-Dsun.jnu.encoding=UTF-8')

# ============================== HELPER FUNCTIONS ==============================

function Write-Step($msg) {
  Write-Output "`n========== $msg =========="
}

function Test-PortUp($port) {
  return [bool](Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue)
}

function Wait-Ports($ports, $timeoutSec) {
  $deadline = (Get-Date).AddSeconds($timeoutSec)
  while ((Get-Date) -lt $deadline) {
    $up = @($ports | Where-Object { Test-PortUp $_ })
    if ($up.Count -ge $ports.Count) { return $true }
    Start-Sleep -Seconds 4
  }
  return $false
}

function Find-MavenHome {
  $candidates = @(
    "$env:LOCALAPPDATA\apache-maven-$MAVEN_VERSION",
    'C:\apache-maven',
    'C:\maven',
    'C:\Program Files\apache-maven',
    'C:\dev\apache-maven',
    'D:\apache-maven',
    'D:\dev\apache-maven'
  )
  foreach ($c in $candidates) {
    if (Test-Path (Join-Path $c 'bin\mvn.cmd')) { return $c }
  }
  $mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
  if ($mvnCmd) { return (Split-Path (Split-Path $mvnCmd.Source -Parent) -Parent) }
  return $null
}

function Download-Maven {
  $zipPath = "$env:TEMP\apache-maven-$MAVEN_VERSION-bin.zip"
  $extractBase = "$env:LOCALAPPDATA"

  $downloaded = $false
  foreach ($url in $MAVEN_URLS) {
    Write-Output "  Downloading Apache Maven $MAVEN_VERSION from $url ..."
    try {
      Invoke-WebRequest -Uri $url -OutFile $zipPath -UseBasicParsing -TimeoutSec 180
      $downloaded = $true
      break
    } catch {
      Write-Output "  Failed: $_"
    }
  }
  if (-not $downloaded) {
    Write-Output "  FATAL: All Maven download URLs failed."
    Write-Output "  Please install Maven manually or use -SkipBuild with pre-built JARs."
    return $null
  }

  Write-Output "  Extracting to $extractBase ..."
  Expand-Archive -Path $zipPath -DestinationPath $extractBase -Force
  Remove-Item $zipPath -Force -ErrorAction SilentlyContinue

  $mavenHome = "$extractBase\apache-maven-$MAVEN_VERSION"
  if (Test-Path (Join-Path $mavenHome 'bin\mvn.cmd')) { return $mavenHome }

  Write-Output "  FATAL: Maven extraction failed."
  return $null
}

function Start-Service-JVM($svc, $memArgs) {
  $jarPath = Join-Path $PROJECT_ROOT "$($svc.D)\target\$($svc.J)"

  if (-not (Test-Path $jarPath)) {
    Write-Output ("  SKIP {0,-20}: JAR not found ({1})" -f $svc.N, $jarPath)
    return $null
  }
  if (Test-PortUp $svc.P) {
    Write-Output ("  ALREADY-UP {0,-20} :{1}" -f $svc.N, $svc.P)
    return $null
  }

  $argList = [System.Collections.Generic.List[string]]::new()
  foreach ($m in $memArgs) { [void]$argList.Add($m) }
  [void]$argList.Add('-jar')
  [void]$argList.Add($jarPath)
  [void]$argList.Add("--server.port=$($svc.P)")
  foreach ($f in $nacosOff) { [void]$argList.Add($f) }

  $proc = Start-Process -FilePath $JAVA -ArgumentList $argList `
    -RedirectStandardOutput (Join-Path $LOG_DIR "$($svc.N)-stdout.log") `
    -RedirectStandardError  (Join-Path $LOG_DIR "$($svc.N)-stderr.log") `
    -WindowStyle Hidden -PassThru

  Write-Output ("  START  {0,-20} :{1}  PID={2}" -f $svc.N, $svc.P, $proc.Id)
  return $proc
}

# ============================== MAIN ==============================

Write-Step 'EMgtv2 Platform - One-Click Startup'
Write-Output "  Project root : $PROJECT_ROOT"
Write-Output "  Java         : $JAVA"
Write-Output "  MySQL        : $MYSQL"
Write-Output "  Log dir      : $LOG_DIR"

# ---------- Phase 0: Prerequisites ----------
Write-Step 'Phase 0: Prerequisites'

if (-not (Test-Path $JAVA)) {
  Write-Output 'FATAL: Java 17 not found at expected path.'
  exit 1
}
Write-Output "Java: $((& $JAVA -version 2>&1 | Select-Object -First 1))"

if (-not (Test-Path $NPM)) {
  Write-Output 'WARN: npm not found - frontend will be skipped.'
} else {
  Write-Output "Node: $((& $NPM -v 2>&1))"
}

$mysqlSvc = Get-Service -Name 'MySQL267' -ErrorAction SilentlyContinue
if (-not $mysqlSvc) {
  $mysqld = Get-Process mysqld -ErrorAction SilentlyContinue
  if (-not $mysqld) {
    Write-Output 'FATAL: MySQL is not running. Start MySQL service MySQL267 first.'
    exit 1
  }
  Write-Output 'MySQL: Running (process detected)'
} else {
  if ($mysqlSvc.Status -ne 'Running') {
    Write-Output 'Starting MySQL service...'
    Start-Service -Name 'MySQL267'
    Start-Sleep -Seconds 5
  }
  Write-Output "MySQL: $($mysqlSvc.Status)"
}

$os = Get-CimInstance Win32_OperatingSystem
Write-Output ("Free RAM: {0:N2} GB" -f ($os.FreePhysicalMemory / 1MB))

# ---------- Phase 1: Maven ----------
Write-Step 'Phase 1: Maven Setup'

$mvnHome = Find-MavenHome
if (-not $mvnHome) {
  if ($SkipMavenDownload) {
    Write-Output 'FATAL: Maven not found and -SkipMavenDownload specified.'
    exit 1
  }
  $mvnHome = Download-Maven
  if (-not $mvnHome) { exit 1 }
}
$mvnExe = Join-Path $mvnHome 'bin\mvn.cmd'
Write-Output "Maven home: $mvnHome"
Write-Output "Maven: $((& $mvnExe -v 2>&1 | Select-Object -First 1))"

# ---------- Phase 2: Databases ----------
Write-Step 'Phase 2: Create Databases'

foreach ($db in $databases) {
  & $MYSQL -u root "-p$MYSQL_ROOT_PW" -e "CREATE DATABASE IF NOT EXISTS \`$db\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;" 2>&1 | Out-Null
  Write-Output "  $db - OK"
}

# ---------- Phase 3: Build ----------
if (-not $SkipBuild) {
  Write-Step 'Phase 3: Build All Modules (mvn package -DskipTests)'

  # 3a: iam-sdk (install to .m2 so children can resolve parent)
  Write-Output 'Building iam-sdk (mvn install)...'
  & $mvnExe -f "$PROJECT_ROOT\iam-sdk\pom.xml" install -DskipTests -q 2>&1 | Select-Object -Last 3
  Write-Output '  iam-sdk: installed'

  # 3b: all service modules
  foreach ($svc in $svcDefs) {
    $modPath = Join-Path $PROJECT_ROOT $svc.D
    $pomPath = Join-Path $modPath 'pom.xml'
    if (-not (Test-Path $pomPath)) {
      Write-Output "  SKIP $($svc.N): pom.xml not found"
      continue
    }
    Write-Output "Building $($svc.N)..."
    & $mvnExe -f $pomPath package -DskipTests -q 2>&1 | Select-Object -Last 3

    $jarPath = Join-Path $modPath "target\$($svc.J)"
    if (Test-Path $jarPath) {
      $sz = [math]::Round((Get-Item $jarPath).Length / 1MB, 1)
      Write-Output "  $($svc.N): built OK (${sz} MB)"
    } else {
      Write-Output "  $($svc.N): BUILD FAILED - JAR not found"
    }
  }
} else {
  Write-Step 'Phase 3: Build SKIPPED (-SkipBuild)'
}

if ($BuildOnly) {
  Write-Step 'Build complete. (-BuildOnly specified, exiting.)'
  exit 0
}

# Prepare log directory
if (-not (Test-Path $LOG_DIR)) { New-Item -ItemType Directory -Path $LOG_DIR -Force | Out-Null }

# ---------- Phase 4: Wave 1 - Infrastructure ----------
Write-Step 'Wave 1: Infrastructure (ServiceRegistry, ConfigServer, APIGateway)'

$allPids = @()
$wave1 = $svcDefs | Where-Object { $_.W -eq 1 }

foreach ($svc in $wave1) {
  $p = Start-Service-JVM -svc $svc -memArgs $memInfra
  if ($p) { $allPids += $p }
  Start-Sleep -Seconds 2
}

if ($wave1.Count -gt 0) {
  $ok = Wait-Ports -ports @($wave1.P) -timeoutSec 120
  $up1 = @($wave1.P | Where-Object { Test-PortUp $_ })
  Write-Output "Wave 1 result: $($up1.Count)/$($wave1.Count) services up"
}

# ---------- Phase 5: Wave 2 - Core ----------
Write-Step 'Wave 2: Core Services (IAM, OrgMgt, ModuleConfig, SysMgt, SysMonitor)'

$wave2 = $svcDefs | Where-Object { $_.W -eq 2 }

foreach ($svc in $wave2) {
  $p = Start-Service-JVM -svc $svc -memArgs $memCore
  if ($p) { $allPids += $p }
  Start-Sleep -Milliseconds 1500
}

if ($wave2.Count -gt 0) {
  $ok = Wait-Ports -ports @($wave2.P) -timeoutSec 180
  $up2 = @($wave2.P | Where-Object { Test-PortUp $_ })
  Write-Output "Wave 2 result: $($up2.Count)/$($wave2.Count) services up"
}

# ---------- Phase 6: Wave 3 - Business ----------
Write-Step 'Wave 3: Business Services'

$wave3 = $svcDefs | Where-Object { $_.W -eq 3 }

foreach ($svc in $wave3) {
  $p = Start-Service-JVM -svc $svc -memArgs $memBiz
  if ($p) { $allPids += $p }
  Start-Sleep -Seconds 2
}

if ($wave3.Count -gt 0) {
  $ok = Wait-Ports -ports @($wave3.P) -timeoutSec 180
  $up3 = @($wave3.P | Where-Object { Test-PortUp $_ })
  Write-Output "Wave 3 result: $($up3.Count)/$($wave3.Count) services up"
}

# ---------- Phase 6b: Wave 4 - HRM Modules (two gated sub-waves to avoid memory spikes) ----------
Write-Step 'Wave 4: HRM Modules (Recruitment/Employee/Perf/Attendance/Qualification/Training/Talent/SkillCert/Salary/HRPlanning)'

$wave4 = $svcDefs | Where-Object { $_.W -eq 4 }
# Sub-wave 4a: first 5 modules
$wave4a = $wave4 | Select-Object -First 5
foreach ($svc in $wave4a) {
  $p = Start-Service-JVM -svc $svc -memArgs $memBiz
  if ($p) { $allPids += $p }
  Start-Sleep -Seconds 2
}
if ($wave4a.Count -gt 0) {
  $ok = Wait-Ports -ports @($wave4a.P) -timeoutSec 180
  $up4a = @($wave4a.P | Where-Object { Test-PortUp $_ })
  Write-Output "Wave 4a result: $($up4a.Count)/$($wave4a.Count) services up"
}
# Sub-wave 4b: remaining modules
$wave4b = $wave4 | Select-Object -Skip 5
foreach ($svc in $wave4b) {
  $p = Start-Service-JVM -svc $svc -memArgs $memBiz
  if ($p) { $allPids += $p }
  Start-Sleep -Seconds 2
}
if ($wave4b.Count -gt 0) {
  $ok = Wait-Ports -ports @($wave4b.P) -timeoutSec 180
  $up4b = @($wave4b.P | Where-Object { Test-PortUp $_ })
  Write-Output "Wave 4b result: $($up4b.Count)/$($wave4b.Count) services up"
}

# ---------- Phase 7: Frontend ----------
if (-not $SkipFrontend) {
  Write-Step 'Phase 7: Frontend (Vite)'

  if (Test-PortUp $VITE_PORT) {
    Write-Output "  Vite already UP on :$VITE_PORT"
  } else {
    if (-not (Test-Path "$PROJECT_ROOT\frontend\node_modules\vite")) {
      Write-Output '  Installing npm dependencies...'
      & $NPM install --prefix "$PROJECT_ROOT\frontend" 2>&1 | Select-Object -Last 3
    }
    $proc = Start-Process -FilePath 'cmd.exe' `
      -ArgumentList '/c', "`"$NPM`" run dev > `"$LOG_DIR\vite.log`" 2>&1" `
      -WorkingDirectory "$PROJECT_ROOT\frontend" `
      -WindowStyle Hidden -PassThru
    $rv = Wait-Ports -ports @($VITE_PORT) -timeoutSec 60
    Write-Output "  Vite: $(if($rv){'UP'}else{'DOWN - check vite.log'})"
  }
}

# ---------- Phase 8: Health Check ----------
Write-Step 'Phase 8: Health Check Matrix'
Write-Output '  (HTTP 200/400/401/404/405 = service alive, path may differ)'

$checks = @(
  @(8848,'ServiceRegistry','/nacos','GET'),
  @(8849,'ConfigServer',   '/nacos','GET'),
  @(8080,'APIGateway',     '/actuator/health','GET'),
  @(8081,'IAM',            '/api/auth/login','POST'),
  @(8082,'OrgMgt',         '/api/org-unit/tree','GET'),
  @(9000,'ModuleConfig',   '/api/module-config/enabled','GET'),
  @(8111,'SysMgt',         '/api/user-mgt','GET'),
  @(8112,'SysMonitor',     '/api/sys-mon/server','GET'),
  @(8089,'ProcessMgt',     '/api/process/template/list','GET'),
  @(8091,'ProjectMgt',     '/api/project/pm/list','GET'),
  @(8142,'ImproveMgt',     '/api/improve/proposal/list','GET'),
  @(8196,'DisciplineMgt',  '/api/discipline/case/list','GET'),
  @(8101,'StrategyBlmMgt',       '/api/strategy/ping','GET'),
  @(8102,'StrategyDsteMgt',      '/api/strategy/dste/kpis','GET'),
  @(8103,'StrategySixsMgt',      '/api/strategy/sixs/s1','GET'),
  @(8104,'StrategyBudgetMgt',    '/api/strategy/budget/subjects','GET'),
  @(8105,'StrategyWorkplanMgt',  '/api/strategy/workplan/templates','GET'),
  @(8106,'EnvironMgt',      '/api/eehs/env/ping','GET'),
  @(8107,'SafetyMgt',       '/api/eehs/safety/ping','GET'),
  @(8108,'OccHealthMgt',    '/api/eehs/health/ping','GET'),
  @(8109,'EnergyMgt',       '/api/eehs/energy/ping','GET'),
  # Wave 4: HRM Modules
  @(8086,'RecruitmentMgt',  '/api/recruitment/requirement/list','GET'),
  @(8083,'EmployeeMgt',     '/api/employee','GET'),
  @(8085,'PerfMgt',          '/api/perf/evaluation/list','GET'),
  @(8084,'AttendanceMgt',   '/api/attendance/daily/list','GET'),
  @(8090,'QualificationMgt','/api/qualification/list','GET'),
  @(8087,'TrainingMgt',      '/api/training/course/list','GET'),
  @(8094,'TalentMgt',        '/api/talent/assessment/list','GET'),
  @(8093,'SkillCertMgt',     '/api/skill-cert/skill/list','GET'),
  @(8092,'SalaryMgt',        '/api/salary/structure/list','GET'),
  @(8095,'HRPlanningMgt',   '/api/hr-plan/list','GET')
)

$alive = 0; $dead = @()
foreach ($c in $checks) {
  $port=$c[0]; $name=$c[1]; $path=$c[2]; $m=$c[3]
  $code = 'ERR'
  try {
    $hdr = @{ 'X-User-Id' = 'admin' }
    if ($m -eq 'POST') {
      $r = Invoke-WebRequest -Uri "http://localhost:$port$path" -Method Post -Body '{}' -ContentType 'application/json' -TimeoutSec 8 -UseBasicParsing
    } else {
      $r = Invoke-WebRequest -Uri "http://localhost:$port$path" -Headers $hdr -TimeoutSec 8 -UseBasicParsing
    }
    $code = [int]$r.StatusCode
  } catch {
    if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode.value__ }
  }
  $ok = ($code -in 200,400,401,404,405)
  if ($ok) { $alive++ } else { $dead += "$port $name($code)" }
  Write-Output ("  {0,-6} {1,-20} {2,-5} {3}" -f $port,$name,$code,$(if($ok){'ALIVE'}else{'DEAD'}))
}

# ---------- Summary ----------
Write-Step 'SUMMARY'

$os2 = Get-CimInstance Win32_OperatingSystem
$freeRam2 = [math]::Round($os2.FreePhysicalMemory / 1MB, 2)
$javaCount = (Get-Process java -ErrorAction SilentlyContinue).Count
$viteUp = Test-PortUp $VITE_PORT

Write-Output "Services responding : $alive / $($checks.Count)"
if ($dead.Count) { Write-Output "DEAD: $($dead -join '; ')" }
Write-Output "Free RAM            : ${freeRam2} GB"
Write-Output "Java processes      : $javaCount"
Write-Output "Vite :${VITE_PORT}         : $(if($viteUp){'UP'}else{'DOWN'})"
Write-Output "Frontend URL        : http://localhost:$VITE_PORT (admin / admin123)"
Write-Output "Logs                : $LOG_DIR"
Write-Output ""
Write-Output "Holding host - press Ctrl+C to stop all services."

# Hold the host alive so child JVMs are not reaped
if ($allPids.Count -gt 0) {
  Wait-Process -Id ($allPids.Id) -ErrorAction SilentlyContinue
}
