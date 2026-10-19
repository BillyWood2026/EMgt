# HRM 平台跨平台桌面安装包实施计划

## Context

d:\EMgtv3 是一个 HRM 微服务平台：45 个 Spring Boot（Java 17）模块 + MySQL + Vue3/Vite 前端。目前只能在本机以开发方式运行（start-all.ps1 九波拉起 44 JVM + Vite dev server）。

用户要求：构建 Windows / Linux / macOS 三平台安装文件，安装时自动配置运行环境与数据库，安装完成即可登录使用。

已确认决策：单机演示+部门级使用两者兼顾；捆绑便携版 MySQL；Electron 桌面壳+后台服务编排；45 模块全量打包+JVM 低内存参数。

## 关键事实（已探明）

- 45 个模块 target/ 下均有 `*-1.0.0.jar`，合计 1786MB；44 个业务库已在 dev MySQL 播种完毕（数据 <50MB）
- 45 模块 DB 连接硬编码 `localhost:3306 root/Sun-0126King-0325`（5 个模块支持环境变量覆盖但默认值相同）→ **捆绑 MySQL 必须监听 3306 且 root 密码固定**
- 本机 MySQL 是 26.7（创新版）；**捆绑 MySQL 钉死 8.4 LTS**（8.4 的 `mysql_native_password=ON` 可兼容全部 Connector/J；26.x 已删除该插件不可捆绑）
- 本机 `lower_case_table_names=1`，捆绑 MySQL 必须在初始化 datadir 之前写入该配置，否则 Linux/macOS 表名大小写全灭
- 启用 `skip-name-resolve` 后 `'root'@'localhost'` 对 TCP 失效，需另建 `'root'@'127.0.0.1'` 同密账号
- 九波启动编排/JVM 参数/端口门禁的权威参照：[start-all.ps1](file:///d:/EMgtv3/.trae/skills/hrm-platform-recovery/scripts/start-all.ps1)（W1 基础设施96MB×4 … W9 中台72MB×6，JVM 参数 `-Xms32m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:MaxMetaspaceSize=72m -XX:ReservedCodeCacheSize=48m -Xss512k --spring.sql.init.mode=never`，波内等端口 150s）
- 前端 ~90 条代理规则在 [vite.config.js](file:///d:/EMgtv3/frontend/vite.config.js)（含 6 条正则键，JS 对象按插入序匹配），登录 POST /api/auth/login → token 存 localStorage，vue-router history 模式
- FileMgt 硬编码 `file.store-root: d:\EMgtv3\file-store`，用 `--file.store-root=` 启动参数覆盖
- 本机可直接产出 Windows NSIS；Linux AppImage/deb 用 WSL2 或 CI；macOS dmg 必须 Mac/CI

## 目录结构

```
d:\EMgtv3\desktop\
├── package.json / electron-builder.yml / .gitignore
├── build\            # icon.ico/icns/png、installer.nsh、uninstaller.nsh、license.txt
├── src\
│   ├── main.js       # 入口：单实例锁、生命周期、窗口、托盘、优雅退出
│   ├── splash.html / error.html / status.html / preload.js
│   ├── services\     # paths/logger/portCheck/mysqlManager/gateway/orchestrator/
│   │                 # processTree/healthCheck/pids（各一个 .js）
│   └── config\
│       ├── services.json     # 收集产物：45 条 {name,port,db,wave,xmx,jar}
│       └── proxy-rules.json  # 收集产物：保序 [{match,type,target}]
├── scripts\          # collect-resources.ps1、download-runtime.ps1/.sh、
│                     # strip-mysql.ps1、verify-bundle.ps1、e2e-smoke.ps1
├── vendor\           # 【不入git】jars\*.jar ×45、frontend\dist、db-dump\*.sql.gz ×44+manifest
├── runtime\          # 【不入git】win/linux/darwin-x64/darwin-arm64 各含 jre+mysql
└── release\          # electron-builder 输出
```

## 实施步骤

### 1. 资源收集（scripts/collect-resources.ps1，本机执行，ASCII-only）
1. 前端 `npm ci && npm run build` → 复制 dist 到 vendor/frontend/dist
2. 收集 45 个 jar → vendor/jars/\<模块名\>.jar（过滤 sources/javadoc，计数≠45 报错）
3. 生成 services.json：内置九波表；正则解析各 application.yml 端口（`(?m)^\s*port:\s*(\d+)`）与库名（字面量 `jdbc:mysql://[^/]+/(\w+)`，失败回退 `\$\{DB_NAME:(\w+)\}`）；打印 44 库清单供确认，支持 extra-databases.txt 补充
4. 生成 proxy-rules.json：按行序正则提取 vite.config.js 规则（`^` 开头→regex 否则 prefix），localhost→127.0.0.1
5. mysqldump 逐库导出：`--single-transaction --routines --triggers --events --set-gtid-purged=OFF --column-statistics=0 --no-tablespaces --databases <db>`，GzipStream 压缩；导出前 `SHOW DATABASES LIKE` 校验
6. 写 manifest.json（sha256/字节数/源 MySQL 版本）

### 2. 运行时下载（download-runtime.ps1/.sh，版本钉死+sha256 校验）
- JRE：Adoptium Temurin JRE 17（`api.adoptium.net/v3/binary/version/...`），四组 os/arch
- MySQL 8.4.6：winx64.zip、linux-glibc2.17-x86_64、macos x64/arm64 tar.gz（cdn.mysql.com，回退 dev.mysql.com/get/）
- strip-mysql.ps1 白名单裁剪（保留 mysqld/mysql/mysqladmin/mysqldump/mysqlcheck + 必要 dll/so + share/errmsg+charsets），-60% 体积

### 3. dump→MySQL 8.4 兼容性验证（门禁，不过则不打包）
便携 8.4 起独立 3307 实例 → 完整走首启导入 → 校验 44 库表计数与本机一致 → IAM 以 DB_PORT=3307 指向 8.4 跑登录冒烟

### 4. Electron 主进程
- **paths.js**：只读根 process.resourcesPath；可写根 userData（mysql-data/、logs/、file-store/、.initialized、pids.json）
- **首启**：端口预检（3306 被占→error.html 列占用 PID 引导释放；18100 可改）→ 写 my.cnf（lower_case_table_names=1、mysql_native_password=ON、skip-name-resolve、innodb_buffer_pool=96M、performance_schema=OFF、mysqlx=OFF）→ `--initialize-insecure` → 启动 → 双 root 账号设密 → 流式 gunzip|mysql 逐库导入（splash 进度 n/44）→ 抽样校验 → 落 .initialized；中断恢复（有 datadir 无标记→确认后重置）
- **gateway.js（18100，纯 node:http）**：/api/ 按 proxy-rules.json 数组序 first-match（prefix startsWith / regex test）→ http.request 双向流式 pipe（支持 100MB 上传，目标拒连返 502）；其余 GET 静态服务 dist + 扩展名 MIME + gzip + history 回退 index.html；只绑 127.0.0.1
- **orchestrator.js**：按 services.json 九波 spawn java（参数完全复刻 start-all.ps1，FileMgt 追加 --file.store-root，env 注入 DB_HOST/DB_PORT；stdout/err→logs/\<svc\>.log；Unix detached 进程组、Windows 靠 taskkill /T；pid→pids.json）→ 波内 500ms 端口探测 150s 门禁 → webContents.send 进度 → splash 九波 45 格子
- **main.js**：单实例锁 → splash → mysqld+ping → gateway → 九波 → healthCheck（45 端口+登录冒烟）→ 主窗口 loadURL 127.0.0.1:18100；托盘（打开/服务状态/重启全部/日志目录/退出）；退出序列 JVM 优雅→强杀 → mysqladmin shutdown → 清 pids.json；启动时清理 pids.json 孤儿；`--minimized` 自启只驻托盘（setLoginItemSettings）

### 5. 安装包（electron-builder.yml + build/installer.nsh）
- NSIS：oneClick:false、perMachine:false（装用户目录免管理员）、可选目录、桌面快捷方式、完成页"立即启动"、选项页（快捷方式/开机自启→install-options.json）、`.onInit` 3306 预检警告、内存 <7.5GB 提示
- extraResources 按平台注入 runtime/{jre,mysql}+jars+frontend+db-dump+config；asar 打包 src
- Linux：AppImage+deb（deb Depends: libaio1, libtinfo5, libncurses5, libnuma1）
- macOS：dmg ×2（x64/arm64），README 附 `xattr -dr com.apple.quarantine` 指引（公证列 P2）
- 卸载器询问是否删除 userData 业务数据
- **本机产出 Windows NSIS + portable**；Linux/mac 走 .github/workflows/desktop-installer.yml matrix（windows-2022/ubuntu-22.04/macos-13/macos-14，vendor 大资产经 gh release 传递，artifact 回收四平台安装器）；无 GitHub 则 Linux 用 WSL2 本机构建

### 6. 端到端验证
1. verify-bundle.ps1 打包前门禁（jar 数/JSON 合法/gz 可解含 CREATE DATABASE/sha256）
2. 本机模拟干净机：停 dev MySQL 与全部 JVM → `npm run dev` 走完首启 → admin/admin123 登录 → e2e-smoke.ps1 抽查（登录/组织树/员工/考勤/薪酬/绩效/战略/EEHS/FileMgt >10MB 上传下载/45 端口全 UP）
3. 净机验证：干净 Win 虚拟机（8GB，无 JDK/MySQL/Node）装 NSIS → 首启计时 → 登录抽查 → 重启系统验证恢复 → 卸载两分支
4. Linux/mac 实测 CI artifact（重点 lower_case_table_names、glibc、Gatekeeper）

## 体积与内存

- Windows 安装包约 2.0-2.1GB（45 jar 1750MB 主导 + MySQL ~130MB + JRE ~46MB + Electron ~85MB）
- 运行内存：Xmx 合计 4880MB，RSS 约 5-6GB，8GB 机器紧张可跑，建议 16GB
- P2 优化（后续排期）：jar 依赖去重（loader.path 共享 libs，1786→500MB）、代码签名/公证、tier 精简包

## 主要风险与缓解

R1 dump 26.7→8.4 兼容性 → 步骤 3 门禁验证；R2 认证插件 → 钉死 8.4 + mysql_native_password=ON；R3/R4 见关键事实（双账号、初始化前写配置）；R5 内存 → 精简参数+内存检测+tier 预留；R6 杀软 → 白名单提示；R9 3306 硬编码不可换 → 安装预检+明确引导；R10/R11 首启中断/进程泄漏 → .initialized 标记 + pids.json 双阶段关停；R13 中文路径 → spawn 参数数组+my.cnf 正斜杠；R14 冷启动 3-6 分钟 → splash 分波进度。

## 关键参照文件

- [start-all.ps1](file:///d:/EMgtv3/.trae/skills/hrm-platform-recovery/scripts/start-all.ps1) — 九波编排移植源
- [vite.config.js](file:///d:/EMgtv3/frontend/vite.config.js) — 代理规则提取源
- [IAM application.yml](file:///d:/EMgtv3/IAM/src/main/resources/application.yml) — 环境变量型 DB 配置样本
- [FileMgt application.yml](file:///d:/EMgtv3/FileMgt/src/main/resources/application.yml) — store-root 覆盖点
