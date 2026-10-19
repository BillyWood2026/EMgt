# 党员管理（需求第6章）业务化改造 实施方案

## 一、调研结论（现状基线）

- 后端微服务 `PartyAffairsMgt`（Spring Boot 3.2 / JDK17 / MyBatis-Plus，端口 8150，库 `party_affairs_mgt`），包 `com.example.partyaffairsmgt`。
- `member` 域现有 6 个**薄 CRUD**（控制器仅继承 BaseCrudController，无业务规则）：
  - `party_member`（党员主档，13 人，901–913，含 1 名预备党员周明轩 908，分属 901/903/904 三个组织，**不可破坏其结构**）、
  - `relation_transfer`（组织关系转接，字段单薄，0 条）、
  - `member_score`（积分，0 条）、
  - `floating_member`（流动党员，0 条）、
  - `care_assistance`（关怀帮扶，0 条）、
  - `education_training`（教育培训，0 条）。
- 公共层 `common/party/` 已有：PartyRole（R02/R03/R04/R07/R08/R12/R99）、`PartyBizException.block(code,msg)`、`PartyAuthException`(403)、AuditService、CurrentHeader。
- 前端 `frontend/src/views/party-mgt/member/` 6 个页面均为 `PartyCrud` 薄壳；菜单 module.config.json 已有 6 个菜单项，路径与文件名对应，**无需改菜单结构**。
- 需求第 6 章无新增角色，沿用现有 7 角色。
- 需求范围：6.1 党员信息档案（脱敏/解锁/党龄/结构统计/档案材料）、6.2 组织关系转接（申请→审批→介绍信→接收→回执）、6.3 党员积分（规则/流水/排名/年度重置）、6.4 流动党员（流出流入/双向联系/活动证/失联预警）、6.5 关怀帮扶（困难台账/走访慰问/帮扶发放）、6.6 教育培训（计划/课程/学时/师资/统计）。

## 二、设计原则与权限模型

- 完全沿用 org/membership 域改造模式：Service 承载全部规则、Controller 写接口 `PartyRole.requireAny(...)`、查询不鉴权（R99 只读）、统一 ResponseResult、异常 HTTP400 且 `data.errorCode`、权限异常 403、AuditService 留痕。
- 权限矩阵：
  - **党员档案**：编辑/新增 R02/R04/R07/R08；敏感字段解锁 R02/R04/R07；本人可维护联系方式（R99 只读不可写）。
  - **组织关系转接**：申请/转出登记 R02/R04/R07/R08；原支部审核 R07/R08；原党委审批 R03；接收确认 R02/R04/R07/R08。
  - **积分**：规则配置 R02/R04；流水登记（手工）R02/R04/R07/R08；排名查看 R99。
  - **流动党员**：流出/流入登记 R02/R04/R07/R08；联系记录 R02/R04/R07/R08；销号 R02/R04/R07/R08。
  - **关怀帮扶**：台账/慰问/帮扶登记 R02/R04/R07/R08；公示合规校验 R02/R04。
  - **教育培训**：计划/课程/师资维护 R02/R04；学时登记 R02/R04/R07/R08；统计查看 R99。

## 三、数据库改造（`src/main/resources/migration_member_v2.sql`，可重入；同步 schema.sql；种子 `seed_member_demo.sql`）

### 新建表
1. `member_archive_material` 档案材料：member_id、material_type（入党志愿书/申请书/考察登记表/转正申请书/其他）、material_name、file_path、file_size、upload_by、upload_time、borrower、borrow_date、return_date、status（在库/借阅中）、时间戳、deleted。
2. `member_sensitive_unlock` 敏感字段解锁申请：member_id、field_name（id_card/phone/address）、applicant、reason、approver、audit_status（待审批/已批准/已驳回）、audit_time、expire_time（解锁有效期）、时间戳、deleted。
3. `transfer_letter` 介绍信：letter_no（组织编码+年份+4位流水）、transfer_id、member_id、member_name、from_org_id、from_org_name、to_org_id、to_org_name、issue_date、valid_days（默认30）、expire_date、receive_date、receiver、receipt_no、receipt_date、status（已开具/已接收/已回执/已过期）、时间戳、deleted。
4. `transfer_flow` 转接审批链：transfer_id、node（支部审核/党委审批/开具介绍信/接收确认/回执归档）、approver、opinion、audit_time、status（通过/驳回）、时间戳。
5. `score_rule` 积分规则：rule_key（按时交党费/组织生活/学习培训/志愿服务/岗位建功/建言献策/违纪扣分）、rule_name、score_value、period（年度/季度/月度）、auto_flag（是否自动关联）、enabled、creator、时间戳。
6. `score_flow` 积分流水：member_id/member_name/org_id、rule_key、rule_name、score、source_type（缴费/活动/手工）、source_id、audit_status（手工加分需审批：待审批/已批准/已驳回）、approver、audit_time、score_year、score_month、remark、时间戳、deleted。
7. `float_contact` 双向联系记录：floating_id、member_id、contact_date、contact_way（电话/走访/视频/书信）、content、recorder、时间戳。
8. `float_cert` 流动党员活动证：floating_id、member_id、cert_no、issue_date、issuer、status（有效/已注销/已过期）、时间戳。
9. `care_difficulty` 困难党员台账：member_id/member_name/org_id、difficulty_type（生活困难/因公伤残/老党员/因公殉职牺牲家庭）、difficulty_desc、apply_date、approve_date、approver、status（在册/已退出）、exit_date、exit_reason、时间戳、deleted。
10. `care_visit` 走访慰问记录：member_id/member_name/org_id、visit_node（春节/七一/中秋/重阳/其他节日/纪念日）、visit_date、visitors、content、money、materials、is_duplicate（同节点重复提醒标记）、时间戳。
11. `care_help` 帮扶申请与发放：member_id/member_name/org_id、help_type、amount、apply_date、approve_date、approver、grant_date、grant_way、status（申请中/已批准/已发放/已驳回）、dues_expense_id（关联党费使用）、时间戳、deleted。
12. `edu_plan` 培训计划：plan_name、org_id、train_year、target_count、form（集中轮训/党委理论学习中心组/理论宣讲/在线学习/其他）、target_hours、start_date、end_date、status（计划中/进行中/已完成/已取消）、creator、时间戳、deleted。
13. `edu_course` 课程库：course_name、category、hours、lecturer、content、enabled、creator、时间戳、deleted。
14. `edu_teacher` 师资库：teacher_name、title、specialty、org_id、phone、enabled、creator、时间戳、deleted。
15. `edu_study` 在线学习记录：member_id/member_name/org_id、course_id、course_name、study_date、study_minutes、course_minutes、progress（学习进度%）、valid_flag（是否计入学时：进度≥80%）、score_hours（实际计入学时）、时间戳、deleted。
16. `edu_hours_stat` 学时统计：member_id/member_name/org_id、stat_year、total_hours、target_hours、completion_rate、status（达标/未达标）、时间戳、deleted。

### 扩展列（ALTER，均给默认值保证旧数据兼容）
- `party_member`：photo_url（照片）、political_status（政治面貌：正式党员/预备党员/共青团员/群众）、float_status（在岗/流动/失联）、archive_complete（档案材料是否完整：0/1）、contact_audit_status（联系方式修改待审批/已批准）。
- `relation_transfer`：from_org_id、to_org_id、apply_reason（原 reason 保留）、current_node（当前节点）、letter_no、receive_date、receipt_status、flow_status（在办/已完成/已驳回）。
- `member_score`：rule_key、source_type、source_id、audit_status、approver、audit_time、score_year、score_month。
- `floating_member`：cert_no、last_contact_date、contact_cycle（约定联系周期，默认3个月）、float_alert_status（正常/预警/告警）、flow_status（流出中/已返回/失联）。
- `care_assistance`：visit_node、visitors、materials、is_duplicate、dues_expense_id。
- `education_training`：plan_id、course_id、study_minutes、progress、valid_flag、score_hours。

### 种子数据（可重入）
- 13 名党员的档案材料（每人至少 1 份入党志愿书，1 人缺材料生成补录待办演示）。
- 敏感字段解锁申请 2 条（1 条已批准、1 条待审批）。
- 组织关系转接 3 条：1 条已完成（含介绍信+回执）、1 条在办（支部审核节点）、1 条预警（介绍信超期未接收 30 天）。
- 积分规则 7 条（系统预置）；积分流水 2026 年 1–9 月数据：按时交党费自动加分、组织生活加分、1 条手工加分待审批、1 条违纪扣分、1 条重复加分去重演示、1 人负分预警。
- 流动党员 3 条：1 条流出（已发活动证，双向联系 2 次）、1 条流入、1 条失联预警（3 月未联系）。
- 关怀帮扶：困难台账 2 人、走访慰问 3 条（含 1 条同节点重复慰问提醒）、帮扶申请 2 条（1 条已发放关联党费使用）。
- 教育培训：年度培训计划 1 条、课程 3 门、师资 2 人、在线学习记录 5 条（含 1 条进度<80% 不计学时、1 条重复学习去重）、学时统计按 2026 年度目标参数化比对。

## 四、后端实现（member 域，全部重写控制器 + 新增 Service）

### 4.1 公共层
- 无新增角色，沿用 PartyRole 现有常量。

### 4.2 党员信息档案 `MemberProfileService` + 重写 `PartyMemberController`，前缀 `/api/party/member/info`
- `GET /list(orgId,politicalStatus,post,status,keyword)` 党员列表（组织树内筛选，脱敏返回：身份证 3 4 X 4 4、手机号 3 4、住址前 6 字）。
- `GET /{id}` 党员详情（卡片式：基本信息/政治面貌/党籍/党内职务/学历/奖惩/培训/党费/活动/评议/入党历程时间轴；敏感字段默认脱敏，已解锁且未过期字段返回原文）。
- `POST` /`PUT`（R02/R04/R07/R08：身份证 18 位校验+重复阻断 E-MB-01；转正日期≥入党日期阻断 E-MB-02；入党日期早于出生日期 14 年预警 E-MB-03；缺联系方式/照片预警 E-MB-04；档案材料缺失生成补录待办 E-MB-05）。
- `POST /{id}/unlock` 敏感字段解锁申请（R02/R04/R07：field_name/reason 必填，生成待审批）。
- `POST /unlock/{id}/audit`（R03：批准/驳回；批准后解锁有效期 24 小时，审计留痕）。
- `GET /materials(memberId,materialType)` 档案材料列表；`POST /materials`（上传登记）；`POST /materials/{id}/borrow`（借阅登记）；`POST /materials/{id}/return`（归还）。
- `GET /statistics` 结构统计（年龄/学历/岗位/党龄分布，按组织树汇总）。
- `GET /import/template` 导入模板下载；`POST /import/preview`（CSV 预览，校验身份证/日期逻辑）；`POST /import/submit`（批量入库，错误行返回）。
- 党龄算法：`fullDate` 到当前日期满年数，服务层计算不存库。

### 4.3 组织关系转接 `RelationTransferService` + 重写 `RelationTransferController`，前缀 `/api/party/member/relation-transfer`
- `GET /list(orgId,status,keyword)` 转接列表（含在办/已完成/预警）。
- `POST`（R02/R04/R07/R08：转接对象必须为本组织党员 E-MB-06；存在未完结在办流程阻断 E-MB-07；目标组织必须存在且层级匹配 E-MB-08；预备党员材料不完整预警 E-MB-09）。
- `POST /{id}/branch-audit`（R07/R08：原支部审核，通过/驳回+意见）。
- `POST /{id}/committee-audit`（R03：原党委审批，通过/驳回+意见；批准后生成介绍信）。
- `POST /{id}/issue-letter`（R02/R04/R07/R08：开具介绍信，编号=组织编码+年份+4 位流水，有效期默认 30 天参数化；介绍信生成后状态=已开具）。
- `POST /{id}/receive`（R02/R04/R07/R08：目标组织接收确认，接收日期+接收人；接收后更新 party_member.org_id）。
- `POST /{id}/receipt`（R02/R04/R07/R08：回执归档，回执编号+日期；流程完结）。
- `GET /letters` 介绍信管理列表；`GET /letter/{letterNo}` 介绍信详情。
- `GET /alerts` 超期未接收预警（超期 30 天升级告警）；`POST /alerts/remind`（向转出方与转入方发送提醒，站内信/消息模拟）。
- `GET /history(memberId)` 个人转接历史。

### 4.4 党员积分 `MemberScoreService` + 重写 `MemberScoreController`，前缀 `/api/party/member/score`
- 规则：`GET /rules`、`POST/PUT /rules`（R02/R04；规则分值调整历史不变，仅影响新流水）。
- 流水：`GET /flows(memberId,orgId,year,month,ruleKey)`、`POST /flows`（R02/R04/R07/R08：手工加分需审批，audit_status=待审批；自动关联业务单据，同一事项重复加分去重 E-MB-10；负分预警 E-MB-11）。
- `POST /flows/{id}/audit`（R03：手工加分审批）。
- 排名：`GET /rank(orgId,year,scope=branch/group)`（按支部/全集团排名，返回 topN+本人排名）。
- 年度重置：`POST /reset-year`（R02/R04：按配置年度重置或结转，生成年度结转记录）。
- `GET /my-score(memberId,year)` 个人积分明细与汇总。

### 4.5 流动党员 `FloatingMemberService` + 重写 `FloatingMemberController`，前缀 `/api/party/member/floating`
- `GET /list(orgId,flowType,status,keyword)` 台账。
- `POST /out` 流出登记（R02/R04/R07/R08：外出地点/时间/联系方式必填；发放活动证编号）。
- `POST /in` 流入登记（R02/R04/R07/R08：流入单位/时间/联系方式必填；关联原组织）。
- `POST /{id}/return` 返回销号（R02/R04/R07/R08）。
- `GET /{id}/contacts` 双向联系记录；`POST /{id}/contacts`（R02/R04/R07/R08：每季度至少一次参数化，低于周期预警）。
- `GET /certs` 活动证管理；`POST /certs/{id}/revoke` 注销。
- `GET /alerts` 失联预警（3 月预警/6 月告警，按 last_contact_date 计算）；`POST /alerts/contact` 提醒联系人。

### 4.6 关怀帮扶 `CareAssistanceService` + 重写 `CareAssistanceController`，前缀 `/api/party/member/care`
- 困难台账：`GET /difficulties(orgId,difficultyType)`、`POST /difficulties`（R02/R04/R07/R08）、`POST /difficulties/{id}/exit`（退出）。
- 走访慰问：`GET /visits(orgId,visitNode,year)`、`POST /visits`（R02/R04/R07/R08：同一慰问节点重复慰问提醒 E-MB-12，不阻断但标记 is_duplicate）。
- 帮扶：`GET /helps(orgId,status)`、`POST /helps`（R02/R04/R07/R08）、`POST /helps/{id}/approve`（R03）、`POST /helps/{id}/grant`（R02/R04/R07/R08：发放+关联党费使用记录，资金联动）。
- `GET /publicity-check` 帮扶名单公示合规校验（是否超范围公开敏感信息）。

### 4.7 教育培训 `EducationTrainingService` + 重写 `EducationTrainingController`，前缀 `/api/party/member/education`
- 计划：`GET /plans(orgId,year)`、`POST/PUT /plans`（R02/R04）。
- 课程：`GET /courses`、`POST/PUT /courses`（R02/R04）。
- 师资：`GET /teachers`、`POST/PUT /teachers`（R02/R04）。
- 在线学习：`GET /study-records(memberId,courseId,year)`、`POST /study-records`（R02/R04/R07/R08：进度<80% 不计学时 E-MB-13；同一课程重复学习去重 E-MB-14）。
- 学时统计：`GET /hours-stat(orgId,year)`（按年度目标参数化比对，返回达标/未达标清单）；`POST /hours-stat/generate`（R02/R04：按学习记录生成年度学时统计）。
- `GET /plan-completion(planId)` 计划完成率（完成人数/应训人数）。
- `GET /training-warning` 学时不足预警（纳入考核提示）。

## 五、前端实现（重写 6 个页面，沿用 RoleBar + can + axiosConfig 模式）

1. `member/MemberProfile.vue`：Tab【党员列表】（筛选/搜索/卡片视图切换/批量操作/新增编辑弹窗含身份证校验）【党员详情】（大弹窗：基本信息+入党历程时间轴+敏感字段解锁申请按钮）【档案材料】（上传/预览/借阅登记）【结构统计】（年龄/学历/岗位/党龄图表）【导入导出】。
2. `member/RelationTransfer.vue`：Tab【转接申请】（新增弹窗含组织选择/事由/材料清单）【审批链】（支部审核→党委审批→介绍信→接收→回执五步步骤条，各节点操作按钮按角色显隐）【介绍信管理】（列表/详情/超期预警）【转接历史】。
3. `member/MemberScore.vue`：Tab【积分规则】（CRUD+启停）【积分流水】（列表/手工登记弹窗/审批 R03/重复去重提示）【积分排名】（支部/集团切换，topN 表格+本人高亮）【年度重置】（结转确认弹窗）。
4. `member/FloatingMember.vue`：Tab【流出登记】【流入登记】【流动台账】（状态 tag/活动证号/最后联系日期）【双向联系】（时间线+新增记录弹窗）【失联预警】（3 月预警/6 月告警卡片+提醒按钮）。
5. `member/CareAssistance.vue`：Tab【困难台账】（类型筛选/退出操作）【走访慰问】（节点日历/重复慰问提醒 tag/新增弹窗）【帮扶申请发放】（申请→审批→发放三步，关联党费使用）【公示合规】（校验结果列表）。
6. `member/EducationTraining.vue`：Tab【培训计划】（年度 CRUD+完成率进度条）【课程库】（课程卡片+学时）【在线学习】（学习记录登记/进度条/不计学时标记）【学时统计】（年度目标比对表/达标率图表/预警名单）【师资库】。

## 六、实施顺序（依赖序）

1. migration SQL + 执行 + schema.sql 同步 + 种子。
2. 党员档案 Service/Controller（脱敏/解锁/党龄/结构统计/档案材料）→ 冲烟。
3. 组织关系转接 Service（审批链+介绍信+超期预警）→ 冲烟。
4. 积分 Service（规则/流水/排名/年度重置）→ 冲烟。
5. 流动党员 Service（流出流入/联系记录/活动证/失联预警）→ 冲烟。
6. 关怀帮扶 Service（困难台账/慰问/帮扶/公示合规）→ 冲烟。
7. 教育培训 Service（计划/课程/学时/师资/统计）→ 冲烟。
8. 前端 6 页面重写 + GetDiagnostics。
9. 端到端验证（接口脚本全部分支 + 浏览器 6 页面角色显隐/关键弹窗/阻断提示）。

## 七、验证方案

- 后端：`mvn -q package -DskipTests` 重启 8150；PowerShell 冲烟脚本覆盖：
  - 档案：身份证非法/重复 E-MB-01、转正<入党 E-MB-02、缺联系方式 E-MB-04、解锁申请/审批/过期、结构统计 200。
  - 转接：非本组织党员 E-MB-06、在办重复 E-MB-07、目标组织不存在 E-MB-08、介绍信编号生成规则、超期预警、预备党员材料预警。
  - 积分：规则 CRUD、手工加分审批、重复加分去重 E-MB-10、负分预警 E-MB-11、排名、年度重置。
  - 流动：流出/流入/销号、联系记录周期预警、失联 3 月/6 月告警。
  - 关怀：困难台账 CRUD、重复慰问提醒 E-MB-12、帮扶审批发放、公示合规。
  - 教育：计划/课程/师资 CRUD、进度<80% 不计学时 E-MB-13、重复学习去重 E-MB-14、学时统计达标率。
- 前端：GetDiagnostics 0 错；浏览器 6 页面渲染、按钮显隐、工作台/弹窗与阻断 message。

## 八、风险与处理

- **介绍信编号并发**：采用数据库唯一索引+序列表或 UUID 兜底，确保“组织编码+年份+4 位流水”不重复。
- **敏感字段解锁**：解锁记录存库+有效期控制+审计留痕，R99 不可见原文。
- **跨域联动**（帮扶发放→党费使用；转接接收→party_member.org_id 更新；积分自动关联缴费/活动）：通过 Service 直接调用 Mapper 完成，不引入消息机制；全部包在事务内并写审计。
- **旧数据兼容**：仅 ALTER 加列带默认值，不删除/改名旧列；旧 PartyCrud 配置（entityConfig.js 中 6 个 cfgKey）不再被页面引用但保留文件不删。
- 不改动 org/membership 等其他域代码；菜单结构无需变更。
