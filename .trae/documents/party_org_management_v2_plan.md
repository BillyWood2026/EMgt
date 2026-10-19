# 党组织管理子模块业务化改造（4.1–4.6）实施计划

## 一、调研结论

- 目标域：[PartyAffairsMgt/org](file:///d:/EMgtv3/PartyAffairsMgt/src/main/java/com/example/partyaffairsmgt/org)（端口8150，库 party_affairs_mgt），当前 6 个实体（party_org、term_election、org_life_meeting、democratic_appraisal、heart_talk、party_affair_publicity）均为通用 BaseCrudController + 前端通用 PartyCrud 页，无业务校验/流程/权限。
- 党员数据在 member 域 `party_member` 表（含 org_id、member_status 正式/预备/流动/停止党籍、join_date 入党日期），组织域通过只读统计 Mapper 跨包 COUNT，不反向改业务。
- 鉴权现状：[axiosConfig.js](file:///d:/EMgtv3/frontend/src/api/axiosConfig.js) 仅传 `X-User-Id`；dev 自动登录写入 role=ADMIN。全项目无 R02 等角色体系 → 本域自包含角色适配（请求头 `X-Party-Role`），不改 IAM/网关。
- 历史经验：改造限定在 party-mgt/org 目录与 org 后端包内；树组件统一事件模型（箭头展开/名称选中/按钮 stopPropagation）；不新增全局函数。
- 通用机制保留：其余 9 个功能域继续使用 PartyCrud；本域页面改为专用页面，旧薄页面文件直接覆盖/新增，菜单在 module.config.json 手工修订。

## 二、数据库改造（迁移脚本 v2 + 同步更新 schema.sql）

`party_org` 增列：org_code(唯一层级码)、approval_no 批复文号、term_no 届次、term_end 届满日、is_floating、party_groups(JSON 党小组)、remark、verify_flag 待核实标记、audit_status 待审核/已批复；org_type 枚举增加"临时党支部"；status=筹备/正常/换届中/合并/撤销。
`party_member` 增列：voting_right_suspended（停止表决权，默认0）。

新增表（12 张）：
- org_history 历史沿革（变更类型/前后值/批复文号/操作人/时间）
- org_merge_record 合并撤销与影响分析（影响党员数/下级数/在办流程/欠缴党费）
- term_candidate 候选人（得票、当选职务、党龄、排序）
- term_witness 监票/计票人
- meeting_material 会前材料（四类）
- meeting_absence 缺席及原因
- meeting_rectification 整改问题清单（时限/销号/佐证/提醒级别）
- appraisal_plan 评议计划（实名匿名、优秀比例参数、状态）
- appraisal_item 党员评议明细（预备党员标志、四维分、加权分、各阶段意见、等次调整理由）
- talk_plan / talk_coverage 谈话计划与应谈名单
- publicity_config 公开事项默认期限配置
- party_audit_log 导出/越权等审计日志

种子数据：根节点"中共××集团委员会"（编码01）+ 1个党总支 + 2个党支部 + 约12名党员（不同入党日期/状态，用于党员数、树角标、人员选择、选举党龄算法演示；可删除）。

## 三、后端（org 包内新增 service 层，保留实体/Mapper 模式）

- `common/party/`：PartyRole（R02/R03/R04/R07/R99 常量）、PartyAuthException + @RestControllerAdvice（code=403）、requireRole 工具；审计写入 AOP 风格手动调用。
- **PartyOrgService**：懒加载树（成员数实时 COUNT、届满状态徽标：临近180天黄/超期90天红、hasChildren）；搜索定位；详情聚合（基本/班子/党员/最新换届/组织生活完成情况/沿革）；编码生成器（上级编码.两位序号，唯一、不可改、撤销不重用 → E-ORG-01）；组织类型合规校验（100/50/7/3 人阈值，参数化，E-ORG-02 预警放行打 verify_flag、E-ORG-05 无党员预警）；上级类型兼容矩阵（E-ORG-04）；新建→待审核→R03批复登记文号→正常挂载+沿革留痕；合并/撤销影响分析与阻断（E-ORG-03）；拖拽改隶属；届满提醒列表（E-ORG-06）；CSV 导出（授权+审计）；Apache POI 5.2.5 实现 Excel 模板下载/导入预览（逐行错误 E-ORG-07）/选择性提交。
- **TermElectionService**：向导四步（方案/候选人/会议/备案）；E-ELEC-01 提前换届需批准件、E-ELEC-07 届期重叠；应到会=在册正式党员有表决权者；会议有效 实到>应到/2（参数）；票数 收回≤发出、有效+无效+弃权=收回；当选 赞成票>应到/2（E-ELEC-02/03/04）；候选人党龄 党委委员3年（央企/省属5年，参数）、支部书记1年（E-ELEC-05）；公示起止必填（E-ELEC-06）；备案通过后回写组织届次/任期（党委5年、总支/支部3年）与班子、写沿革。
- **OrgLifeMeetingService**：计划/发起/补录（注明原因）；"已召开"前置校验四类会前材料齐备、缺席必填原因（阻断）；整改清单 CRUD + 销号（结果+佐证）；到期提醒/超期30天升级；年度频次（各≥1，参数化）与召开率/按时率统计、季度末未开告警。
- **DemocraticAppraisalService**：计划→按在册党员（含预备）生成明细，预备党员不定等次阻断；自评/互评（计划实名匿名）/支评/党委审定/公示反馈分阶段权限；四维加权（30/20/30/20，参数）；优秀比例≤1/3 阻断（可附上级说明）；等次调整必填理由+审批；进度（自评率/互评率）、未参加名单、连续基本合格/不合格提醒。
- **HeartTalkService**：扩展谈话方式（个别/集体）、待解决问题、跟进人；按配置生成应谈名单（班子/分管、支委/党员每年≥1）；未覆盖清单与周提醒、重复对子非阻断提示、年底未覆盖考核提示。
- **PartyPublicityService**：事项默认期限配置；发布敏感信息校验（身份证/手机号未脱敏阻断）、范围越权阻断；默认期限自动到期（自动下架/归档/续期）；台账时限监控与超期未发布提醒。

## 四、前端（仅 party-mgt/org 目录；module.config.json 修订为14个菜单项）

新增 [partyAuth.js](file:///d:/EMgtv3/frontend/src/views/party-mgt)（角色读取/can()，ADMIN→R02，页头角色切换器写入 localStorage `party_role`）；axios 增传 `X-Party-Role` 请求头（全局仅加一个头，不影响其他模块）。

- 组织架构：`OrgStructure.vue`（左 el-tree 懒加载+类型图标+党员数角标+黄/红届满徽标+红点异常+搜索定位/类型筛选+面包屑+拖拽二次确认；右侧节点明细与操作）；`OrgDetail.vue`（Tab：基本信息/班子成员/党员列表/换届信息/组织生活/历史沿革）；`OrgListPage.vue`（同级表格、排序、分页、批量导出）；`MobileOrg.vue`（只读组织链）。
- 弹窗组件：OrgFormDialog（全字段、编码只读自动生成、树选择上级、党员中选书记、委员职务、党小组、届次/任期联动、流动标识）、OrgMergeDialog（影响党员数）、OrgRevokeDialog（阻断原因明细）、OrgImportDialog（模板下载、上传、错误行定位、勾选导入）、删除二次确认。
- 换届：`TermElection.vue` 年度计划列表+提醒弹窗；`ElectionWizard.vue` 四步向导（未完成不可下一步）：方案/候选人（拖拽排序、增删弹窗、党龄校验）/会议（选举方式、票数录入实时差额校验、监票计票人）/备案（公示区间强制勾选、批复件）。
- 生活会：`LifeMeeting.vue` 计划列表+发起/补录弹窗；`MeetingRecord.vue`（议程富文本、四类材料拖拽上传占位、缺席原因、整改清单表格）；`MeetingRectification.vue`（筛选/到期排序/一键提醒/销号弹窗）；`MeetingStats.vue` 召开率按时率+缺会告警。
- 评议：`DemocraticAppraisal.vue` 计划列表+发起/公示弹窗；`AppraisalWorkspace.vue` 进度看板（自评率/互评率/催办）、自评、批量互评打分、支评、党委审定、等次调整理由弹窗、预备党员拦截、优秀比例阻断提示。
- 谈话：`HeartTalk.vue` 计划+记录（重复提示）；`TalkCoverage.vue` 应谈/未覆盖名单。
- 公开：`Publicity.vue` 三 Tab（事项配置/发布[敏感词阻断、范围、方式、期限]/台账[时限监控、到期处理]）。

## 五、实施顺序

1. SQL 迁移+种子；schema.sql 同步；POI 依赖。
2. 后端公共角色/审计 → 组织架构（树/详情/编码/校验/审批/合并撤销/导入导出/提醒）。
3. 换届选举 → 生活会 → 民主评议 → 谈话 → 公开。
4. mvn package + 全量接口冒烟（重点：各 E-xxx 阻断/预警分支、算法边界值）。
5. 前端 partyAuth/axios → 组织三页五弹窗 → 换届向导 → 生活会/评议/谈话/公开。
6. 诊断 + 浏览器端到端冒烟（建组织、编码、树角标、选举票数校验、材料阻断、评议比例、公开敏感词拦截）。

## 六、验证要点

- 编码：根01→子01.01；重复/改码 400 阻断；撤销不重用。
- 党员数阈值：3/7/50/100 边界 E-ORG-02 预警可存；无党员 E-ORG-05。
- 届满：180天黄、超期红；合并撤销存在下级/欠费阻断并列明细。
- 选举：实到≤半数阻断；收回>发出阻断；当选票≤半数阻断；公示缺失阻断；备案回写任期。
- 生活会：缺材料/缺缺席原因不可登记已召开；整改超期升级。
- 评议：预备党员不定等次；优秀>1/3 阻断；调整无理由阻断。
- 权限：R99 只读、R07 仅党小组、写操作 R02/R04、审批 R03、导出审计有日志。
- 公开：身份证/手机号未脱敏阻断；到期自动处理。

## 七、风险

- 体量大（后端~40 文件、前端~20 文件）：分批实现，每批编译/接口验证；全部改动限定 org 域，不影响其余 9 域。
- POI 增重：仅 party 模块 pom 引入，不波及其他服务；导入失败返回明确行错误。
- 无真实用户体系：R 角色经请求头+前端切换器演示，预留后续接 IAM 角色映射。
