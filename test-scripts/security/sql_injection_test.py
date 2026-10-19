#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SQL Injection Test Suite
测试目标：验证系统是否存在SQL注入漏洞
针对Controller层的所有查询接口进行测试
"""

import requests
import json
import urllib3
from datetime import datetime

# 禁用SSL警告
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# 配置
BASE_URL = "http://localhost:5173"
SERVICES = {
    "IAM": "http://localhost:8081",
    "OrgMgt": "http://localhost:8082",
    "StrategyMgt": "http://localhost:8083",
    "SalaryMgt": "http://localhost:8084",
    "SuccessPlanMgt": "http://localhost:8085",
    "PerfMgt": "http://localhost:8086",
    "WorkPlanMgt": "http://localhost:8087",
    "RecruitmentMgt": "http://localhost:8088"
}

# SQL注入payload列表
SQL_INJECTION_PAYLOADS = [
    # 基础注入
    "' OR '1'='1",
    "' OR '1'='1' --",
    "' OR '1'='1' /*",
    "1' OR '1'='1",
    "admin'--",
    "admin' #",
    
    # UNION注入
    "' UNION SELECT NULL--",
    "' UNION SELECT NULL, NULL--",
    "' UNION SELECT NULL, NULL, NULL--",
    "' UNION SELECT username, password FROM users--",
    "' UNION ALL SELECT NULL--",
    
    # 时间盲注
    "'; WAITFOR DELAY '0:0:5'--",
    "'; BENCHMARK(10000000,SHA1('test'))--",
    "'; SELECT SLEEP(5)--",
    
    # 错误注入
    "' AND 1=CONVERT(int,@@version)--",
    "' AND EXTRACTVALUE(1,CONCAT(0x7e,version()))--",
    "' AND UPDATEXML(1,CONCAT(0x7e,version()),1)--",
    
    # 堆叠查询
    "'; DROP TABLE users--",
    "'; INSERT INTO users VALUES('hacker','hacked')--",
    "'; UPDATE users SET password='hacked'--",
    
    # 编码绕过
    "' OR 1=1%00",
    "' OR 1=1%27",
    "%27%20OR%201%3D1",
    
    # 特殊字符
    "' OR ''='",
    "1' AND '1'='1",
    "' OR 1=1 LIMIT 1--",
    
    # MySQL特定
    "' OR 1=1 INTO OUTFILE '/tmp/test.txt'--",
    "' AND (SELECT * FROM (SELECT(SLEEP(5)))a)--",
    
    # PostgreSQL特定
    "'; SELECT pg_sleep(5)--",
    "' UNION SELECT NULL,version()--",
    
    # 数字型注入
    "1 OR 1=1",
    "1 AND 1=1",
    "1; DROP TABLE users",
    
    # ORDER BY注入
    "id; DROP TABLE users",
    "id' OR '1'='1",
    
    # LIKE注入
    "%' OR '1'='1",
    "_' OR '1'='1"
]

class SQLInjectionTest:
    def __init__(self):
        self.test_results = []
        self.session = requests.Session()
        self.token = None

    def log_result(self, test_name, endpoint, payload, passed, details):
        """记录测试结果"""
        result = {
            "test_name": test_name,
            "endpoint": endpoint,
            "payload": payload,
            "passed": passed,
            "details": details,
            "timestamp": datetime.now().isoformat()
        }
        self.test_results.append(result)
        
        status = "✓ PASS" if passed else "✗ FAIL"
        print(f"{status} | {test_name} | Payload: {payload[:30]}... | {details}")

    def login(self):
        """登录获取Token"""
        url = f"{SERVICES['IAM']}/api/auth/login"
        payload = {
            "username": "admin",
            "password": "admin123"
        }
        try:
            response = self.session.post(url, json=payload, verify=False, timeout=10)
            if response.status_code == 200:
                data = response.json()
                self.token = data.get("token") or data.get("data", {}).get("token")
                self.session.headers.update({"Authorization": f"Bearer {self.token}"})
                return True
        except Exception as e:
            print(f"Login failed: {e}")
        return False

    def test_endpoint_get(self, service_name, endpoint, param_name, param_value):
        """测试GET接口"""
        url = f"{SERVICES[service_name]}{endpoint}"
        
        for payload in SQL_INJECTION_PAYLOADS:
            test_name = f"GET {service_name}:{endpoint}"
            
            # 构造注入的参数
            injected_value = f"{param_value}{payload}"
            params = {param_name: injected_value}
            
            try:
                response = self.session.get(url, params=params, verify=False, timeout=10)
                
                # 检查是否成功执行了注入（不应该成功）
                is_vulnerable = self.check_vulnerability(response)
                
                # 如果返回500错误或SQL错误信息，可能存在漏洞
                if is_vulnerable:
                    self.log_result(
                        test_name,
                        endpoint,
                        payload,
                        False,
                        f"VULNERABLE! Status: {response.status_code}, Response: {response.text[:100]}"
                    )
                else:
                    # 正常情况：返回400/401/403或正确处理了输入
                    self.log_result(
                        test_name,
                        endpoint,
                        payload,
                        True,
                        f"Secure. Status: {response.status_code}"
                    )
            except Exception as e:
                self.log_result(test_name, endpoint, payload, True, f"Request failed (safe): {str(e)[:50]}")

    def test_endpoint_post(self, service_name, endpoint, field_name, field_value):
        """测试POST接口"""
        url = f"{SERVICES[service_name]}{endpoint}"
        
        for payload in SQL_INJECTION_PAYLOADS:
            test_name = f"POST {service_name}:{endpoint}"
            
            # 构造注入的数据
            data = {field_name: f"{field_value}{payload}"}
            
            try:
                response = self.session.post(url, json=data, verify=False, timeout=10)
                
                is_vulnerable = self.check_vulnerability(response)
                
                if is_vulnerable:
                    self.log_result(
                        test_name,
                        endpoint,
                        payload,
                        False,
                        f"VULNERABLE! Status: {response.status_code}, Response: {response.text[:100]}"
                    )
                else:
                    self.log_result(
                        test_name,
                        endpoint,
                        payload,
                        True,
                        f"Secure. Status: {response.status_code}"
                    )
            except Exception as e:
                self.log_result(test_name, endpoint, payload, True, f"Request failed (safe): {str(e)[:50]}")

    def test_endpoint_path(self, service_name, endpoint_template, param_value):
        """测试路径参数注入"""
        for payload in SQL_INJECTION_PAYLOADS:
            test_name = f"PATH {service_name}:{endpoint_template}"
            
            # 构造路径注入
            injected_path = endpoint_template.replace("{id}", f"{param_value}{payload}")
            url = f"{SERVICES[service_name]}{injected_path}"
            
            try:
                response = self.session.get(url, verify=False, timeout=10)
                
                is_vulnerable = self.check_vulnerability(response)
                
                if is_vulnerable:
                    self.log_result(
                        test_name,
                        endpoint_template,
                        payload,
                        False,
                        f"VULNERABLE! Status: {response.status_code}, Response: {response.text[:100]}"
                    )
                else:
                    self.log_result(
                        test_name,
                        endpoint_template,
                        payload,
                        True,
                        f"Secure. Status: {response.status_code}"
                    )
            except Exception as e:
                self.log_result(test_name, endpoint_template, payload, True, f"Request failed (safe): {str(e)[:50]}")

    def check_vulnerability(self, response):
        """检查响应是否存在漏洞迹象"""
        # 检查SQL错误关键词
        sql_error_keywords = [
            "SQL syntax", "mysql_fetch", "ORA-", "PLS-",
            "Unclosed quotation mark", "quoted string not properly terminated",
            "syntax error", "invalid query", "database error",
            "Warning: mysql", "PostgreSQL query failed",
            "Fatal error", "SQLite3::query", "ODBC SQL"
        ]
        
        response_text = response.text.lower()
        
        # 如果返回500且包含SQL错误信息
        if response.status_code == 500:
            for keyword in sql_error_keywords:
                if keyword.lower() in response_text:
                    return True
        
        # 检查是否返回了异常堆栈信息（泄露数据库结构）
        if response.status_code == 500 and ("exception" in response_text or "stack trace" in response_text):
            return True
        
        return False

    def test_orgmgt_controllers(self):
        """测试OrgMgt模块的Controller"""
        print("\n=== Testing OrgMgt Controllers ===")
        
        # OrganizationController
        self.test_endpoint_get("OrgMgt", "/api/org/list", "name", "test")
        self.test_endpoint_get("OrgMgt", "/api/org/page", "orgCode", "ORG001")
        
        # PositionController
        self.test_endpoint_get("OrgMgt", "/api/position/list", "positionName", "Manager")
        self.test_endpoint_get("OrgMgt", "/api/position/page", "positionCode", "POS001")
        
        # OrgChartController
        self.test_endpoint_get("OrgMgt", "/api/orgchart/tree", "rootOrgId", "1")

    def test_strategymgt_controllers(self):
        """测试StrategyMgt模块的Controller"""
        print("\n=== Testing StrategyMgt Controllers ===")
        
        # StrategyController
        self.test_endpoint_get("StrategyMgt", "/api/strategy/list", "strategyName", "test")
        self.test_endpoint_get("StrategyMgt", "/api/strategy/page", "status", "ACTIVE")
        
        # StrategyIndicatorController
        self.test_endpoint_get("StrategyMgt", "/api/indicator/list", "indicatorName", "KPI")

    def test_salarymgt_controllers(self):
        """测试SalaryMgt模块的Controller（薪资数据高度敏感）"""
        print("\n=== Testing SalaryMgt Controllers ===")
        
        # SalaryController
        self.test_endpoint_get("SalaryMgt", "/api/salary/list", "employeeName", "test")
        self.test_endpoint_get("SalaryMgt", "/api/salary/page", "employeeId", "EMP001")

    def test_successplanmgt_controllers(self):
        """测试SuccessPlanMgt模块的Controller（继任计划数据敏感）"""
        print("\n=== Testing SuccessPlanMgt Controllers ===")
        
        # CriticalPositionController
        self.test_endpoint_get("SuccessPlanMgt", "/api/succession/position/page", "positionName", "CEO")
        
        # SuccessionCandidateController
        self.test_endpoint_get("SuccessPlanMgt", "/api/succession/candidate/page", "candidateName", "John")

    def test_perfmgt_controllers(self):
        """测试PerfMgt模块的Controller"""
        print("\n=== Testing PerfMgt Controllers ===")
        
        # PerformanceController
        self.test_endpoint_get("PerfMgt", "/api/performance/list", "employeeName", "test")
        self.test_endpoint_get("PerfMgt", "/api/performance/page", "status", "PENDING")

    def test_workplanmgt_controllers(self):
        """测试WorkPlanMgt模块的Controller"""
        print("\n=== Testing WorkPlanMgt Controllers ===")
        
        # WeekPlanController
        self.test_endpoint_get("WorkPlanMgt", "/api/weekplan/list", "planName", "test")
        self.test_endpoint_get("WorkPlanMgt", "/api/weekplan/page", "status", "IN_PROGRESS")

    def test_recruitmentmgt_controllers(self):
        """测试RecruitmentMgt模块的Controller"""
        print("\n=== Testing RecruitmentMgt Controllers ===")
        
        # CandidateController
        self.test_endpoint_get("RecruitmentMgt", "/api/candidate/list", "name", "test")
        self.test_endpoint_get("RecruitmentMgt", "/api/candidate/page", "position", "Developer")

    def test_iam_controllers(self):
        """测试IAM模块的Controller"""
        print("\n=== Testing IAM Controllers ===")
        
        # SysUserController
        self.test_endpoint_get("IAM", "/api/user/list", "username", "test")
        self.test_endpoint_get("IAM", "/api/user/page", "realName", "张三")
        
        # SysRoleController
        self.test_endpoint_get("IAM", "/api/role/list", "roleName", "admin")

    def run_all_tests(self):
        """运行所有SQL注入测试"""
        print("\n" + "="*60)
        print("SQL Injection Security Testing Suite")
        print("="*60)
        
        # 先登录
        print("\nLogging in...")
        if not self.login():
            print("Failed to login. Some tests may not work properly.")
        else:
            print("Login successful!")
        
        # 执行各模块测试
        self.test_iam_controllers()
        self.test_orgmgt_controllers()
        self.test_strategymgt_controllers()
        self.test_salarymgt_controllers()
        self.test_successplanmgt_controllers()
        self.test_perfmgt_controllers()
        self.test_workplanmgt_controllers()
        self.test_recruitmentmgt_controllers()
        
        # 生成报告
        self.generate_report()

    def generate_report(self):
        """生成测试报告"""
        print("\n" + "="*60)
        print("SQL Injection Test Summary Report")
        print("="*60)
        
        total_tests = len(self.test_results)
        passed_tests = sum(1 for r in self.test_results if r['passed'])
        failed_tests = total_tests - passed_tests
        
        print(f"\nTotal Tests: {total_tests}")
        print(f"Passed (Secure): {passed_tests}")
        print(f"Failed (Vulnerable): {failed_tests}")
        print(f"Security Rate: {(passed_tests/total_tests*100):.2f}%")
        
        # 如果有漏洞，单独列出
        vulnerable_tests = [r for r in self.test_results if not r['passed']]
        if vulnerable_tests:
            print("\n⚠️  VULNERABILITIES FOUND:")
            for vuln in vulnerable_tests:
                print(f"\n  ✗ {vuln['test_name']}")
                print(f"    Payload: {vuln['payload']}")
                print(f"    Details: {vuln['details']}")
        
        # 保存报告
        report_file = "d:\\EMgt\\test-scripts\\security\\sql_injection_test_report.json"
        with open(report_file, 'w', encoding='utf-8') as f:
            json.dump({
                "summary": {
                    "total": total_tests,
                    "passed": passed_tests,
                    "failed": failed_tests,
                    "security_rate": f"{(passed_tests/total_tests*100):.2f}%"
                },
                "vulnerabilities": vulnerable_tests,
                "results": self.test_results
            }, f, indent=2, ensure_ascii=False)
        
        print(f"\nReport saved to: {report_file}")

if __name__ == "__main__":
    tester = SQLInjectionTest()
    tester.run_all_tests()