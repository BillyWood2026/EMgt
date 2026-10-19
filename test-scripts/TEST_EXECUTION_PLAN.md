# EMgt系统全面测试执行计划

## 📋 测试概述

**项目名称**: EMgt企业管理系统全面测试  
**测试类型**: 安全测试、性能测试、并发测试、容错测试、兼容性测试、数据一致性测试、本地化测试、报表测试  
**执行周期**: 2026年7月31日 - 2026年8月15日（预计15个工作日）  
**测试环境**: 
- 前端：http://localhost:5173
- 后端：微服务集群（端口8081-8112）
- 数据库：MySQL 8.0 (utf8mb4)

---

## 🎯 测试目标

在现有边界条件测试、冒烟测试、测试用例测试和集成测试的基础上，补充以下8类测试：

1. **安全测试**: JWT认证、SQL注入、数据脱敏
2. **性能与压力测试**: 并发压力、数据库性能、JWT性能
3. **并发与事务一致性测试**: 乐观锁、悲观锁、事务回滚、分布式锁
4. **容错与熔断测试**: 服务降级、数据库故障恢复、外部接口Mock
5. **兼容性与多端测试**: 浏览器兼容性、分辨率适配
6. **数据一致性与回归测试**: 跨模块数据流、历史数据迁移、状态机流转
7. **本地化与编码测试**: 中文编码、多语言支持
8. **报表与边界数据测试**: 大数据量报表、极值边界、导出功能

---

## 📁 测试脚本目录结构

```
d:\EMgt\test-scripts\
├── security\                      # 安全测试
│   ├── jwt_auth_penetration_test.py
│   ├── sql_injection_test.py
│   ├── data_masking_test.py
│   └── reports\
├── performance\                   # 性能与压力测试
│   ├── stress_test.py
│   ├── jmeter_test_plan.jmx
│   └── reports\
├── concurrency\                   # 并发与事务一致性测试
│   ├── TransactionConcurrencyTest.java
│   └── reports\
├── fault-tolerance\              # 容错与熔断测试
│   ├── fault_tolerance_test.py
│   └── reports\
├── compatibility\                # 兼容性测试
│   ├── browser_compatibility_test.py
│   ├── screenshots\
│   └── reports\
├── data-consistency\             # 数据一致性测试
│   ├── data_consistency_test.py
│   └── reports\
├── localization\                 # 本地化与编码测试
│   ├── encoding_localization_test.py
│   └── reports\
├── reporting\                    # 报表与边界测试
│   ├── boundary_test.py
│   ├── exports\
│   └── reports\
└── TEST_EXECUTION_PLAN.md        # 本文档
```

---

## 🗓️ 测试执行计划

### 第一阶段：准备阶段（Day 1-2）

| 时间 | 任务 | 负责人 | 交付物 |
|------|------|--------|--------|
| Day 1 上午 | 环境检查与初始化 | 测试团队 | 环境检查清单 |
| Day 1 下午 | 安装测试依赖（Python库、JMeter、Selenium） | 测试团队 | 依赖安装确认 |
| Day 2 上午 | 数据库备份与测试数据准备 | DBA | 测试数据库快照 |
| Day 2 下午 | 测试脚本验证与调试 | 测试团队 | 脚本可执行确认 |

### 第二阶段：核心测试（Day 3-10）

#### Week 1: 安全与性能测试

| 时间 | 测试类型 | 执行脚本 | 预计耗时 |
|------|----------|----------|----------|
| Day 3 上午 | JWT认证穿透测试 | `jwt_auth_penetration_test.py` | 2小时 |
| Day 3 下午 | SQL注入测试 | `sql_injection_test.py` | 3小时 |
| Day 4 上午 | 数据脱敏测试 | `data_masking_test.py` | 2小时 |
| Day 4 下午 | 性能压力测试（Python版） | `stress_test.py` | 3小时 |
| Day 5 全天 | 性能压力测试（JMeter版） | `jmeter_test_plan.jmx` | 6小时 |

#### Week 2: 并发、容错与兼容性测试

| 时间 | 测试类型 | 执行脚本 | 预计耗时 |
|------|----------|----------|----------|
| Day 6 全天 | 并发与事务一致性测试 | `TransactionConcurrencyTest.java` | 6小时 |
| Day 7 上午 | 容错与熔断测试 | `fault_tolerance_test.py` | 3小时 |
| Day 7 下午 | 浏览器兼容性测试 | `browser_compatibility_test.py` | 3小时 |
| Day 8 上午 | 数据一致性测试 | `data_consistency_test.py` | 3小时 |
| Day 8 下午 | 本地化与编码测试 | `encoding_localization_test.py` | 2小时 |
| Day 9 全天 | 报表与边界测试 | `boundary_test.py` | 6小时 |

### 第三阶段：分析与报告（Day 11-15）

| 时间 | 任务 | 负责人 | 交付物 |
|------|------|--------|--------|
| Day 11-12 | 测试结果汇总与分析 | 测试团队 | 各类测试报告 |
| Day 13 | 缺陷分类与优先级排序 | 测试+开发 | 缺陷清单 |
| Day 14 | 编写综合测试报告 | 测试负责人 | 综合测试报告 |
| Day 15 | 测试评审会议 | 项目组 | 测试评审会议纪要 |

---

## 🔧 测试环境配置

### 1. Python环境

```bash
# 安装Python依赖
pip install requests urllib3 selenium webdriver-manager pillow

# 验证安装
python -c "import requests; import selenium; print('Python环境就绪')"
```

### 2. Java环境（并发测试）

```bash
# 编译Java测试类
cd d:\EMgt\test-scripts\concurrency
javac -cp ".;d:\EMgt\common\*" TransactionConcurrencyTest.java

# 运行测试（需要Spring Boot环境）
java -cp ".;d:\EMgt\common\*" org.junit.runner.JUnitCore TransactionConcurrencyTest
```

### 3. JMeter环境

```bash
# 安装JMeter（如果未安装）
# 下载：https://jmeter.apache.org/download_jmeter.cgi

# 运行JMeter测试计划（非GUI模式）
jmeter -n -t d:\EMgt\test-scripts\performance\jmeter_test_plan.jmx -l results.jtl -e -o report-dashboard
```

### 4. Selenium环境（浏览器测试）

```bash
# 安装浏览器驱动（自动管理）
pip install webdriver-manager

# 浏览器要求：
# - Chrome: 最新版本
# - Edge: 最新版本
# - Firefox: 最新版本
```

---

## 🚀 测试执行命令

### 安全测试套件

```bash
# 1. JWT认证穿透测试
cd d:\EMgt\test-scripts\security
python jwt_auth_penetration_test.py

# 2. SQL注入测试
python sql_injection_test.py

# 3. 数据脱敏测试
python data_masking_test.py
```

### 性能测试套件

```bash
# 1. Python压力测试
cd d:\EMgt\test-scripts\performance
python stress_test.py

# 2. JMeter压力测试（GUI模式）
jmeter -t jmeter_test_plan.jmx

# 3. JMeter压力测试（非GUI模式，生产环境推荐）
jmeter -n -t jmeter_test_plan.jmx -l results.jtl
```

### 并发测试套件

```bash
# Java并发测试（需要Maven）
cd d:\EMgt\test-scripts\concurrency
mvn test
```

### 其他测试套件

```bash
# 容错测试
cd d:\EMgt\test-scripts\fault-tolerance
python fault_tolerance_test.py

# 浏览器兼容性测试
cd d:\EMgt\test-scripts\compatibility
python browser_compatibility_test.py

# 数据一致性测试
cd d:\EMgt\test-scripts\data-consistency
python data_consistency_test.py

# 本地化与编码测试
cd d:\EMgt\test-scripts\localization
python encoding_localization_test.py

# 报表与边界测试
cd d:\EMgt\test-scripts\reporting
python boundary_test.py
```

---

## 📊 测试成功标准

### 安全测试
- JWT认证：所有伪造/过期/篡改Token应被拒绝（返回401/403）
- SQL注入：所有注入尝试应被拦截，不应返回数据库错误信息
- 数据脱敏：敏感数据（薪资、身份证、银行卡）应在API响应中脱敏

### 性能测试
- 并发登录：支持50并发用户，平均响应时间<500ms
- 数据库查询：慢查询（>3秒）应少于总查询的5%
- JWT验证：支持100并发验证，吞吐量>50 req/s

### 并发测试
- 乐观锁：多人同时修改同一记录，仅一人成功，其他人收到版本冲突提示
- 事务回滚：跨表写入失败时，所有操作应回滚，数据保持一致性

### 容错测试
- 服务降级：非核心服务故障时，核心业务功能应正常提供
- 数据库故障：连接超时后，系统应提供友好提示，不应返回500错误

### 兼容性测试
- 浏览器支持：Chrome、Edge、Firefox最新版应完全兼容
- 分辨率适配：1920x1080、1366x768、1280x720应无横向滚动条

### 数据一致性测试
- 跨模块数据流：员工从招聘入职到薪资计算的完整流程数据应一致
- 状态机流转：撤回-重新提交、驳回-修改等路径应正确执行

### 本地化测试
- 中文编码：所有中文字符（包括生僻字、Emoji）应正确存储和显示
- 多语言：支持中英文切换（如实现）

### 报表测试
- 大数据量：10000条记录的报表生成应<10秒
- 极值处理：薪资为0、负数、超大值应有合理的验证或处理

---

## 🐛 缺陷管理

### 缺陷优先级定义

| 级别 | 定义 | 示例 | 处理时限 |
|------|------|------|----------|
| P0 - 紧急 | 系统崩溃、安全漏洞、数据丢失 | SQL注入成功、JWT认证绕过 | 4小时内 |
| P1 - 高 | 核心功能失败、性能严重不达标 | 50并发下系统崩溃、事务不回滚 | 24小时内 |
| P2 - 中 | 次要功能异常、性能轻微不达标 | 薪资导出格式错误、响应时间稍慢 | 3个工作日 |
| P3 - 低 | 界面问题、文档错误、优化建议 | UI错位、帮助文档更新 | 下个版本 |

### 缺陷报告模板

```
缺陷标题: [模块名称] 简短描述
严重程度: P0/P1/P2/P3
测试类型: 安全/性能/并发/容错/兼容/数据一致性/本地化/报表
测试脚本: 测试脚本文件名
重现步骤:
  1. 步骤1
  2. 步骤2
  3. ...

预期结果: 应该发生什么
实际结果: 实际发生了什么
附件: 截图、日志、报告文件路径
```

---

## 📈 测试报告模板

测试完成后，每个测试类型应生成以下报告：

### 1. 执行报告（自动生成）

每个测试脚本会自动生成JSON格式的报告，包含：
- 测试总数、通过数、失败数
- 通过率
- 详细测试结果（每个测试用例的名称、状态、详情、时间戳）

### 2. 综合报告（手动编写）

测试负责人需编写综合报告，包含：
- 测试概述
- 测试环境
- 测试执行情况
- 测试结果汇总（8类测试的总体情况）
- 缺陷统计与分析
- 风险与建议
- 结论与下一步计划

---

## ⚠️ 测试注意事项

### 安全测试
- **警告**: SQL注入测试可能对数据库造成影响，建议在独立测试数据库执行
- **建议**: 测试前完整备份数据库

### 性能测试
- **注意**: 高并发测试可能导致服务响应变慢，建议在非高峰期执行
- **建议**: 监控服务器资源使用情况（CPU、内存、网络）

### 并发测试
- **注意**: 并发测试会产生测试数据，测试后需清理
- **建议**: 使用独立的测试数据库，避免影响生产数据

### 浏览器测试
- **要求**: 需要安装Chrome、Edge、Firefox最新版
- **注意**: 自动化测试会打开多个浏览器窗口

### 数据一致性测试
- **注意**: 会创建测试员工数据，测试后需清理
- **建议**: 在测试环境执行，避免污染生产数据

---

## 📞 联系方式

**测试负责人**: [姓名]  
**邮箱**: [邮箱地址]  
**紧急联系电话**: [电话号码]

**开发对接人**: [姓名]  
**邮箱**: [邮箱地址]

---

## 📚 参考资料

- [OWASP Top 10 安全风险](https://owasp.org/www-project-top-ten/)
- [JMeter最佳实践](https://jmeter.apache.org/usermanual/best-practices.html)
- [Selenium文档](https://www.selenium.dev/documentation/)
- [Spring Boot测试指南](https://spring.io/guides/gs/testing-web/)

---

**文档版本**: 1.0  
**最后更新**: 2026-07-31  
**编制**: EMgt测试团队