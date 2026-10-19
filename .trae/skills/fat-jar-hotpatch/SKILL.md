---
name: "fat-jar-hotpatch"
description: "Hot-patches Spring Boot fat jars without rebuilding the entire project. Invoke when fixing backend bugs in running microservices (OrgMgt/PerfMgt/SalaryMgt/Recruitment/Training/WorkPlan/Strategy/etc.) and a full mvn package is too slow or unavailable."
---

# Fat Jar Hot-Patch (微服务热补丁)

Patches compiled `.class` files directly into running Spring Boot fat jars — no `mvn package` needed. Used 7+ times in this workspace for emergency bug fixes across OrgMgt, PerfMgt, SalaryMgt, RecruitmentMgt, TrainingMgt, WorkPlanMgt, StrategyPlanning, TalentDevepPrj, HRArchive, etc.

## Prerequisites

- JDK 17 at `C:\Program Files\Java\jdk-17.0.18\` (`$javac` = `...\bin\javac.exe`, `$java` = `...\bin\java.exe`)
- Service fat jars located under each project's `target/` directory (e.g., `d:\EMgt\eHRM\SalaryMgt\target\SalaryMgt-1.0.0.jar`)
- External dependencies at `d:\EMgt\server\ext` (shared libs not in the fat jar)
- Patch staging dir: `d:\EMgtv2\logs\pentest\ext` (writable scratch space)
- PowerShell (commands below use PowerShell syntax)

## Workflow (7 Steps)

### Step 1: Fix the Source

Edit the `.java` source file in the project's `src/main/java/...` directory. Common fixes in this workspace:
- Add `deleted=0` / `version=1` defaults for MyBatis-Plus logical deletion
- Add optimistic lock interceptor config
- Fix `/list` → root GET path conflicts
- Add missing `@RequestBody(required=false)` for approve/reject endpoints
- Fix field name mismatches between frontend and entity

### Step 2: Unpack Fat Jar (extract classpath for compilation)

```powershell
$jarPath = 'd:\EMgt\eHRM\<Project>\target\<Project>-1.0.0.jar'
$unpackDir = "d:\EMgtv2\logs\pentest\unpack-<Project>"
New-Item -ItemType Directory -Path $unpackDir -Force
Push-Location $unpackDir
& 'C:\Program Files\Java\jdk-17.0.18\bin\jar.exe' xf $jarPath
Pop-Location
```

This extracts `BOOT-INF/lib/*.jar` (dependency jars) and `BOOT-INF/classes/` (existing classes) — needed as classpath for `javac`.

### Step 3: Compile Patched Class

**CRITICAL**: Always use `-parameters` flag — Spring Boot controllers need parameter names for `@RequestParam`, `@PathVariable`, etc. Without it, runtime 400 errors occur.

```powershell
$javac = 'C:\Program Files\Java\jdk-17.0.18\bin\javac.exe'
$srcFile = 'd:\EMgt\eHRM\<Project>\src\main\java\com\example\<module>\controller\SomeController.java'

# Build classpath from unpacked BOOT-INF/lib + BOOT-INF/classes + server/ext
$libs = (Get-ChildItem "$unpackDir\BOOT-INF\lib\*.jar" -ErrorAction SilentlyContinue).FullName -join ';'
$extLibs = (Get-ChildItem 'd:\EMgt\server\ext\*.jar' -ErrorAction SilentlyContinue).FullName -join ';'
$cp = "$unpackDir\BOOT-INF\classes;$libs;$extLibs"

& $javac -parameters -cp $cp -d "$unpackDir\BOOT-INF\classes" $srcFile
```

If compiling multiple files, pass all `.java` paths. If there are compile errors, fix source and retry.

### Step 4: Stop the Running Service

```powershell
# Find PID by port
$port = 8092  # change per service
$pid = (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue).OwningProcess
if ($pid) { Stop-Process -Id $pid -Force }
```

### Step 5: Update Fat Jar (jar uf)

**CRITICAL**: Spring Boot only loads classes under `BOOT-INF/classes/`. Entry names inside the jar MUST start with `BOOT-INF/classes/`. Run `jar uf` from the **unpack root** (not from `BOOT-INF\classes`) and pass paths prefixed with `BOOT-INF\classes\`:

```powershell
Push-Location $unpackDir   # unpack ROOT, not BOOT-INF\classes
$classRelPaths = @('BOOT-INF\classes\com\example\<module>\controller\SomeController.class')
& 'C:\Program Files\Java\jdk-17.0.18\bin\jar.exe' uf $jarPath @classRelPaths
Pop-Location
# Verify entry placement:
& 'C:\Program Files\Java\jdk-17.0.18\bin\jar.exe' tf $jarPath | Select-String 'SomeController'
# Expect: BOOT-INF/classes/com/example/... (NOT com/example/... at jar root)
```

If entries accidentally land at jar root (running jar uf from inside BOOT-INF\classes), remove them with ZipArchive then re-add correctly:

```powershell
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::Open($jarPath, 'Update')
($zip.Entries | Where-Object { $_.FullName -match '^com/example/.*SomeClass' }).Delete()
$zip.Dispose()
```

### Step 6: Restart Service

```powershell
$java = 'C:\Program Files\Java\jdk-17.0.18\bin\java.exe'
$logs = 'd:\EMgtv2\logs\pentest'
$p = Start-Process -FilePath $java -ArgumentList @(
    '-Xmx320m', '-Xss512k', '-XX:+UseSerialGC',
    '-cp', $jarPath,
    '-Dloader.path=d:\EMgt\server\ext,d:\EMgtv2\logs\pentest\ext',
    '-Dspring.sql.init.mode=never',
    'org.springframework.boot.loader.launch.PropertiesLauncher',
    "--server.port=$port"
) -RedirectStandardOutput "$logs\<Project>-stdout.log" `
  -RedirectStandardError "$logs\<Project>-stderr.log" `
  -NoNewWindow -PassThru
Write-Host "Started PID=$($p.Id) on port $port"
```

### Step 7: Verify

```powershell
# Wait ~10s for startup, then test endpoint
Start-Sleep -Seconds 10
curl.exe -s -w "`nHTTP:%{http_code}" "http://localhost:$port/api/<endpoint>"
```

Check stderr log if the service doesn't start:
```powershell
Get-Content "$logs\<Project>-stderr.log" -Tail 30
```

## Service Port & Jar Reference

| Service | Port | Jar Path |
|---------|------|-----------|
| IAM | 8081 | `d:\EMgt\eHRM\IAM\target\*.jar` |
| OrgMgt | 8082 | `d:\EMgt\OrgMgt\target\*.jar` |
| PerfMgt | 8083 | `d:\EMgt\eHRM\PerfMgt\target\*.jar` |
| QualRankMgt | 8084 | `d:\EMgt\eHRM\QualRankMgt\target\*.jar` |
| TalentDevepPrj | 8095 | `d:\EMgt\eHRM\Talent\TalentDevepPrj\target\*.jar` |
| TalentQualMgt | 8096 | `d:\EMgt\eHRM\Talent\TalentQualMgt\target\*.jar` |
| RecruitmentMgt | 8097 | `d:\EMgt\eHRM\RecruitmentMgt\target\*.jar` |
| SalaryMgt | 8092 | `d:\EMgt\eHRM\SalaryMgt\target\*.jar` |
| TrainingMgt | 8086 | `d:\EMgt\eHRM\TrainingMgt\target\*.jar` |
| HRArchive | 8093 | `d:\EMgt\eHRM\HRArchive\target\*.jar` |
| WorkPlanMgt | 8103 | `d:\EMgt\StrategyMgt\WorkPlanMgt\target\*.jar` |
| StrategyPlanning | 8101 | `d:\EMgt\StrategyMgt\StrategyPlanning\target\*.jar` |
| ModuleConfigCenter | 9000 | `d:\EMgt\module-config-center\target\*.jar` |

## Common Pitfalls

1. **Missing `-parameters`**: `javac` without `-parameters` strips method parameter names → Spring `@RequestParam("employeeId")` fails → 400 errors. Always include it.
2. **Wrong classpath**: If `javac` can't find symbols, ensure `BOOT-INF/lib/*.jar` + `BOOT-INF/classes` + `server/ext/*.jar` are all on the classpath.
3. **`jar uf` path**: The class file must be at the same relative path inside the jar. Push-Location to `BOOT-INF/classes` before running `jar uf`.
4. **Service won't start**: Check `-Dloader.path` includes both `server/ext` and `logs/pentest/ext`. Use PropertiesLauncher (not JarLauncher).
5. **Port already in use**: Ensure old process is fully killed before restart. Use `Get-NetTCPConnection -LocalPort` to verify.
6. **JPA ddl-auto**: For JPA services (like ModuleConfigCenter), the service auto-creates tables on startup. For MyBatis-Plus services, run DDL manually.
7. **Logic deletion**: New records must have `deleted=0`. Entity inserts must include this or the record is invisible after insert.
8. **Optimistic lock**: MyBatis-Plus services need `MybatisPlusConfig` with `OptimisticLockerInnerInterceptor` or updates fail with `MP_OPTLOCK_VERSION_ORIGINAL not found`.
