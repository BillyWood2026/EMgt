# 党籍党费管理（需求第5章）业务化改造 实施方案

## 一、调研结论（现状基线）

- 后端微服务 `PartyAffairsMgt`（Spring Boot 3.2 / JDK17 / MyBatis-Plus，端口 8150，库 `party_affairs_mgt`），包 `com.example.partyaffairsmgt`。
- `membership` 域现有 5 个**薄 CRUD**（控制器仅继承 BaseCrudController，无业务规则）：
  - `party_membership`（党籍，字段单薄，0 条数据）、`member_development`（发展党员，仅 6 个日期字段，0 条）、
  - `dues_payment`（党费收缴，无基数/比例/凭证/确认人，0 条）、`dues_expense`（党费使用，0 条）、`dues_publication`（党费公示，0 条）。
- 主数据 `party_member` 有 12 名党员（901–912，含 1 名预备党员周明轩 908，分属 901/903/904 三个组织），org 域改造时已被评议/谈话/选举服务引用，**不可破坏其结构**。
- 公共层 `common/party/` 已有：PartyRole（R02/R03/R04/R07/R99）、`PartyBizException.block(code,msg)`、`PartyAuthException`(403)、AuditService、CurrentHeader。
- 前端 `frontend/src/views/party-mgt/membership/` 5 个页面均为 `PartyCrud` 薄壳；菜单 module.config.json 已有 5 个菜单项（党籍管理/发展党员/党费收缴/党费使用/党费公示统计），路径与文件名对应，**无需改菜单结构**。
- 需求第 5 章新增两个角色：**R08 支部组织委员**（党员发展、党费业务经办）、**R12 党费财务（会计/出纳）**（收缴确认、记账、对账、公示，会计出纳分设）。
- 需求范围：5.1 党籍管理（状态机+审批+证明）、5.2 发展党员五阶段全流程（E-DEV-01~13、时限/票决/材料/结构统计）、5.3 党费收缴（分段比例算法、月账单、收缴确认、催缴、补缴减免、大额/5%上缴、公示、E-DU-01~11）、5.4 党费使用（五类范围、集体讨论纪要、收支平衡、公示）。

## 二、设计原则与权限模型

- 完全沿用 org 域改造模式：Service 承载全部规则、Controller 写接口 `PartyRole.requireAny(...)`、查询不鉴权（R99 只读）、统一 ResponseResult、异常 HTTP400 且 `data.errorCode`、权限异常 403、AuditService 留痕。
- 新增角色常量：PartyRole 增加 `R08`、`R12`；前端 partyAuth 角色切换器同步增加两项。
- 权限矩阵：
  - **党籍**：变动发起 R02/R04/R07/R08；上级党委审批 R03；证明开具 R02/R07/R08。
  - **发展党员**：节点经办 R02/R04/R07/R08；上级备案/预审/党委审批类节点 R03；政审结论仅 R02/R03/R04/R07 可见（R08 经办可见，R99 脱敏隐藏）。
  - **党费收缴**：基数维护 R02/R08/R12；账单生成 R02/R08；收缴登记 R02/R08/R12；财务确认入账 R12（登记人与确认人不可同一人，会计出纳分设）；减免审批 R03；大额/上缴 R02/R12；公示 R02/R12。
  - **党费使用**：计划/申请/登记 R02/R07/R08/R12；审批 R03（超权限留痕）；公示 R02/R12。

## 三、数据库改造（`src/main/resources/migration_membership_v2.sql`，可重入；同步 schema.sql；种子 `seed_membership_demo.sql`）

### 新建表
1. `membership_change` 党籍变动记录：member_id/member_name/org_id、change_type（停止党籍/恢复党籍/自行脱党/除名/开除党籍/死亡注销/转入/转出）、reason(必填)、basis_no 依据文号、effective_date、branch_meeting_date、approver、approve_date、audit_status（待审批/已批准/已驳回）、audit_opinion、creator、时间戳、deleted。
2. `membership_proof` 党籍证明：member_id、proof_no（自动编号）、purpose、issue_date、issuer、时间戳。
3. `dev_plan` 年度发展计划：org_id/org_name/plan_year/target_total/youth_target/frontline_target/intellectual_target/remark/creator。
4. `dev_person` 发展对象主档：name/gender/birth_date/ethnicity/education/department/position/phone/id_card、org_id/org_name、stage（APPLICANT/ACTIVIST/TARGET/PROBATION/FORMAL）、leave_date（E-DEV-12 用）、extend_months、status（在途/已发展/已取消/已延长/已推迟）、时间戳。
5. `dev_node` 流程节点：person_id、stage、node_key、node_name、node_date、payload_json（谈话人/票数/学时等差异化数据）、materials_json（已交材料清单）、operator、audit_status（经办节点默认通过，审批类节点 待审批/已批准/已驳回）、remark、create_time。
6. `dues_base` 党费基数：member_id/member_name/org_id、member_category（月薪/年薪制/个体/离退休/农民/学生/下岗失业/抚恤救济/低保）、base_amount、fixed_amount（特殊群体固定额）、ratio（系统生成）、eff_year/eff_month、prev_base、audit_status（正常/待审批/已批准）、creator/approver、时间戳。
7. `dues_reduce` 减免申请：member_id/org_id/fee_year/fee_month、reduce_type（少交/免交）、amount、reason、branch_record（支部研究记录，必填）、approver、audit_status/opinion、时间戳。
8. `dues_backpay` 补缴登记：member_id/org_id/from_yearmonth/to_yearmonth/amount/reason/branch_agreed/operator/pay_date。
9. `dues_remittance` 上缴单：remit_type（年度上缴/大额党费）、org_id/fee_year/amount/ratio（默认0.05）/remit_date/payee/receipt_no/receipt_date/status/related_payment_id/remark。
10. `dues_account` 党费账户：org_id/bank_name/account_no/enabled（六大行参数化校验 E-DU-09）。
11. `dues_use_plan` 使用年度计划：org_id/plan_year/planned_amount/remark。

### 扩展列（ALTER，均给默认值保证旧数据兼容）
- `dues_payment`：bill_no、member_category、ratio、voucher_no 凭证号、confirmer、confirm_time、reduce_id、backpay_id、diff_reason、large_flag（≥1000）、remit_id、pay_method 细分（现金/转账/银行代扣/线上缴纳/委托代缴，兼容旧 pay_type）。
- `dues_expense`：plan_id、discussion_attachment（集体讨论纪要，必填）、execute_result、audit_status（草稿/待审批/已批准/已登记/已核销）、category（=usage_type 受五类枚举约束）。
- `dues_publication`：fee_year、period_start、period_end、public_scope、attachment。
- `party_membership`：current_status（正常/停止党籍/转出/除名/开除党籍/死亡注销/自行脱党）、stop_date、restore_date、proof_issued_count（由服务维护，不加也可）。
- `party_member` 不加新列（member_status 由党籍服务回写：停止党籍/除名等，值为新增枚举，既有页面按文本显示不受影响）。

### 种子数据（可重入）
- 12 名党员的 dues_base（绝大多数"月薪"档，基数分布 2800–12000 以覆盖四档比例；周明轩亦建基数，预备党员按规定交纳党费）。
- 生成 2026 年 1–9 月账单并登记部分收缴：制造连续 3 月未缴（催缴预警 E-DU-06）、连续 6 月未缴（自行脱党告警 E-DU-07）各 1 例；1 笔大额党费（1200 元，已上缴登记收据）；1 条减免审批演示。
- 发展党员演示人员约 6 人，分布五个阶段（含 1 名公示不足 5 天、1 名培养期不足 1 年、1 名票决不达标、1 名超时未谈话等可复现校验的在途案例）。
- 党籍：1 条已批准的"停止党籍→恢复"历史 + 1 条待审批变动；党费使用计划 1 条、使用登记 2 条（五类范围内）。

## 四、后端实现（membership 域，全部重写控制器 + 新增 Service）

### 4.1 公共层
- `PartyRole` 增加 R08、R12 常量。

### 4.2 党籍 `MembershipService` + 重写 `PartyMembershipController`，前缀 `/api/party/membership`
- `GET /ledger(orgId,status,keyword)` 党籍台账（组织树内党员 + 当前党籍状态，状态来自 party_membership，无记录视为"正常"）。
- `GET /changes(memberId,orgId)`、`POST /change`（发起，R02/R04/R07/R08：reason 必填；状态机校验）、`POST /change/{id}/audit`（R03，pass/opinion；**批准才生效**：回写 party_membership.current_status 与 party_member.member_status，停止期间评议/党费联动标记；审计）。
- `GET /proof/list`、`POST /proof`（R02/R07/R08：自动 proof_no）。
- 状态机：正常→停止/自行脱党/除名/开除/死亡注销/转出；停止党籍→恢复党籍；转出态冻结（转出下再停止=非法）；已除名/开除/死亡为终态。非法流转阻断（E-MS-01）、原因为空阻断（E-MS-02）、未审批生效阻断（E-MS-03，即未批准变动不改台账）。
- `GET /deserter-alerts`：连续 6 月无正当理由未缴党费名单（由党费域数据计算，供 5.1/5.3 共用）。

### 4.3 发展党员 `MemberDevelopmentService` + 重写 `MemberDevelopmentController`，前缀 `/api/party/membership/development`
- 节点注册表（代码内常量，有序）：
  - APPLICANT：APPLY 建档（E-DEV-01 年龄≥18；E-DEV-11 身份证+阶段去重）→ TALK 谈话（1 月内；超 30 天 E-DEV-02 预警/告警）。
  - ACTIVIST：RECOMMEND（党员推荐/群团推优）→ BRANCH_DECIDE 支委会决议 → COMMITTEE_FILE 上级党委备案(R03) → CONTACTS 培养联系人 1–2 名正式党员 → INSPECTION 半年考察（可多条）。
  - TARGET：OPINIONS 意见听取 → BRANCH_STUDY 支委研究+上级同意(R03) → PUBLICITY 公示（**≥5 个自然日**，E-DEV-04 按异常表"按自然日计算"执行）→ INTRODUCERS 介绍人 2 名 → POLITICAL_REVIEW 政审（材料+结论合格，E-DEV-05；高密级字段）→ TRAINING 培训（≥3 天或 24 学时，E-DV-06）→ BRANCH_REVIEW 支委审查 → PRE_AUDIT 基层党委预审(R03)。阶段门槛：积极分子培养满 1 年（E-DEV-03）。
  - PROBATION：MEMBER_MEETING（shouldVote/actualVote/赞成/反对/弃权；实到*2>应到 且 赞成*2>应到，E-DEV-07；逐人表决）→ PARTY_APPROVE 党委审批(R03，3 月内预警/6 月阻断 E-DEV-08) → OATH 宣誓（批复后 1 月内提醒）。
  - FORMAL：APPLY_TURN（E-DEV-09 预备期满；延长 EXTEND 须 ≥6 月且 ≤12 月 E-DEV-13；推迟 POSTPONE ≤6 月；取消 CANCEL）→ OPINIONS2 → BRANCH_REVIEW2 → MEETING2（票决同上）→ PARTY_APPROVE2(R03) → DONE（转正：写 party_member 正式党员/转正日期 + party_membership，党龄起算=转正之日）。
- 每节点配置前置材料清单；完成节点时校验材料齐备（E-DEV-10，逐项返回缺失项）、前置节点全部完成；节点串行，未完成前置按钮对应的后端调用一律阻断。
- `GET /person/list(stage,orgId,keyword)`、`POST /person`、`GET /person/{id}`（返回 person + 节点时间轴 + 缺失材料）、`POST /person/{id}/node/{nodeKey}`（经办节点）、`POST /node/{id}/audit`（R03 审批节点）。
- `GET /plan/list`、`POST/PUT /plan`（R02/R04）、`GET /board`（漏斗：各阶段在途人数、计划完成率、超时预警清单、青年/一线/高知识占比——按 dev_person 的 education/department 近似口径统计并在响应中注明口径）。
- `GET /timeouts`：谈话超 30 天、党委审批超 3 月、宣誓超 1 月、转正审批超 3 月分级提醒。
- E-DEV-12：leave_date 距大会节点 3 个月内 → 预警不阻断。

### 4.4 党费收缴 `DuesService` + 重写 `DuesPaymentController`，前缀 `/api/party/membership/dues`
- **比例引擎**（纯函数 + 单元级冲烟覆盖）：月薪 0–3000→0.5%，3000–5000→1%，5000–10000→1.5%，>10000→2%；离退休 ≤5000→0.5%，>5000→1%；农民固定 0.2–1 元（默认 0.5，可配）；学生/下岗/抚恤/低保固定 0.2 元；应缴=基数×比例，四舍五入两位小数。`GET /calc-preview(category,base)`。
- 基数：`GET /base/list`、`POST /base`（R02/R08/R12；变动 ±20% 置"待审批"，E-DU-02，`POST /base/{id}/audit` R03/R04）、批量导入预览/提交（CSV，参考 org 导入模式）。
- 账单：`POST /bills/generate(orgId,year,month)`（R02/R08；基数缺失/为 0 的党员跳过并返回 skipped 清单，E-DU-01；幂等：同人同月不重复生成）；`GET /bills(year,month,orgId,status)`。
- 收缴：`POST /payments/register`（R02/R08/R12；同人同月重复阻断 E-DU-03；实缴≠应缴且无减免/补缴单时阻断并要求 diff_reason，E-DU-04；实缴≥1000 置 large_flag）；`POST /payments/confirm`（R12；确认人≠登记人，会计出纳分设；批量 id 列表）；凭证号/方式/日期落库。
- 减免：`GET /reduce/list`、`POST /reduce`（R02/R07/R08；branch_record 必填）、`POST /reduce/{id}/audit`（R03；审批链不完整阻断 E-DU-05；批准后账单 status=减免）。
- 补缴：`GET /backpay/list`、`POST /backpay`（R02/R08/R12；跨度 >6 月 E-DU-11 预警需支部同意 branch_agreed；批准后生成对应账单 status=补缴）。
- 催缴：`GET /remind` 返回 {warn3:[…], alarm6:[…]}（E-DU-06/07）；`POST /deserter-workflow`（R02/R07：对 alarm6 人员生成党籍"自行脱党"变动待办，联动 membership_change 并审计）。
- 上缴：`GET /remittance/list`、`POST /remittance/large`（大额党费上缴单，E-DU-08：large_flag 账单未生成上缴单不能归档）、`POST /remittance/yearly`（按全年实交×5% 生成年度上缴单，次年 4 月底提醒参数化）、收据登记 `PUT /remittance/{id}/receipt`。
- 账户：`GET/POST /account`（R02/R12；银行名须命中六大行参数 E-DU-09）。
- 公示待办：`GET /publication-todo`（年度未公示支部，E-DU-10）。
- 统计：`GET /stats(year)` 收缴率/月趋势/类别结构/已上缴/留存余额；`GET /export` 对账单 CSV。

### 4.5 党费使用 `DuesExpenseService` + 重写 `DuesExpenseController`，前缀 `/api/party/membership/dues-expense`
- 计划：`GET /plan/list`、`POST/PUT /plan`（R02/R04）。
- 使用单：`GET /list`、`POST`（R02/R07/R08：usage_type 必须属五类枚举，超范围阻断 E-UE-01；discussion_attachment 必填，无集体讨论纪要阻断 E-UE-02；plan_id 支出超出计划预警 E-UE-03；状态草稿）、`POST /{id}/audit`（R03）、`POST /{id}/register`（R12 财务登记支出日期/核销）。
- 收支平衡：登记时校验该组织当年累计支出 ≤ 当年实交党费 − 已上缴 − 已支出（超收阻断 E-UE-04）。
- `GET /year-summary(orgId,year)` 供公示取数（收/缴/支/余）。

### 4.6 党费公示：重写 `DuesPublicationController` 为薄业务控制器（Service 方法并入 DuesService 或独立 `DuesPublicationService`），前缀沿用 `/api/party/membership/dues-publication`
- `GET /list(year,orgId)`、`POST/PUT`（R02/R12：按支部年度生成，金额由 year-summary 自动带出，period 起止必填）、`POST /publish/{id}`（R02/R12；同年仅允许一条已发布公示）、`GET /todo`（E-DU-10）。

## 五、前端实现（重写 5 个页面，沿用 RoleBar + can + axiosConfig 模式）

1. `membership/Membership.vue`：Tab【党籍台账】（组织/状态筛选、状态 tag、发起变动）【变动记录】（个人时间线、审批按钮 R03）【党籍证明】（开具记录+新增）。变动弹窗含类型/原因(必填)/依据文号/生效日/支委会日期。
2. `membership/Development.vue`：Tab【流程看板】（五阶段漏斗卡片+超时预警表）【发展计划】（CRUD+结构目标）【人员台账】（阶段筛选、新增；人员详情大弹窗：el-steps 五阶段+节点时间轴，节点办理按节点类型渲染表单——谈话/票决 7 数字实时红绿校验/公示日期差/学时/政审材料勾选；未满足前置条件按钮置灰并提示；审批按钮仅 R03）。
3. `membership/DuesPayment.vue`：Tab【党费基数】（列表+导入+调整，±20% 待审批 tag）【月度账单】（组织/年月选择、一键生成、跳过清单提示、批量确认 R12）【收缴登记】（登记弹窗含方式/凭证号/差异原因，会计出纳分设提示）【催缴提醒】（3 月预警/6 月告警 tag、生成处置工单）【补缴减免】（两个分区表单+审批 R03）【统计上缴】（收缴率卡片+趋势表+大额/5%上缴单+收据登记）。
4. `membership/DuesExpense.vue`：Tab【年度计划】【使用申请登记】（五类范围下拉、纪要附件必填、预算/余额实时提示、审批流）。
5. `membership/DuesPublication.vue`：年度公示单列表+新建（金额自动取数）+发布+公示待办提醒。
6. `partyAuth.js` 的 PARTY_ROLES 增加 R08/R12（不影响既有 can 判断）。

## 六、实施顺序（依赖序）

1. migration SQL + 执行 + schema.sql 同步 + 种子；PartyRole 加 R08/R12；重新打包重启 8150。
2. 党籍 Service/Controller（状态机+审批+证明）→ 冲烟。
3. 党费收缴 Service（比例引擎→基数→账单→收缴→减免补缴→催缴→上缴→统计）→ 冲烟。
4. 党费使用 + 公示 Service → 冲烟。
5. 发展党员 Service（节点注册表+五阶段+票决/时限/材料+看板）→ 冲烟（最复杂，放后端最后可复用前三个域的模式与数据）。
6. 前端 5 页面重写 + partyAuth 角色；GetDiagnostics。
7. 端到端验证（接口脚本全部分支 + 浏览器 5 页面角色显隐/关键弹窗/阻断提示）。

## 七、验证方案

- 后端：`mvn -q package -DskipTests` 重启 8150；Node/PowerShell 冲烟脚本覆盖：
  - 党籍状态机非法流转 400(E-MS-01)、无原因 400、未审批台账不变、审批后 member 状态回写、终态保护。
  - 比例引擎四档+离退休+固定额边界（3000/5000/10000 临界）；缺基数跳过；同人同月重复 E-DU-03；金额不符 E-DU-04；R99/R08 登记 403 矩阵；R12 确认人=登记人阻断；减免无支委会记录 E-DU-05；六大行校验。
  - 使用五类范围外 E-UE-01、无纪要 E-UE-02、超支 E-UE-04。
  - 发展：E-DEV-01/03/04/05/06/07/09/10/11/13 各构造 1 例 400；审批节点 R03 403 矩阵；转正完成后 party_member 回写；看板/超时接口 200。
- 前端：GetDiagnostics 0 错；浏览器 5 页面渲染、角色条新增 R08/R12、按钮显隐、工作台/向导弹窗与阻断 message。

## 八、风险与处理

- **发展党员流程极长**：以"代码内节点注册表 + 节点 payload_json 泛化"实现，避免为 ~25 个节点各建表/接口；节点差异数据集中在前端表单与 payload 校验。
- **E-DEV-04 口径矛盾**：正文写"5 个工作日"、异常表写"按自然日计算"——按异常表（显式校验规则）执行自然日 ≥5 天，并在代码注释与计划中标注，供用户后续裁定。
- **跨域联动**（党费 6 月未缴→党籍自行脱党；发展转正→party_member/party_membership 回写；停止党籍→评议/党费）：通过 Service 直接调用 Mapper 完成，不引入消息机制；全部包在事务内并写审计。
- **旧数据兼容**：仅 ALTER 加列带默认值，不删除/改名旧列；旧 PartyCrud 配置（entityConfig.js 中 5 个 cfgKey）不再被页面引用但保留文件不删。
- 不改动 org/member 等其他 9 域代码；改菜单结构非必须，故不触发 vite 重启要求（仅 HMR）。
