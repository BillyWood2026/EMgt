# 党务管理模块（PartyAffairsMgt）建设实施计划

## 一、仓库调研结论（Repository Research）

- 项目为 Spring Cloud 风格多模块工程，根目录 `d:\EMgtv3` 下每个业务域是**独立 Spring Boot 3.2 微服务**（JDK17），无聚合 pom，逐模块 `mvn package` 出 jar。
- 最新模块 [AuditMgt](file:///d:/EMgtv3/AuditMgt/pom.xml) 为标准范本：
  - 依赖：spring-boot-starter-web、mybatis-plus-spring-boot3-starter 3.5.5、mysql-connector-j 8.2.0、lombok。
  - 分层：`controller → mapper(BaseMapper) → entity(@TableName/@TableLogic)`，无 service 层；统一返回 `common/ResponseResult`；`config/MybatisPlusConfig` 注册分页插件。
  - 标准接口：`GET /list`（LambdaQueryWrapper + Page 分页）、`GET /{id}`、`POST`、`PUT /{id}`、`DELETE /{id}`；控制器 `@CrossOrigin(origins="*")`。
  - 配置：独立 `application.yml`（独立端口、独立 MySQL 库、hikari 限 5 连接、下划线转驼峰、逻辑删除字段 deleted）。
- 前端 Vue3 + Vite + Element Plus：
  - 模块目录 `frontend/src/views/<模块>/module.config.json` 被 [moduleMeta.js](file:///d:/EMgtv3/frontend/src/config/moduleMeta.js) 自动 glob 加载，支持 parent → subModules（菜单分组）→ items（路由页面），无需手写路由。
  - 代理统一在 [vite.config.js](file:///d:/EMgtv3/frontend/vite.config.js) 按 `/api/前缀` 映射后端端口。
  - 页面范本 [Plan.vue](file:///d:/EMgtv3/frontend/src/views/audit-mgt/Plan.vue)：搜索表单 + el-table + 分页 + 新增/编辑对话框 + 删除确认。
- 端口占用：8080–8103、8111/8112、8121、8142、8143、8145 已占用 → 新模块选用 **8150**。
- `start-all.ps1` 已过时（写死 D:\EMgtv2 且不含新模块），不改动它；新服务用独立命令启动。
- 已确认范围：**单一微服务 + 52 个子域全部标准 CRUD + 按表中 10 个功能域实施**。

## 二、目标结构

- 后端：`d:\EMgtv3\PartyAffairsMgt`，artifactId `PartyAffairsMgt:1.0.0`，基础包 `com.example.partyaffairsmgt`，端口 **8150**，MySQL 库 **party_affairs_mgt**，API 前缀 `/api/party/<域>/<实体>`。
- 前端：`d:\EMgtv3\frontend\src\views\party-mgt\`，父菜单 key `/party-mgt`，标题「党务管理」，icon `Flag`，displayOrder 6；10 个 subModules 对应 10 个功能域，共 52 个页面。
- 后端按 10 域分包：`org / membership / member / news / opinion / activity / assessment / discipline / supervision / stats`，每包含 `entity、mapper、controller` 子包；`@MapperScan("com.example.partyaffairsmgt")` 递归扫描。

## 三、52 个实体/数据表清单（均含 id、create_time、update_time、deleted 公共字段）

1. **org 党组织管理**
   - `party_org` 组织架构（org_no、name、org_type、parent_id、secretary、contact_phone、established_date、sort_order、status）
   - `term_election` 换届选举（org_id、term_no、election_date、term_start/end、elected_secretary、voter_count、status）
   - `org_life_meeting` 组织/民主生活会（org_id、meeting_type、topic、meeting_date、location、host、participants、minutes、status）
   - `democratic_appraisal` 民主评议党员（org_id、member_id、member_name、appraisal_year、result、self/mutual/org 意见、appraisal_date）
   - `heart_talk` 谈心谈话（org_id、talker/interviewee、talk_type、talk_date、location、topic、content、status）
   - `party_affair_publicity` 党务公开（org_id、title、category、content、public_scope、publisher、publish_date、status）
2. **membership 党籍党费管理**
   - `party_membership` 党籍管理（member_id、card_no、membership_status、join_date、probation_start/end、full_member_date、reason）
   - `member_development` 发展党员全流程（applicant_name、gender、id_card、branch_name、stage 五阶段、apply/activist/target/probation/full 日期、introducer）
   - `dues_payment` 党费收缴（member_id、fee_year/month、base_amount、due_amount、paid_amount、pay_date、pay_type、collector、status）
   - `dues_expense` 党费使用（expense_no、org_id、usage_type、amount、expense_date、purpose、handler、approver、status）
   - `dues_publication` 党费公示统计（org_id、period、total_collected/turned_over/used、balance、publish_date、status）
3. **member 党员管理**
   - `party_member` 党员信息档案（member_no、name、gender、birth_date、id_card、ethnicity、education、phone、org_id、position、join/full_date、member_status）
   - `relation_transfer` 组织关系转接（member_id、transfer_type、from_org、to_org、reason、apply/transfer_date、letter_no、status）
   - `member_score` 党员积分（member_id、score_year/month、item_type、item_name、score、record_date、recorder）
   - `floating_member` 流动党员（member_id、flow_type、destination、contact_phone、start/end_date、activity_status、manager、status）
   - `care_assistance` 关怀帮扶（member_id、care_type、difficulty_type、amount、content、care_date、handler、status）
   - `education_training` 教育培训（title、training_type、org_id、start/end_date、location、lecturer、hours、participants、status）
4. **news 新闻宣传管理**
   - `news_column` 栏目（name、code、parent_id、sort_order、visible、status）
   - `news_article` 稿件（title、column_id、author、source、summary、content、cover_image、submit_time、status）
   - `news_review` 三级审核（article_id、review_level、reviewer、review_time、result、opinion）
   - `news_recommendation` 置顶推荐（article_id、rec_type、rank_score、start/end_time、operator、status）
   - `news_read_stat` 阅读统计（article_id、stat_date、pv、uv、like/comment/share_count）
   - `news_publish` 多端发布（article_id、channel、publisher、publish_time、url、status）
5. **opinion 舆情监控管理**
   - `opinion_collection` 采集（title、source、url、author、region、keywords、content、collect_time、status）
   - `opinion_analysis` 分析（collection_id、sentiment、heat_level、heat_value、keywords、analyst、analyze_time、summary）
   - `opinion_alert` 分级预警（collection_id、alert_level 蓝黄橙红、alert_reason、alert_time、receiver、deadline、status）
   - `opinion_handle` 处置闭环（alert_id、measure、handler、handle_time、deadline、result、feedback、status）
   - `opinion_report` 舆情报告（report_no、report_type、period_start/end、content、reporter、report_time、status）
6. **activity 党组织活动管理**
   - `activity_plan` 活动计划（org_id、title、activity_type、plan_date、location、content、participant_scope、budget、status）
   - `activity_enrollment` 发布报名签到（activity_id、member_id、enroll/sign_in/sign_out_time、attend_status）
   - `activity_record` 记录归档（activity_id、hold_date、host、attend_count、content、minutes、archive_time、archivist、status）
   - `three_meetings_lesson` 三会一课台账（org_id、meeting_type 四类、title、hold_date、host、should/actual_count、content、status）
   - `activity_assessment` 活动统计考核（org_id、assess_year、plan/actual_count、attendance_rate、score、grade、assess_date）
7. **assessment 党建考核评价**
   - `assessment_indicator` 指标体系（name、code、parent_id、category、weight、max_score、scoring_standard、sort_order、status）
   - `assessment_scheme` 考核方案（scheme_no、name、assess_year、target_type、start/end_date、description、status）
   - `assessment_process` 考核流程（scheme_id、node_name、node_order、handler_role、deadline、requirement、status）
   - `score_algorithm` 评分算法（name、code、algorithm_type、formula、weight_rule、description、status）
   - `assessment_result` 结果应用（scheme_id、target_org、total_score、grade、rank_level、application、feedback、status）
8. **discipline 纪检监察管理**
   - `petition_report` 信访举报（report_no、reporter_name、is_anonymous、reported_name/org、channel、content、receive_date、status）
   - `problem_clue` 问题线索（clue_no、source、target_name/org、problem_type、description、receive_date、handler、status）
   - `discipline_investigation` 审查调查（case_no、clue_id、target_name、investigator、start/end_time、facts、measure、result、status）
   - `case_adjudication` 案件审理与处分执行（case_no、punishment_type 五种处分、adjudication_opinion、adjudicate/execute_date、executor、status）
   - `discipline_education` 纪律教育（title、education_type、org_id、hold_date、location、lecturer、participants、content、status）
9. **supervision 监察监督与党风廉政**
   - `daily_supervision` 日常监督（title、supervision_type、target_org/person、supervisor、supervise_date、content、finding、requirement、status）
   - `integrity_profile` 廉政档案（person_name、org_id、position、base/family_info、personal_report、commitment、discipline_record、update_date）
   - `integrity_risk` 廉政风险防控（org_id、risk_point、risk_category、risk_level、prevention_measure、owner、status）
   - `integrity_talk` 廉政谈话与提醒（talk_type 四类、talker/interviewee_name、org_id、talk_date、content、rectification、status）
   - `inspection_rectification` 巡察与整改监督（inspection_name、round_no、inspected_org、inspect_date、feedback_issue、measure、deadline、rectify_date、status）
   - `integrity_assessment` 党风廉政建设考核（org_id、assess_year、score、grade、responsibility_person、assess_date、comment、status）
10. **stats 统计分析中心**
    - `dashboard_stat` 党建驾驶舱快照（stat_date、org/member/full/probation/floating/activity/meeting_count、due_collected、petition_count）
    - `stat_report` 固定报表（report_no、report_type、period_start/end、content、generator、generate_time、status）
    - `stat_drill` 数据下钻配置（name、dimension、metric、drill_path、config_json、description、update_time）

## 四、文件清单（Files and Modules）

后端（共约 162 个文件）：
- `PartyAffairsMgt/pom.xml`（复制 AuditMgt 依赖，改名）
- `src/main/resources/application.yml`（端口 8150、库 party_affairs_mgt）
- `src/main/resources/schema.sql`（52 张表 CREATE TABLE IF NOT EXISTS，utf8mb4）
- `PartyAffairsMgtApplication.java`、`common/ResponseResult.java`、`config/MybatisPlusConfig.java`
- 10 域 ×（entity + mapper + controller）= 52 × 3 = 156 个 Java 文件（严格复用 AuditMgt 模板）

前端（共 55 个文件 + 1 处修改）：
- 新增 `views/party-mgt/PartyCrud.vue`：通用 CRUD 页（搜索/表格/分页/对话框表单/删除，字段由 config 元数据驱动，支持 input/textarea/select/date/number）。
- 新增 `views/party-mgt/entityConfig.js`：52 个页面的字段元数据（apiBase、标题、搜索项、列、表单项、枚举 options）。
- 新增 52 个薄页面 `views/party-mgt/<域>/<Name>.vue`（每个约 15 行，向 PartyCrud 传入对应 config）。
- 新增 `views/party-mgt/module.config.json`（10 subModules / 52 items）。
- 修改 [vite.config.js](file:///d:/EMgtv3/frontend/vite.config.js)：新增 `'/api/party' → http://localhost:8150`。

## 五、实施步骤（依赖排序）

1. 建后端骨架：pom.xml、Application、common/config、application.yml。
2. 编写 `schema.sql`（52 表）；用本机 mysql CLI 创建 `party_affairs_mgt` 库并导入 schema（失败则仅交付脚本并说明手动执行命令）。
3. 按 10 个域分批生成 entity/mapper/controller（模板化，可并行子代理，每批完成后自查包名/路径/表名）。
4. `mvn -f PartyAffairsMgt/pom.xml package -DskipTests` 验证编译打包。
5. 前端：先写 PartyCrud.vue 与 entityConfig.js，再批量生成 52 个薄页面与 module.config.json，最后改 vite 代理。
6. 启动 jar（8150）做冒烟测试：每域抽 1 个接口验证 GET /list（共 10 个），再对其中 1 个做 POST/PUT/DELETE 闭环。
7. 前端诊断（GetDiagnostics）并校验 JSON 合法；如 vite 在运行则确认菜单/路由自动出现。

## 六、依赖与注意事项（Dependencies & Considerations）

- 全部技术沿用存量版本：Spring Boot 3.2.0 / MyBatis-Plus 3.5.5 / MySQL Connector 8.2.0 / Lombok / JDK17，不引入新依赖。
- 实体日期类型按语义用 LocalDate/LocalDateTime；状态/枚举类字段统一 String + 前端 options 映射，与 AuditMgt 一致。
- 控制器保持无 service 层、直接注入 Mapper 的存量风格；分页固定 pageNum/pageSize。
- 前端通用页为工程化取舍：52 页同构，元数据驱动可保证一致性并便于后续扩展字段；外观与审计模块页面保持一致。
- 不注册 Nacos、不改网关与 start-all 脚本；前端经 vite 代理访问，与 AuditMgt 模式相同。
- MySQL 连接信息取自现有 application.yml（root @ localhost:3306）。

## 七、验证（Validation）

- `mvn package` 成功且产出 `PartyAffairsMgt/target/PartyAffairsMgt-1.0.0.jar`。
- MySQL 中 `party_affairs_mgt` 库存在 52 张表（`SHOW TABLES` 计数）。
- 8150 端口启动后，10 个代表接口均返回 code=200；1 个实体 POST→PUT→DELETE 闭环成功。
- 前端 module.config.json 为合法 JSON；52 个 component 路径文件均存在；GetDiagnostics 无报错；vite 下「党务管理」父菜单与 10 个分组、52 个页面可见可打开。

## 八、风险（Risks）

- **体量大（约 217 个文件）**：严格模板化并按域分批/并行生成；每批用编译与路径检查兜底，避免返工。
- **MySQL CLI/口令不可用**：降级为仅交付 schema.sql，附 `mysql -u root -p ... < schema.sql` 手动导入命令，不阻塞代码交付。
- **端口 8150 冲突**：启动前用 Get-NetTCPConnection 探测，被占则顺延 8151 并同步改 yml 与 vite 代理。
- **vite 缓存导致新菜单不显示**：提示重启 `npm run dev`（模块元数据在启动时 eager glob）。
