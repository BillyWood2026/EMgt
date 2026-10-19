---
name: "hrm-platform-recovery"
description: "One-click bring-up of all 44 HRM backend JVMs + MySQL + Vite frontend + auto-login on this Windows box via 9 memory-gated waves with port verification. Invoke after machine reboot / JVM die-off, or when user asks to 恢复/拉起/重启 HRM 平台服务."
---

# HRM 平台一键启动

在 Windows 机器上，把 HRM 全平台（MySQL + 44 个 Spring Boot 后端 + Vite 前端 + 自动登录）从全停状态恢复到全在线。

## 一键执行

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "d:\EMgtv3\.trae\skills\hrm-platform-recovery\scripts\start-all.ps1"
```

- 不带参数：恢复 MySQL + 44 后端 + Vite + 自动登录浏览器（约 12 分钟，9 波门禁）
- `-SkipVite`：只恢复后端，不启动前端和登录
- `-SkipMySql`：跳过 MySQL 预检
- 脚本幂等：已监听的端口自动跳过，可重复执行
- 退出码 0=全部在线、1=有端口 DOWN

### 自动登录流程

Vite 启动后，脚本自动：
1. 调用 IAM(8081) `/api/auth/login`（admin/admin123）获取 JWT token
2. 生成 `tmp/recovery/auto-login.html`，内嵌 localStorage 写入 token/username/role
3. 用默认浏览器打开该 HTML → 写入 localStorage → 自动跳转 `http://localhost:5174/`
4. 前端读到 localStorage 中的 token，直接进入主页，无需手动登录

若登录 API 失败，回退为直接打开 `http://localhost:5174/`（需手动登录）。

## 恢复架构（6 波 × 5 服务，顺序执行，波间等端口门禁）

| 波次 | 服务 | 端口 |
|---|---|---|
| 1 | OrgMgt, EmployeeMgt, AttendanceMgt, RecruitmentMgt, PerfMgt | 8082-8086 |
| 2 | TrainingMgt, QualificationMgt, SalaryMgt, SkillCertMgt, TalentMgt | 8087-8094 |
| 3 | ProjectMgt, HRPlanningMgt, HolidayMgt, TalentReviewMgt, SuccessionPlanMgt | 8091-8192 |
| 4 | TalentDevMgt, OnlineLearnMgt, OnlineExamMgt, AuditMgt, DisciplineMgt | 8193-8196 |
| 5 | PartyAffairsMgt, CultureMgt, ProcessMgt, ImproveMgt, FlowEngineMgt | 8142-8206 |
| 6 | StrategyBlmMgt, StrategyDsteMgt, StrategySixsMgt, StrategyBudgetMgt, StrategyWorkplanMgt | 8101-8105 |

每波启动后最多等 150 秒端口门禁，全部 UP 或超时后进入下一波。

全部 JVM 统一参数：`-Xmx128m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Dfile.encoding=UTF-8` + `--spring.sql.init.mode=never`（应用参数，必须置于 jar 之后）。

## 前置条件

- MySQL 必须先起（Windows 服务；root/Sun-0126King-0325 @ localhost:3306）。脚本开头检查 mysqld 进程，缺失则报错退出
- JDK17 固定在 `C:\Program Files\Java\jdk-17.0.18\bin\`
- Node.js 固定在 `C:\Program Files\nodejs\node.exe`
- 所有数据库 schema 已建好且已播种 → 必须用 `sql.init.mode=never`，否则 schema.sql 对已播种库会 Duplicate entry 崩溃
- 恢复完成后空闲内存通常只剩 2-4GB，**不要再启动新 JVM**

## 执行后验证

脚本末尾自动跑 31 端口汇总（30 后端 + 5174 vite），输出 UP/DOWN 列表。
- 全部 UP → 退出码 0
- 有 DOWN → 退出码 1，查日志

日志目录：`d:\EMgtv3\tmp\recovery\<模块名>.out.log` / `.err.log`。

前端：http://localhost:5174

## 故障处理

### 1. 服务启动即退出，stderr 报 "Unrecognized option: --spring.sql.init.mode=never"

应用参数被放在了 `-jar` 之前，被 JVM 当成非法选项。必须确保 `--spring.sql.init.mode=never` 在 jar 路径之后（当前脚本已修正此顺序）。

### 2. 服务反复崩溃 / 端口起不来

- 看 `<模块名>.err.log`（CNFE/NPE）和 `.out.log` 里的 `Caused by`
- `Duplicate entry ... for key PRIMARY` = 漏了 `--spring.sql.init.mode=never`
- `Unknown column` / `Table doesn't exist` = 实体与 DDL 漂移；按字段名 `ALTER TABLE ... ADD COLUMN`

### 3. 平台又发生整批 JVM 死亡（内存雪崩 / 过夜停机）

直接重跑本 skill 的 `recover-all.ps1` 即可（幂等）。切勿一次并行拉起全部 30 个 JVM —— 必须走脚本的六波门禁。

### 4. 前端代理问题

- vite 代理配置：`d:\EMgtv3\frontend\vite.config.js`；改后自动重启
- 前缀冲突注意：`/api/talent-pool`（8085）必须注册在 `/api/talent` catch-all（8094）之前

## PowerShell 5.1 硬规则（本工作区）

- 调用脚本一律 `powershell -NoProfile -ExecutionPolicy Bypass -File ...`
- .ps1 脚本内容**只能含 ASCII**
- HTTP 测试用 `curl.exe`（curl 是 Invoke-WebRequest 别名）或 Invoke-RestMethod；URL 含 `&` 必须加引号
