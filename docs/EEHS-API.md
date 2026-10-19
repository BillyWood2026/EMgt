# EEHS 板块后端接口文档

EEHS 板块由 4 个独立微服务组成，前端统一通过 `/api/eehs` 前缀访问，Vite 代理按二级路径分流。

| 微服务 | 端口 | 数据库 | API 前缀 | 说明 |
|---|---|---|---|---|
| EnvironMgt | 8106 | env_mgt | `/api/eehs/env` | 环境管理 |
| SafetyMgt | 8107 | safety_mgt | `/api/eehs/safety` | 安全管理 |
| OccHealthMgt | 8108 | occ_health_mgt | `/api/eehs/health` | 职业健康管理 |
| EnergyMgt | 8109 | energy_mgt | `/api/eehs/energy` | 能源管理 |

## 通用约定

**统一响应体**（`ResponseResult<T>`）：

```json
{ "code": 200, "message": "success", "data": ... }
```

**每个业务实体均提供 5 个标准 CRUD 接口**（以 `/api/eehs/env/monitors` 为例）：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/{path}?keyword=` | 列表查询（keyword 可选，模糊匹配 name/title 字段，按 id 倒序） |
| GET | `/{path}/{id}` | 详情 |
| POST | `/{path}` | 新增 |
| PUT | `/{path}/{id}` | 更新 |
| DELETE | `/{path}/{id}` | 逻辑删除 |

**通用字段**：所有实体均含 `id`、`createTime`、`updateTime`（自动维护），`deleted` 为逻辑删除标记（不返回）。

**健康检查**：`GET /api/eehs/{env|safety|health|energy}/ping`

---

## 一、环境管理 EnvironMgt（8106）

### 1.1 环境监测 `env_monitor` → `/api/eehs/env/monitors`

| 字段 | 类型 | 说明 |
|---|---|---|
| monitorPoint | String | 监测点位 |
| pollutant | String | 污染物因子 |
| monitorValue | Decimal | 监测值 |
| standardValue | Decimal | 标准限值 |
| unit | String | 单位 |
| monitorTime | DateTime | 监测时间 |
| status | String | 状态（normal 正常 / exceeded 超标） |
| remark | String | 备注 |

### 1.2 排污管理 `env_emission` → `/api/eehs/env/emissions`

| 字段 | 类型 | 说明 |
|---|---|---|
| outletCode | String | 排口编码 |
| outletName | String | 排口名称 |
| pollutantType | String | 污染物类型 |
| permitValue | Decimal | 许可排放量 |
| actualValue | Decimal | 实际排放量 |
| unit | String | 单位 |
| dischargePeriod | String | 排放周期 |
| status | String | 状态（normal / exceeded 超许可） |

### 1.3 废弃物管理 `env_waste` → `/api/eehs/env/wastes`

| 字段 | 类型 | 说明 |
|---|---|---|
| wasteCode | String | 废弃物编码 |
| wasteName | String | 废弃物名称 |
| category | String | 类别（危险废物 / 一般固废） |
| quantity | Decimal | 数量 |
| unit | String | 单位 |
| disposeMethod | String | 处置方式 |
| vendor | String | 处置单位 |
| produceDate | Date | 产生日期 |
| status | String | 状态（stored 暂存 / disposed 已处置） |

### 1.4 环保政策 `env_policy` → `/api/eehs/env/policies`

| 字段 | 类型 | 说明 |
|---|---|---|
| title | String | 制度标题 |
| policyNo | String | 文号 |
| publishOrg | String | 发布机构 |
| publishDate | Date | 发布日期 |
| effectiveDate | Date | 生效日期 |
| content | Text | 正文 |
| status | String | 状态（draft / published / abolished） |

---

## 二、安全管理 SafetyMgt（8107）

### 2.1 安全检查 `safety_inspection` → `/api/eehs/safety/inspections`

| 字段 | 类型 | 说明 |
|---|---|---|
| inspectionName | String | 检查名称 |
| inspectionType | String | 类型（daily 日常 / special 专项 / quarterly 季度 / yearly 年度） |
| inspector | String | 检查人 |
| inspectDate | Date | 检查日期 |
| location | String | 检查地点 |
| result | String | 结果（qualified 合格 / rectifying 整改中 / unqualified 不合格） |
| issueDesc | String | 问题描述 |
| rectifyDeadline | Date | 整改期限 |
| status | String | 状态（open 未闭环 / closed 已闭环） |

### 2.2 事故管理 `safety_incident` → `/api/eehs/safety/incidents`

| 字段 | 类型 | 说明 |
|---|---|---|
| incidentNo | String | 事故编号 |
| incidentName | String | 事故名称 |
| level | String | 事故等级（一般/较大/重大/特别重大） |
| incidentType | String | 事故类型 |
| occurTime | DateTime | 发生时间 |
| location | String | 发生地点 |
| casualty | String | 伤亡情况 |
| economicLoss | Decimal | 经济损失（元） |
| description | Text | 事故描述 |
| status | String | 状态（reported / investigating / closed） |

### 2.3 安全制度 `safety_policy` → `/api/eehs/safety/policies`

| 字段 | 类型 | 说明 |
|---|---|---|
| title | String | 制度标题 |
| policyNo | String | 文号 |
| category | String | 制度分类 |
| publishOrg | String | 发布部门 |
| publishDate | Date | 发布日期 |
| effectiveDate | Date | 生效日期 |
| content | Text | 正文 |
| status | String | 状态（draft / published / abolished） |

### 2.4 安全培训 `safety_training` → `/api/eehs/safety/trainings`

| 字段 | 类型 | 说明 |
|---|---|---|
| trainingName | String | 培训名称 |
| trainer | String | 培训讲师 |
| trainDate | Date | 培训日期 |
| location | String | 培训地点 |
| participantCount | Integer | 参训人数 |
| content | Text | 培训内容 |
| result | String | 考核结果 |
| status | String | 状态（planned / done / cancelled） |

---

## 三、职业健康管理 OccHealthMgt（8108）

### 3.1 职业健康检查 `oh_examination` → `/api/eehs/health/examinations`

| 字段 | 类型 | 说明 |
|---|---|---|
| employeeName | String | 员工姓名 |
| dept | String | 部门 |
| post | String | 岗位 |
| examType | String | 检查类型（上岗前/在岗期间/离岗时） |
| examDate | Date | 检查日期 |
| hospital | String | 检查机构 |
| result | String | 检查结论 |
| hazardFactor | String | 危害因素 |
| nextExamDate | Date | 下次检查日期 |

### 3.2 健康档案 `oh_health_record` → `/api/eehs/health/records`

| 字段 | 类型 | 说明 |
|---|---|---|
| employeeName | String | 员工姓名 |
| dept | String | 部门 |
| post | String | 岗位 |
| gender | String | 性别 |
| age | Integer | 年龄 |
| hazardFactor | String | 接触危害因素 |
| examCount | Integer | 检查次数 |
| lastExamDate | Date | 最近检查日期 |
| status | String | 档案状态（normal / attention 需关注 / abnormal 异常） |
| remark | String | 备注 |

### 3.3 防护管理 `oh_protection` → `/api/eehs/health/protections`

| 字段 | 类型 | 说明 |
|---|---|---|
| facilityName | String | 防护设施/用品名称 |
| category | String | 类别（防护设施 / 防护用品） |
| post | String | 适用岗位 |
| standard | String | 配备标准 |
| quantity | Integer | 数量 |
| checkCycle | String | 检查周期 |
| lastCheckDate | Date | 最近检查日期 |
| status | String | 状态（normal / maintenance / scrapped） |
| remark | String | 备注 |

---

## 四、能源管理 EnergyMgt（8109）

### 4.1 能耗统计 `energy_consumption` → `/api/eehs/energy/consumptions`

| 字段 | 类型 | 说明 |
|---|---|---|
| statPeriod | String | 统计周期（如 2026-09） |
| energyType | String | 能源类型（电/水/天然气/蒸汽） |
| consumption | Decimal | 消耗量 |
| unit | String | 计量单位 |
| cost | Decimal | 费用（元） |
| dept | String | 使用部门 |
| standardCoal | Decimal | 折标煤（吨） |
| remark | String | 备注 |

### 4.2 节能项目 `energy_saving` → `/api/eehs/energy/savings`

| 字段 | 类型 | 说明 |
|---|---|---|
| projectName | String | 项目名称 |
| projectNo | String | 项目编号 |
| measure | String | 节能措施 |
| investAmount | Decimal | 投资额（元） |
| annualSaving | Decimal | 年节约金额（元） |
| paybackPeriod | Decimal | 投资回收期（年） |
| startDate | Date | 开始日期 |
| endDate | Date | 结束日期 |
| responsible | String | 责任人 |
| status | String | 状态（planning / implementing / completed） |

### 4.3 能源分析 `energy_analysis` → `/api/eehs/energy/analyses`

| 字段 | 类型 | 说明 |
|---|---|---|
| analysisPeriod | String | 分析周期 |
| energyType | String | 能源类型 |
| consumption | Decimal | 消耗量 |
| yoyRate | Decimal | 同比（%） |
| momRate | Decimal | 环比（%） |
| unitOutputConsumption | Decimal | 单位产值能耗 |
| targetValue | Decimal | 目标值 |
| conclusion | String | 分析结论 |

---

## 调用示例

```http
POST /api/eehs/safety/inspections
Content-Type: application/json

{
  "inspectionName": "10月综合安全检查",
  "inspectionType": "monthly",
  "inspector": "张工",
  "inspectDate": "2026-10-08",
  "location": "一号厂房",
  "result": "qualified",
  "status": "closed"
}
```

响应：

```json
{ "code": 200, "message": "success", "data": { "id": 1, "inspectionName": "10月综合安全检查", ... } }
```

> 前端封装见 `frontend/src/api/eehs.js`（`envApi` / `safetyApi` / `healthApi` / `energyApi`）。
