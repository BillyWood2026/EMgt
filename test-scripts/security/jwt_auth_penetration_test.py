#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
JWT Authentication Penetration Test
测试目标：验证JWT认证机制的安全性
包括：伪造Token、过期Token、篡改Token、越权访问
"""

import requests
import json
import time
import jwt
import base64
from datetime import datetime, timedelta
import urllib3

# 禁用SSL警告
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# 配置
BASE_URL = "http://localhost:5173"
IAM_SERVICE = "http://localhost:8081"
ORGMGT_SERVICE = "http://localhost:8082"
PERF_SERVICE = "http://localhost:8083"
SUCCESSION_SERVICE = "http://localhost:8085"

# JWT配置（从项目记忆中获取）
JWT_SECRET = "eOpt-IAM-JWT-Secret-Key-2024-At-Least-32-Bytes-Long"

class JWTPenetrationTest:
    def __init__(self):
        self.valid_token = None
        self.admin_token = None
        self.employee_token = None
        self.test_results = []

    def log_result(self, test_name, passed, details):
        """记录测试结果"""
        result = {
            "test_name": test_name,
            "passed": passed,
            "details": details,
            "timestamp": datetime.now().isoformat()
        }
        self.test_results.append(result)
        status = "✓ PASS" if passed else "✗ FAIL"
        print(f"{status} - {test_name}: {details}")

    def login_and_get_token(self, username, password):
        """登录获取有效Token"""
        url = f"{IAM_SERVICE}/api/auth/login"
        payload = {
            "username": username,
            "password": password
        }
        try:
            response = requests.post(url, json=payload, verify=False, timeout=10)
            if response.status_code == 200:
                data = response.json()
                return data.get("token") or data.get("data", {}).get("token")
        except Exception as e:
            print(f"Login failed: {e}")
        return None

    def test_1_forge_token(self):
        """测试1：使用伪造的JWT访问接口"""
        print("\n=== Test 1: Forged Token Access ===")

        # 创建伪造的Token（使用错误的密钥）
        forged_secret = "wrong-secret-key"
        payload = {
            "sub": "admin",
            "role": "ADMIN",
            "exp": datetime.utcnow() + timedelta(hours=1)
        }

        forged_token = jwt.encode(payload, forged_secret, algorithm="HS256")

        # 尝试访问薪资接口
        headers = {"Authorization": f"Bearer {forged_token}"}
        try:
            response = requests.get(
                f"{PERF_SERVICE}/api/salary/list",
                headers=headers,
                verify=False,
                timeout=10
            )

            # 应该返回401或403
            passed = response.status_code in [401, 403]
            self.log_result(
                "Forged Token Test",
                passed,
                f"Status: {response.status_code}, Expected: 401/403"
            )
        except Exception as e:
            self.log_result("Forged Token Test", False, f"Exception: {str(e)}")

    def test_2_expired_token(self):
        """测试2：使用过期的JWT访问接口"""
        print("\n=== Test 2: Expired Token Access ===")

        # 创建过期Token（1小时前过期）
        payload = {
            "sub": "admin",
            "role": "ADMIN",
            "exp": datetime.utcnow() - timedelta(hours=1),
            "iat": datetime.utcnow() - timedelta(hours=2)
        }

        expired_token = jwt.encode(payload, JWT_SECRET, algorithm="HS256")

        headers = {"Authorization": f"Bearer {expired_token}"}
        try:
            response = requests.get(
                f"{PERF_SERVICE}/api/salary/list",
                headers=headers,
                verify=False,
                timeout=10
            )

            passed = response.status_code in [401, 403]
            self.log_result(
                "Expired Token Test",
                passed,
                f"Status: {response.status_code}, Expected: 401/403"
            )
        except Exception as e:
            self.log_result("Expired Token Test", False, f"Exception: {str(e)}")

    def test_3_tampered_payload(self):
        """测试3：篡改JWT Payload"""
        print("\n=== Test 3: Tampered Payload ===")

        # 先获取合法Token
        if not self.admin_token:
            self.admin_token = self.login_and_get_token("admin", "admin123")

        if not self.admin_token:
            print("Failed to get valid token for tampering test")
            return

        # 解码并篡改Payload
        try:
            # 分割Token
            parts = self.admin_token.split('.')
            if len(parts) != 3:
                self.log_result("Tampered Payload Test", False, "Invalid token format")
                return

            # 解码Payload
            payload = json.loads(base64.urlsafe_b64decode(parts[1] + '=='))

            # 篡改角色（从USER改为ADMIN）
            payload['role'] = 'ADMIN'

            # 重新编码Payload（但不重新签名）
            tampered_payload = base64.urlsafe_b64encode(
                json.dumps(payload).encode()
            ).decode().rstrip('=')

            # 构造篡改的Token
            tampered_token = f"{parts[0]}.{tampered_payload}.{parts[2]}"

            # 尝试访问
            headers = {"Authorization": f"Bearer {tampered_token}"}
            response = requests.get(
                f"{PERF_SERVICE}/api/salary/list",
                headers=headers,
                verify=False,
                timeout=10
            )

            passed = response.status_code in [401, 403]
            self.log_result(
                "Tampered Payload Test",
                passed,
                f"Status: {response.status_code}, Expected: 401/403"
            )
        except Exception as e:
            self.log_result("Tampered Payload Test", False, f"Exception: {str(e)}")

    def test_4_horizontal_privilege_escalation(self):
        """测试4：水平越权访问"""
        print("\n=== Test 4: Horizontal Privilege Escalation ===")

        # 模拟普通员工登录
        if not self.employee_token:
            # 这里需要创建一个测试员工账号
            self.employee_token = self.login_and_get_token("employee1", "password123")

        if not self.employee_token:
            print("Skipping: No employee test account available")
            return

        # 尝试访问其他员工的绩效数据
        other_employee_id = 999  # 假设这是另一个员工的ID
        headers = {"Authorization": f"Bearer {self.employee_token}"}

        try:
            response = requests.get(
                f"{IAM_SERVICE}/api/performance/{other_employee_id}",
                headers=headers,
                verify=False,
                timeout=10
            )

            passed = response.status_code in [401, 403, 404]
            self.log_result(
                "Horizontal Privilege Escalation",
                passed,
                f"Status: {response.status_code}, Expected: 401/403/404"
            )
        except Exception as e:
            self.log_result("Horizontal Privilege Escalation", False, f"Exception: {str(e)}")

    def test_5_vertical_privilege_escalation(self):
        """测试5：垂直越权访问"""
        print("\n=== Test 5: Vertical Privilege Escalation ===")

        # 普通员工尝试访问管理员接口
        if not self.employee_token:
            self.employee_token = self.login_and_get_token("employee1", "password123")

        if not self.employee_token:
            print("Skipping: No employee test account available")
            return

        headers = {"Authorization": f"Bearer {self.employee_token}"}

        # 尝试访问用户管理接口（管理员功能）
        try:
            response = requests.get(
                f"{IAM_SERVICE}/api/user/list",
                headers=headers,
                verify=False,
                timeout=10
            )

            passed = response.status_code in [401, 403]
            self.log_result(
                "Vertical Privilege Escalation",
                passed,
                f"Status: {response.status_code}, Expected: 403"
            )
        except Exception as e:
            self.log_result("Vertical Privilege Escalation", False, f"Exception: {str(e)}")

    def test_6_null_token(self):
        """测试6：空Token或无效Token"""
        print("\n=== Test 6: Null/Invalid Token ===")

        test_cases = [
            ("Null Token", None),
            ("Empty Token", ""),
            ("Invalid Token", "invalid.token.here"),
            ("Bearer Only", "Bearer ")
        ]

        for test_name, token in test_cases:
            headers = {"Authorization": f"Bearer {token}"} if token else {}
            try:
                response = requests.get(
                    f"{PERF_SERVICE}/api/salary/list",
                    headers=headers,
                    verify=False,
                    timeout=10
                )

                passed = response.status_code in [401, 403]
                self.log_result(
                    test_name,
                    passed,
                    f"Status: {response.status_code}, Expected: 401/403"
                )
            except Exception as e:
                self.log_result(test_name, False, f"Exception: {str(e)}")

    def test_7_algorithm_confusion(self):
        """测试7：算法混淆攻击（none算法）"""
        print("\n=== Test 7: Algorithm Confusion (None Algorithm) ===")

        # 构造使用none算法的无签名Token
        payload = {
            "sub": "admin",
            "role": "ADMIN",
            "exp": datetime.utcnow() + timedelta(hours=1)
        }

        header = {"alg": "none", "typ": "JWT"}
        header_b64 = base64.urlsafe_b64encode(
            json.dumps(header).encode()
        ).decode().rstrip('=')

        payload_b64 = base64.urlsafe_b64encode(
            json.dumps(payload).encode()
        ).decode().rstrip('=')

        none_token = f"{header_b64}.{payload_b64}."

        headers = {"Authorization": f"Bearer {none_token}"}
        try:
            response = requests.get(
                f"{PERF_SERVICE}/api/salary/list",
                headers=headers,
                verify=False,
                timeout=10
            )

            passed = response.status_code in [401, 403]
            self.log_result(
                "Algorithm Confusion (None)",
                passed,
                f"Status: {response.status_code}, Expected: 401/403"
            )
        except Exception as e:
            self.log_result("Algorithm Confusion (None)", False, f"Exception: {str(e)}")

    def run_all_tests(self):
        """运行所有JWT认证穿透测试"""
        print("\n" + "="*60)
        print("JWT Authentication Penetration Testing Suite")
        print("="*60)

        self.test_1_forge_token()
        self.test_2_expired_token()
        self.test_3_tampered_payload()
        self.test_4_horizontal_privilege_escalation()
        self.test_5_vertical_privilege_escalation()
        self.test_6_null_token()
        self.test_7_algorithm_confusion()

        # 生成测试报告
        self.generate_report()

    def generate_report(self):
        """生成测试报告"""
        print("\n" + "="*60)
        print("Test Summary Report")
        print("="*60)

        total_tests = len(self.test_results)
        passed_tests = sum(1 for r in self.test_results if r['passed'])
        failed_tests = total_tests - passed_tests

        print(f"\nTotal Tests: {total_tests}")
        print(f"Passed: {passed_tests}")
        print(f"Failed: {failed_tests}")
        print(f"Pass Rate: {(passed_tests/total_tests*100):.2f}%")

        print("\nDetailed Results:")
        for result in self.test_results:
            status = "✓ PASS" if result['passed'] else "✗ FAIL"
            print(f"{status} - {result['test_name']}")
            print(f"    Details: {result['details']}")

        # 保存报告到文件
        report_file = "d:\\EMgt\\test-scripts\\security\\jwt_auth_test_report.json"
        with open(report_file, 'w', encoding='utf-8') as f:
            json.dump({
                "summary": {
                    "total": total_tests,
                    "passed": passed_tests,
                    "failed": failed_tests,
                    "pass_rate": f"{(passed_tests/total_tests*100):.2f}%"
                },
                "results": self.test_results
            }, f, indent=2, ensure_ascii=False)

        print(f"\nReport saved to: {report_file}")

if __name__ == "__main__":
    tester = JWTPenetrationTest()
    tester.run_all_tests()