# HRM 平台热补丁 & 批量启动 Runbook（2026-09-04 沉淀）

> 适用环境：Windows + JDK17 + MySQL 26.7 + 30 个 Spring Boot fat-jar 微服务 + Vite5 前端（5174）。
> 本轮成果：P2（存在未启动）22/22、P3（新建后端）8/8，合计 **30/30 LISTEN**，Vite 代理验证通过。

---

## 一、资源文件热补丁（yml / sql）—— 不重打包 mvn package

fat-jar 内资源位于 `BOOT-INF/classes/`。修改 src 后用 staging 目录 + `jar uf` 回写，**禁止从 BOOT-INF\classes 目录内执行**。

### 1.1 标准 SOP

```powershell
$jarU = 'C:\Program Files\Java\jdk-17.0.18\bin\jar.exe'
$stage = 'd:\EMgtv2\logs\pentest\<svc>-cfg'
New-Item -ItemType Directory -Force -Path $stage | Out-Null
# 目标相对路径必须是 BOOT-INF/classes/... （jar entry 用正斜杠）
$dst = Join-Path $stage 'BOOT-INF\classes\application.yml'
New-Item -ItemType Directory -Force -Path (Split-Path $dst) | Out-Null
Copy-Item '<src>\main\resources\application.yml' $dst -Force
Push-Location $stage
& $jarU uf '<svc>-1.0.0.jar' 'BOOT-INF/classes/application.yml'
Pop-Location
```

### 1.2 回写后必须验证（防止 yml 结构断裂）

```powershell
$chk = 'd:\EMgtv2\logs\pentest\chk-<svc>'
New-Item -ItemType Directory -Force -Path $chk | Out-Null
Push-Location $chk; & $jarU xf '<svc>-1.0.0.jar' 'BOOT-INF/classes/application.yml'; Pop-Location
Get-Content (Join-Path $chk 'BOOT-INF\classes\application.yml') | Select-Object -First 25
```

教训：曾出现 `jar uf` 覆盖后 jar 内 yml 的 `spring:` 块缩进断裂（`application.name/profiles.active` 被吞），**必须解包重读确认层次完整**。

### 1.3 schema.sql 热补丁四类坑（MySQL 26.7）

| 坑 | 现象 | 修法 |
|---|---|---|
| UTF-8 BOM（0xEFBBBF） | 启动 SQL 解析失败 | 用无 BOM UTF-8 重写整个文件 |
| `CREATE INDEX IF NOT EXISTS` | 26.7 语法不支持 | 整段删除（不要用 info_schema PREPARE 兜底） |
| `INSERT INTO` 重复键 | 重启报错 | 全部改 `INSERT IGNORE` |
| 同一 CREATE TABLE 内重复列（如 6SDSTE 26 张表各有 2 个 `fill TIMESTAMP`） | Duplicate column | 逐行扫描 CREATE TABLE 块，同名列只保留首条 |

注意 schema 位置不统一：多数在 `BOOT-INF/classes/schema.sql`，SuccessPlanMgt / WorkPlanMgt / LearnToExam 在 `BOOT-INF/classes/db/schema.sql`（以 application.yml 的 `spring.sql.init.schema-locations` 为准）。

---

## 二、spring.autoconfigure.exclude 合法性（8151/8152 根因）

`spring.autoconfigure.exclude` **只接受 `@AutoConfiguration` 类**，混入普通组件会抛：
`IllegalStateException: classes could not be excluded because they are not auto-configuration classes`。

- ❌ 禁止放入：`com.alibaba.cloud.nacos.refresh.NacosContextRefresher`、`...NacosConfigBootstrapConfiguration`、`...NacosDiscoveryBootstrapConfiguration`、`...client.NacosPropertySourceLocator`
- ✅ 合法保留 5 条：
  - `com.alibaba.cloud.nacos.NacosConfigAutoConfiguration`
  - `com.alibaba.cloud.nacos.NacosDiscoveryAutoConfiguration`
  - `com.alibaba.cloud.nacos.NacosServiceAutoConfiguration`
  - `com.alibaba.cloud.nacos.endpoint.NacosConfigEndpointAutoConfiguration`
  - `org.springframework.cloud.autoconfigure.CompatibilityNoticesAutoConfiguration`

Nacos 的彻底关闭不靠 exclude，靠启动参数（见第四节 5 条 nacos-off）+ bootstrap.yml 的 `spring.config.import: "optional:nacos:"`。

---

## 三、.class 热补丁要点（补充 fat-jar-hotpatch skill）

- `javac` 必须带 `-parameters`（否则 `@RequestParam/@PathVariable` 400）。
- classpath = 解包目录 `BOOT-INF/classes` + `BOOT-INF/lib/*.jar` + `d:\EMgt\server\ext\*.jar`。
- `jar uf` 从解包 root 执行，entry 前缀 `BOOT-INF/classes/...`；误放到 jar 根的条目用 `System.IO.Compression.ZipFile` 删除后重加。
- **Bean 条件装配坑**：`@ConditionalOnBean(FlowEngine.class)` 过滤掉 Sdk 适配器后，`@ConditionalOnMissingBean(name="sdkFlowEngineAdapter")` 因 BeanDefinition 注册顺序仍不生效 → 最终模式：
  - Sdk 适配器：`@Component @ConditionalOnBean(FlowEngine.class) @Primary`
  - Local 适配器：`@Component @Primary`（无条件注册；无 FlowEngine 时 Sdk 整个跳过，不会双 primary）

---

## 四、30 JVM 批量启动 SOP（8GB RAM 极限）

### 4.1 JVM 参数（稳定版，防 native Chunk::new malloc 崩溃）

```
-Xmx160~224m -Xms40~64m -Xss256k
-XX:+UseSerialGC -XX:MaxMetaspaceSize=112~128m -XX:ReservedCodeCacheSize=16~24m
-XX:MaxRAM=208~280m -XX:-UseCompressedOops
-XX:CICompilerCount=2 -XX:TieredStopAtLevel=1
-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8
```

- 核心三剑客：`TieredStopAtLevel=1`（禁 C2 编译风暴）、`-UseCompressedOops`（避免压缩指针卡 <4GB 虚拟区）、`UseSerialGC`（降元开销）。
- 分批：10+10+10（或 10+10+5+5），每批间隔 600–900ms 启动，每批后 Sleep 75–150s 等 LISTEN。
- 内存递减：前波 Xmx224m，末波 Xmx160m。

### 4.2 nacos-off 5 参数（含 nacos 依赖的模块必带）

```
--spring.cloud.nacos.config.enabled=false
--spring.cloud.nacos.discovery.enabled=false
--spring.cloud.nacos.config.import-check.enabled=false
--spring.cloud.bootstrap.enabled=false
--spring.cloud.nacos.discovery.register-enabled=false
```

### 4.3 Start-Process 引号大坑

`ArgumentList` 必须用 `System.Collections.Generic.List[string]` + 裸字符串逐个 Add，**不要自己包 `"path"`**，否则 java 报 `Unable to access jarfile  D:\...`（路径前导空格）。

### 4.4 进程清理

`Get-CimInstance Win32_Process -Filter "name='java.exe'"` → 排除命令行含 `jre\21.0.11-win32`（VSCode Java LSP）→ 其余 kill。JDK 用绝对路径 `C:\Program Files\Java\jdk-17.0.18\bin\java.exe`，避免 `javapath` 伪 java 产生重复进程。

### 4.5 MySQL 会话级调优

```sql
SET GLOBAL max_connections=800;
SET GLOBAL max_connect_errors=10000;
```

---

## 五、健康检查 & Vite 代理验证 SOP

### 5.1 端口硬检查（hashtable key 必须转 int）

```powershell
$l=@{}
Get-NetTCPConnection -State Listen -EA SilentlyContinue |
  ForEach-Object { if($_.LocalPort -ge 8080 -and $_.LocalPort -le 9000){ $l[[int]$_.LocalPort]=$_.OwningProcess } }
# 用 foreach + ContainsKey 判断；Where-Object { $ht[$p] } 对 int 数组不稳定
```

- P2(22)：8080,8081,8082,8083,8084,8086,8092,8093,8095,8096,8097,8101,8103,8111,8112,8141,8142,8143,8144,8145,8146,9000
- P3(8)：8085,8088,8091,8094,8102,8121,8151,8152

### 5.2 Vite 5174

```
cd d:\EMgtv2\frontend
node.exe node_modules/vite/bin/vite.js --port 5174 --host   # 后台运行
```

代理验证降级标准：经 5174 访问返回后端的 **404/500（Spring Whitelabel 或后端 JSON 错误体）= 代理链路通**；只有 Vite 自身 `ECONNREFUSED` 才算失败。业务 500 属遗留问题，不计入启动失败。

### 5.3 日志位置

- 服务 stdout/stderr：`d:\EMgt\server\logs\{Name}-{stdout,stderr}.log`
- 打包/热补丁临时区：`d:\EMgtv2\logs\pentest\`
- JVM 崩溃：`d:\EMgtv2\hs_err_pid*.log`（每轮启动前清理）

---

## 六、PowerShell 环境注意

- 每条 Shell 首行必须 `Set-ExecutionPolicy Bypass -Scope Process -Force`，否则 toolhost 包装脚本 `command.ps1` 直接 Exit 1（会出现"java procs=0"的幻觉输出）。
- 批量启动命令块控制在 30–60s timeout 内，避免 Shell 转后台 job 触发包装脚本异常。
