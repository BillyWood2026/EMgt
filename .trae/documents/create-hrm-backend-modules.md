# 创建10个HRM后端微服务模块

## Context

项目 `D:\EMgtv2` 已有15个微服务（战略/组织/流程等），前端 `ehrm/` 下有48个Vue页面覆盖10个HRM子模块，但后端微服务全部缺失。module_config数据库已注册这些模块但端口多指向不存在的服务。本计划创建10个后端微服务并纳入一键启动脚本。

## 标准模块结构（参照ProcessMgt/OrgMgt）

每个模块遵循统一结构：
```
{ModuleMgt}/
├── pom.xml                          # parent: eOpt:1.0.0, deps: spring-boot-starter-web, mybatis-plus-spring-boot3-starter:3.5.7, mysql-connector-j:8.2.0, common, lombok
├── src/main/java/com/example/{pkg}/
│   ├── {Module}Application.java     # @SpringBootApplication @MapperScan("com.example.{pkg}.mapper")
│   ├── controller/                  # @RestController @RequestMapping("/api/{path}")
│   ├── service/                     # extends IService<Entity>
│   ├── service/impl/                # extends ServiceImpl<Mapper,Entity> implements Service
│   ├── mapper/                      # extends BaseMapper<Entity> @Mapper
│   └── entity/                      # @TableName @TableId(AUTO) @TableLogic
└── src/main/resources/
    ├── application.yml              # server.port, datasource, mybatis-plus, profiles
    └── schema.sql                   # CREATE TABLE with deleted column
```

## 10个模块清单

| # | 模块名 | 包名 | 端口 | 数据库 | 前端目录 | 主要Entity/Controller |
|---|--------|------|------|--------|----------|---------------------|
| 1 | RecruitmentMgt | recruitment | 8086 | recruitment_mgt | recruiting/ | RecruitmentRequirement, Candidate, Interview, Offer |
| 2 | TalentMgt | talent | 8094 | talent_mgt | talent/ | TalentAssessment, EvaluationModel, TalentReview, SuccessionPlan, DevProject, DevTracking |
| 3 | EmployeeMgt | employee | 8083 | employee_mgt | staff-mgt/ | Employee, EmployeeProfile, Contract |
| 4 | TrainingMgt | training | 8087 | training_mgt | training-mgt/+learn-to-exam/ | TrainingCourse, TrainingPlan, TrainingRecord, OnlineCourse, OnlineExam, ExamResult |
| 5 | AttendanceMgt | attendance | 8084 | attendance_mgt | attend-holiday/ | Attendance, Leave, Overtime, LeaveBalance, HolidayPolicy |
| 6 | QualificationMgt | qualification | 8090 | qualification_mgt | qual-rank/ | Qualification, QualificationRank |
| 7 | SkillCertMgt | skillcert | 8093 | skill_cert_mgt | skill-certification/ | Skill, Certification, SkillApply, SkillCertificate, SkillStandard |
| 8 | SalaryMgt | salary | 8092 | salary_mgt | salary-mgt/ | SalaryStructure, SalaryPolicy, JobGrade, JobPosition, SalaryCalculation |
| 9 | PerfMgt | perf | 8085 | perf_mgt | perf-mgt/ | PerformanceEvaluation, Kpi, PerformanceResult |
| 10 | HRPlanningMgt | hrplanning | 8095 | hr_planning_mgt | hr-planning/ | HRPlan |

## 实现步骤

### Step 1: 创建10个模块的骨架文件
每个模块用general-purpose agent创建，每个agent负责1-2个模块：
- pom.xml (parent eOpt:1.0.0, 标准依赖)
- Application.java
- application.yml (端口、数据库连接、mybatis-plus配置)
- schema.sql (建表语句，含deleted列)

### Step 2: 创建Entity/Mapper/Service/ServiceImpl/Controller
按前端Vue页面定义的API路径创建REST端点：
- Controller: `@RestController @RequestMapping("/api/{module-path}")` + CRUD endpoints
- Service: `extends IService<Entity>` + 自定义查询方法
- ServiceImpl: `extends ServiceImpl<Mapper,Entity> implements Service`
- Mapper: `extends BaseMapper<Entity>`
- Entity: `@TableName @Data @TableId(AUTO) @TableLogic`

### Step 3: 创建数据库
用MySQL创建10个数据库并导入schema.sql

### Step 4: 构建验证
`mvn clean package -DskipTests` 构建全部10个模块

### Step 5: 纳入一键启动脚本
在 `start-all.ps1` 中：
- `$svcDefs` 添加10个模块（Wave 4 或扩展Wave 3）
- `$databases` 添加10个数据库名
- 健康检查矩阵添加10行

在 `stop-all.ps1` 中：
- `$allPorts` 添加10个端口

## 关键约束（来自project_memory）
- 新记录必须设置 deleted=0
- 数据库表必须有 deleted(默认0) 和 version(默认1) 列
- 须配置 MyBatis-Plus 乐观锁拦截器
- PUT/DELETE 端点必须实现
- 使用 `--spring.sql.init.mode=never` 启动
- JVM使用 `-Xmx160m`（业务模块）
- 后端授权使用 X-User-Id header

## 验证方式
1. `mvn clean package -DskipTests` 全部10个模块BUILD SUCCESS
2. 创建数据库并导入schema
3. 启动服务后 `curl http://localhost:{port}/api/{path}/list` 返回200
4. start-all.ps1 健康检查全部ALIVE
