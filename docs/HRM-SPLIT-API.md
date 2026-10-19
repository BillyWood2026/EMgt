# HRM 拆分微服务后端接口文档

本文档覆盖从原 HRM 板块拆分出的 6 个独立微服务，前端统一通过 `/api` 前缀访问，Vite 代理按二级路径分流到各服务。

| 微服务 | 端口 | 数据库 | 包名 | API 前缀 | 说明 |
|---|---|---|---|---|---|
| HolidayMgt | 8190 | holiday_mgt | com.example.holiday | `/api/holiday` | 假期管理 |
| TalentReviewMgt | 8191 | talent_review_mgt | com.example.talentreview | `/api/talent/review`、`/api/evaluation` | 人才评审 |
| SuccessionPlanMgt | 8192 | succession_plan_mgt | com.example.succession | `/api/talent/succession` | 继任者计划 |
| TalentDevMgt | 8193 | talent_dev_mgt | com.example.talentdev | `/api/talent/dev-project`、`/api/talent/tracking` | 人才培养工程 |
| OnlineLearnMgt | 8194 | online_learn_mgt | com.example.onlinelearn | `/api/learn` | 在线学习 |
| OnlineExamMgt | 8195 | online_exam_mgt | com.example.onlineexam | `/api/learn/exams`、`/api/learnexam` | 在线考试 |

## 通用约定

**统一响应体**（`ResponseResult<T>`）：

```json
{ "code": 200, "message": "success", "data": ... }
```

HolidayMgt 部分列表接口使用 `PageResult<T>`，其在 `ResponseResult` 基础上额外平铺 `records / total / current / size / pages` 字段，前端可直接读取。

**公共字段**：所有实体均含 `id`（自增主键）、`createTime`、`updateTime`（自动维护）、`deleted`（逻辑删除标记，默认 0，不返回）、`version`（乐观锁，默认 1）。HolidayMgt 实体继承 `BaseEntity`，额外含 `createBy`、`updateBy`；其余服务实体在类内直接声明审计字段。

**列表查询**：多数列表接口为 `GET {path}/list`，支持 `pageNum`、`pageSize` 等可选查询参数；TalentReviewMgt / SuccessionPlanMgt / TalentDevMgt 的 `GET {path}` 或 `GET {path}/list` 返回全量数组。

**健康检查**：各服务均提供 `GET /actuator/health` 或对应入口类的健康端点（Spring Boot 默认）。

---

## 一、假期管理 HolidayMgt（8190）

### 1.1 请假申请 `holiday_leave_request` → `/api/holiday/request`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/list` | 分页查询（支持 pageNum / pageSize / requestStatus / employeeName / departmentId / leaveTypeCode） |
| POST | `/` | 新建请假申请（自动生成 requestNo，默认状态 PENDING） |
| DELETE | `/{id}` | 删除（逻辑删除） |
| POST | `/{id}/approve` 或 `/approve/{id}` | 审批通过，状态置为 APPROVED |
| POST | `/{id}/reject` 或 `/reject/{id}` | 审批驳回，状态置为 REJECTED |

**请求体字段表（新建/审批不涉及请求体时仅列出查询/新建相关）**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| requestNo | String | 否 | 请假单号，为空时自动生成 `LR-` 前缀 |
| employeeId | String | 是 | 工号 |
| employeeName | String | 是 | 员工姓名 |
| departmentId | String | 是 | 部门 |
| leaveTypeCode | String | 是 | 请假类型（personal 事假 / sick 病假 / annual 年假 / marriage 婚假 / maternity 产假 / compassionate 丧假） |
| startDate | Date | 是 | 开始日期 |
| endDate | Date | 是 | 结束日期 |
| totalDays | Decimal | 是 | 时长（天） |
| leaveReason | String | 否 | 请假原因 |
| requestStatus | String | 否 | 状态（DRAFT / PENDING / APPROVED / REJECTED / CANCELLED），默认 PENDING |
| approver | String | 否 | 审批人 |

---

### 1.2 假期余额 `holiday_balance` → `/api/holiday/balance`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | ``, `/list` | 分页查询（支持 pageNum / pageSize / balanceYear / employeeName / department / leaveTypeCode） |
| POST | `/adjust` | 调整余额（携带 id 则更新，否则新增；默认状态 ACTIVE） |

**请求体字段表**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| employeeId | String | 是 | 工号 |
| employeeName | String | 是 | 员工姓名 |
| department | String | 是 | 部门 |
| leaveTypeCode | String | 是 | 假期类型（annual 年假 / timeoff 调休 / sick 病假 / marriage 婚假 / maternity 产假 / compassionate 丧假） |
| availableBalance | Decimal | 是 | 总额度（天） |
| usedBalance | Decimal | 是 | 已用（天） |
| balanceYear | Integer | 是 | 生效年度 |
| balanceStatus | String | 否 | 状态（ACTIVE / EXPIRED / CARRYOVER），默认 ACTIVE |
| remark | String | 否 | 备注 |

---

### 1.3 假期政策 `holiday_type_rule` → `/api/holiday/type-rule/type`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/list` | 分页查询（支持 pageNum / pageSize / leaveTypeName / leaveTypeCode / applyTo / status） |
| POST | `/` | 新建假期政策（补全默认状态、适用层级等） |

**请求体字段表**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| leaveTypeCode | String | 是 | 假期类型编码 |
| leaveTypeName | String | 是 | 政策名称 |
| leaveCategory | String | 否 | 适用对象（all / regular / probation / manager） |
| applicableOrgLevel | String | 否 | 适用组织层级，列表回显 applyTo 读取该字段 |
| maxAnnualDays | Integer | 否 | 年度最大天数 |
| maxConsecutiveDays | Integer | 否 | 最大连续天数，默认 0 |
| salaryPaidFlag | Boolean | 否 | 是否带薪，默认 true |
| effectiveStartDate | Date | 否 | 生效日期 |
| effectiveEndDate | Date | 否 | 失效日期 |
| status | String | 否 | 状态（ACTIVE / INACTIVE / DRAFT），默认 ACTIVE |
| description | String | 否 | 政策说明 |

---

## 二、人才评审 TalentReviewMgt（8191）

### 2.1 人才评审 `talent_review` → `/api/talent/review`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | ``, `/list` | 列表查询 |
| GET | `/{id}` | 详情 |
| POST | `/` | 新增 |
| PUT | `/{id}` | 更新（id 在路径中） |
| DELETE | `/{id}` | 删除（逻辑删除） |

**请求体字段表**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| employeeId | Long | 是 | 员工 ID |
| employeeName | String | 是 | 员工姓名 |
| reviewPeriod | String | 否 | 评审周期 |
| reviewer | String | 否 | 评审人 |
| potential | String | 否 | 潜力评估 |
| performance | String | 否 | 绩效评估 |
| nineBox | String | 否 | 九宫格定位 |
| summary | String | 否 | 综合评语 |

---

### 2.2 特殊审批 `special_approval` → `/api/evaluation/special-approval`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/status/{status}` | 按状态分页标签查询（PENDING / IN_PROGRESS / APPROVED / REJECTED）；无法识别的 status 返回全部记录 |
| POST | `/` | 新建/保存审批（带 id 或已存在的 approvalId 时执行更新，幂等 upsert） |
| PUT | `/` | 编辑审批（id 在 body 中；前端亦可能只提交 approvalId 作为定位键） |

**请求体字段表**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| approvalId | String | 否 | 审批业务编号（RV 开头），前端以此作为列表行 id |
| employeeName | String | 是 | 申请人 / 评审对象姓名 |
| orgCode | String | 否 | 部门 / 组织 |
| approvalType | String | 否 | 审批/评审类型（promotion 晋升 / transfer 调岗 / talent 人才 / succession 继任） |
| reason | String | 否 | 审批事由 / 评审主题 |
| hrRecommendation | String | 否 | HR 建议 / 评审专家 |
| effectiveDate | Date | 否 | 生效日期 / 评审日期 |
| approvalStatus | String | 否 | 状态（PENDING / IN_PROGRESS / APPROVED / REJECTED），默认 PENDING |
| approver | String | 否 | 审批人 |
| approveTime | DateTime | 否 | 审批时间 |
| opinion | String | 否 | 审批意见 |

---

## 三、继任者计划 SuccessionPlanMgt（8192）

### 3.1 继任计划 `succession_plan` → `/api/talent/succession`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | ``, `/list` | 列表查询 |
| GET | `/{id}` | 详情 |
| POST | `/` | 新增 |
| PUT | `/{id}` | 更新（id 在路径中） |
| DELETE | `/{id}` | 删除（逻辑删除） |

**请求体字段表**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| positionId | Long | 否 | 岗位 ID |
| positionName | String | 是 | 岗位名称 |
| successorId | Long | 否 | 继任者 ID |
| successorName | String | 是 | 继任者姓名 |
| readiness | String | 否 | 准备度 |
| developmentPlan | String | 否 | 发展计划 |
| reviewDate | Date | 否 | 评审日期 |
| status | String | 否 | 状态 |

---

## 四、人才培养工程 TalentDevMgt（8193）

### 4.1 培养项目 `dev_project` → `/api/talent/dev-project`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | ``, `/list` | 列表查询 |
| GET | `/{id}` | 详情 |
| POST | `/` | 新增 |
| PUT | `/{id}` | 更新（id 在路径中） |
| DELETE | `/{id}` | 删除（逻辑删除） |

**请求体字段表**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| projectName | String | 是 | 项目名称 |
| projectType | String | 否 | 项目类型 |
| startDate | Date | 否 | 开始日期 |
| endDate | Date | 否 | 结束日期 |
| participantCount | Integer | 否 | 参与人数 |
| budget | Decimal | 否 | 预算（元） |
| description | String | 否 | 项目说明 |
| status | String | 否 | 状态 |

---

### 4.2 进度跟踪 `dev_tracking` → `/api/talent/tracking`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/list` | 列表查询 |
| GET | `/{id}` | 详情 |
| POST | `/` | 新增 |
| PUT | `/{id}` | 更新（id 在路径中） |
| DELETE | `/{id}` | 删除（逻辑删除） |

**请求体字段表**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| projectId | Long | 否 | 所属项目 ID |
| employeeId | Long | 否 | 员工 ID |
| employeeName | String | 是 | 员工姓名 |
| milestone | String | 否 | 里程碑 |
| plannedDate | Date | 否 | 计划日期 |
| actualDate | Date | 否 | 实际日期 |
| progress | Integer | 否 | 进度（百分比） |
| status | String | 否 | 状态 |
| comment | String | 否 | 备注 |

---

## 五、在线学习 OnlineLearnMgt（8194）

### 5.1 在线课程 `online_course` → `/api/learn/courses`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/list` | 列表查询（支持 pageNum / pageSize / name / category / required / status） |
| GET | `/{id}` | 详情 |
| POST | `/` | 新增 |
| PUT | `/` | 更新（id 在请求体中，路径无 `/{id}`） |
| DELETE | `/{id}` | 删除（逻辑删除） |

**请求体字段表**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| courseName | String | 是 | 课程名称 |
| category | String | 否 | 课程分类（professional 专业 / management 管理 / compliance 合规 / safety 安全 / general 通用） |
| duration | Integer | 否 | 学时 |
| credit | Decimal | 否 | 学分 |
| level | String | 否 | 难度（beginner 初级 / intermediate 中级 / advanced 高级） |
| required | String | 否 | 必修/选修（mandatory / elective） |
| teacher | String | 否 | 讲师 |
| onlineDate | Date | 否 | 上线日期 |
| description | String | 否 | 课程简介 |
| status | String | 否 | 状态（DRAFT / PUBLISHED / OFFLINE） |
| enrollCount | Integer | 否 | 学习人数 |

---

### 5.2 学习任务 `learn_task` → `/api/learn/tasks`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/list` | 列表查询（支持 pageNum / pageSize / employee / department / status） |
| GET | `/{id}` | 详情 |
| POST | `/` | 新增 |
| PUT | `/` | 更新（id 在请求体中，路径无 `/{id}`） |
| DELETE | `/{id}` | 删除（逻辑删除） |

**请求体字段表**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| employeeName | String | 是 | 员工姓名 |
| department | String | 否 | 部门 |
| courseName | String | 否 | 分配课程 |
| targetHours | Integer | 否 | 目标学时 |
| deadline | Date | 否 | 截止日期 |
| required | String | 否 | 是否必修（mandatory / elective） |
| remark | String | 否 | 学习备注 |
| courseCount | Integer | 否 | 已学课程数 |
| totalHours | Decimal | 否 | 累计学时 |
| credit | Decimal | 否 | 累计学分 |
| progress | Integer | 否 | 完成进度（百分比） |
| lastStudyTime | String | 否 | 最近学习时间 |
| status | String | 否 | 状态（NOT_STARTED / LEARNING / COMPLETED / OVERDUE） |

---

## 六、在线考试 OnlineExamMgt（8195）

### 6.1 在线考试 `online_exam` → `/api/learn/exams`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/list` | 列表查询（支持 pageNum / pageSize / name / type / status） |
| GET | `/{id}` | 详情 |
| POST | `/` | 新增 |
| PUT | `/` | 更新（id 在请求体中，路径无 `/{id}`） |
| DELETE | `/{id}` | 删除（逻辑删除） |

**请求体字段表**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| examName | String | 是 | 考试名称 |
| type | String | 否 | 考试类型（onboarding 入职 / promotion 晋升 / qualification 资格 / annual 年度 / special 专项） |
| duration | Integer | 否 | 考试时长（分钟） |
| totalScore | Integer | 否 | 总分 |
| passScore | Integer | 否 | 及格分 |
| examTime | String | 否 | 考试时间（YYYY-MM-DD HH:mm） |
| mode | String | 否 | 考试形式（closed_online 闭卷线上 / open_online 开卷线上 / offline 线下） |
| timeLimited | Boolean | 否 | 是否限时 |
| description | String | 否 | 考试说明 |
| status | String | 否 | 状态（PENDING / ONGOING / FINISHED / ARCHIVED） |
| examineeCount | Integer | 否 | 参考人数 |

---

### 6.2 考试评估 `exam_evaluation` → `/api/learnexam/evaluation`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/list` | 列表查询（支持 pageNum / pageSize / employee / examName / passed） |
| GET | `/{id}` | 详情 |
| POST | `/` | 新增 |
| PUT | `/` | 更新（id 在请求体中，路径无 `/{id}`） |
| DELETE | `/{id}` | 删除（逻辑删除） |
| POST | `/{id}/issue-certificate` | 发放证书：hasCertificate 置 1，certificateStatus 置 issued |

**请求体字段表**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| employeeName | String | 是 | 员工姓名 |
| department | String | 否 | 部门 |
| courseName | String | 否 | 考试名称（前端 courseName） |
| examTime | String | 否 | 考试时间（YYYY-MM-DD HH:mm） |
| totalScore | Integer | 否 | 总分 |
| passScore | Integer | 否 | 及格分 |
| overallScore | Decimal | 否 | 实际成绩 |
| hasCertificate | Integer | 否 | 是否已发证（0 否 / 1 是） |
| certificateStatus | String | 否 | 证书状态（issued / pending / none） |
| passRate | Integer | 否 | 通过率（百分比） |
| evaluationTime | String | 否 | 评估时间 |
| improvementSuggestions | String | 否 | 改进建议 |
| remark | String | 否 | 备注 |

---

## 调用示例

### 示例 1：新建请假申请

```http
POST http://localhost:8190/api/holiday/request
Content-Type: application/json

{
  "employeeId": "E10001",
  "employeeName": "张三",
  "departmentId": "研发部",
  "leaveTypeCode": "annual",
  "startDate": "2026-10-10",
  "endDate": "2026-10-12",
  "totalDays": 3,
  "leaveReason": "年假调休"
}
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "requestNo": "LR-1696789012345",
    "requestStatus": "PENDING",
    ...
  }
}
```

### 示例 2：按状态查询特殊审批

```http
GET http://localhost:8191/api/evaluation/special-approval/status/PENDING
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "approvalId": "RV-2026001",
      "employeeName": "李四",
      "approvalType": "promotion",
      "approvalStatus": "PENDING",
      ...
    }
  ]
}
```

### 示例 3：发放考试证书

```http
POST http://localhost:8195/api/learnexam/evaluation/5/issue-certificate
```

响应：

```json
{ "code": 200, "message": "success", "data": true }
```

> 前端封装参见各服务对应的前端 API 模块。
