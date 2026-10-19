#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Data Consistency and Regression Testing Suite
测试目标：
1. 跨模块数据流测试：招聘→入职→绩效→薪资→继任
2. 历史数据迁移回归测试
3. 状态机流转测试
"""

import requests
import json
import time
import urllib3
from datetime import datetime, timedelta
import uuid

# 禁用SSL警告
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# 配置
SERVICES = {
    "RecruitmentMgt": "http://localhost:8088",
    "OrgMgt": "http://localhost:8082",
    "PerfMgt": "http://localhost:8086",
    "SalaryMgt": "http://localhost:8084",
    "SuccessPlanMgt": "http://localhost:8085",
    "IAM": "http://localhost:8081"
}

class DataConsistencyTest:
    def __init__(self):
        self.test_results = []
        self.token = None
        self.test_employee_id = None

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
        print(f"{status} | {test_name} | {details}")

    def login(self):
        """登录获取Token"""
        url = f"{SERVICES['IAM']}/api/auth/login"
        payload = {"username": "admin", "password": "admin123"}
        try:
            response = requests.post(url, json=payload, verify=False, timeout=10)
            if response.status_code == 200:
                data = response.json()
                self.token = data.get("token") or data.get("data", {}).get("token")
                return True
        except Exception as e:
            print(f"Login failed: {e}")
        return False

    def get_headers(self):
        """获取认证头"""
        return {"Authorization": f"Bearer {self.token}", "Content-Type": "application/json"}

    def test_1_cross_module_data_flow(self):
        """
        测试1：跨模块数据流测试
        流程：招聘入职 → 组织管理 → 绩效管理 → 薪资管理 → 继任计划
        """
        print("\n" + "="*60)
        print("Test 1: Cross-Module Data Flow Test")
        print("="*60)
        
        headers = self.get_headers()
        test_id = str(uuid.uuid4())[:8]
        employee_name = f"测试员工_{test_id}"
        
        # Step 1: 招聘管理 - 创建候选人
        print("\n[Step 1] Recruitment: Creating candidate...")
        
        candidate_data = {
            "name": employee_name,
            "position": "高级工程师",
            "phone": "13800138000",
            "email": f"test_{test_id}@example.com",
            "status": "INTERVIEWED"
        }
        
        try:
            response = requests.post(
                f"{SERVICES['RecruitmentMgt']}/api/candidate/create",
                json=candidate_data,
                headers=headers,
                verify=False,
                timeout=10
            )
            
            if response.status_code in [200, 201]:
                candidate_id = response.json().get("data", {}).get("id") or response.json().get("id")
                print(f"  候选人已创建: ID={candidate_id}")
                
                self.log_result(
                    "Recruitment - Create Candidate",
                    True,
                    f"Candidate created successfully: {candidate_id}"
                )
            else:
                self.log_result(
                    "Recruitment - Create Candidate",
                    False,
                    f"Failed with status {response.status_code}"
                )
                return
        
        except Exception as e:
            self.log_result("Recruitment - Create Candidate", False, f"Exception: {str(e)}")
            return
        
        # Step 2: 组织管理 - 入职
        print("\n[Step 2] Organization Management: Onboarding...")
        
        employee_data = {
            "employeeName": employee_name,
            "position": "高级工程师",
            "department": "技术部",
            "hireDate": datetime.now().strftime("%Y-%m-%d"),
            "status": "ACTIVE"
        }
        
        try:
            response = requests.post(
                f"{SERVICES['OrgMgt']}/api/employee/create",
                json=employee_data,
                headers=headers,
                verify=False,
                timeout=10
            )
            
            if response.status_code in [200, 201]:
                employee_data_response = response.json()
                self.test_employee_id = employee_data_response.get("data", {}).get("id") or employee_data_response.get("id")
                print(f"  员工已入职: ID={self.test_employee_id}")
                
                self.log_result(
                    "OrgMgt - Employee Onboarding",
                    True,
                    f"Employee onboarded successfully: {self.test_employee_id}"
                )
            else:
                self.log_result(
                    "OrgMgt - Employee Onboarding",
                    False,
                    f"Failed with status {response.status_code}"
                )
                return
        
        except Exception as e:
            self.log_result("OrgMgt - Employee Onboarding", False, f"Exception: {str(e)}")
            return
        
        # Step 3: 验证员工信息是否同步到绩效管理
        print("\n[Step 3] Verifying data sync to Performance Management...")
        
        time.sleep(2)  # 等待数据同步
        
        try:
            response = requests.get(
                f"{SERVICES['PerfMgt']}/api/performance/employee/{self.test_employee_id}",
                headers=headers,
                verify=False,
                timeout=10
            )
            
            if response.status_code == 200:
                self.log_result(
                    "PerfMgt - Data Sync Verification",
                    True,
                    "Employee data synced to Performance Management"
                )
            else:
                self.log_result(
                    "PerfMgt - Data Sync Verification",
                    False,
                    f"Employee not found in Performance Management: {response.status_code}"
                )
        
        except Exception as e:
            self.log_result("PerfMgt - Data Sync Verification", False, f"Exception: {str(e)}")
        
        # Step 4: 薪资管理 - 创建薪资记录
        print("\n[Step 4] Salary Management: Creating salary record...")
        
        salary_data = {
            "employeeId": self.test_employee_id,
            "baseSalary": 15000,
            "bonus": 3000,
            "effectiveDate": datetime.now().strftime("%Y-%m-%d")
        }
        
        try:
            response = requests.post(
                f"{SERVICES['SalaryMgt']}/api/salary/create",
                json=salary_data,
                headers=headers,
                verify=False,
                timeout=10
            )
            
            if response.status_code in [200, 201]:
                self.log_result(
                    "SalaryMgt - Create Salary Record",
                    True,
                    f"Salary record created for employee {self.test_employee_id}"
                )
            else:
                self.log_result(
                    "SalaryMgt - Create Salary Record",
                    False,
                    f"Failed with status {response.status_code}"
                )
        
        except Exception as e:
            self.log_result("SalaryMgt - Create Salary Record", False, f"Exception: {str(e)}")
        
        # Step 5: 继任计划 - 添加为继任候选人
        print("\n[Step 5] Succession Planning: Adding as candidate...")
        
        succession_data = {
            "employeeId": self.test_employee_id,
            "positionId": "POS001",
            "readinessLevel": "READY_2_YEARS",
            "assessmentDate": datetime.now().strftime("%Y-%m-%d")
        }
        
        try:
            response = requests.post(
                f"{SERVICES['SuccessPlanMgt']}/api/succession/candidate/add",
                json=succession_data,
                headers=headers,
                verify=False,
                timeout=10
            )
            
            if response.status_code in [200, 201]:
                self.log_result(
                    "SuccessionMgt - Add Candidate",
                    True,
                    f"Employee added as succession candidate"
                )
            else:
                self.log_result(
                    "SuccessionMgt - Add Candidate",
                    False,
                    f"Failed with status {response.status_code}"
                )
        
        except Exception as e:
            self.log_result("SuccessionMgt - Add Candidate", False, f"Exception: {str(e)}")

    def test_2_data_migration_regression(self):
        """
        测试2：历史数据迁移回归测试
        验证老系统数据迁移后的准确性
        """
        print("\n" + "="*60)
        print("Test 2: Data Migration Regression Test")
        print("="*60)
        
        headers = self.get_headers()
        
        # 测试历史员工数据查询
        print("\n查询历史员工数据...")
        
        test_cases = [
            ("历史员工查询", f"{SERVICES['OrgMgt']}/api/employee/list", {"hireDate": "2020-01-01"}),
            ("历史薪资查询", f"{SERVICES['SalaryMgt']}/api/salary/list", {"year": "2020"}),
            ("历史绩效查询", f"{SERVICES['PerfMgt']}/api/performance/list", {"year": "2020"})
        ]
        
        for test_name, endpoint, params in test_cases:
            try:
                response = requests.get(
                    endpoint,
                    params=params,
                    headers=headers,
                    verify=False,
                    timeout=10
                )
                
                if response.status_code == 200:
                    data = response.json()
                    count = len(data.get("data", [])) if isinstance(data.get("data"), list) else 0
                    
                    self.log_result(
                        f"Data Migration - {test_name}",
                        True,
                        f"Query successful, found {count} records"
                    )
                else:
                    self.log_result(
                        f"Data Migration - {test_name}",
                        False,
                        f"Query failed with status {response.status_code}"
                    )
            
            except Exception as e:
                self.log_result(f"Data Migration - {test_name}", False, f"Exception: {str(e)}")

    def test_3_state_machine_workflow(self):
        """
        测试3：状态机流转测试
        测试审批流程的各种状态流转
        """
        print("\n" + "="*60)
        print("Test 3: State Machine Workflow Test")
        print("="*60)
        
        headers = self.get_headers()
        test_id = str(uuid.uuid4())[:8]
        
        # 创建测试请假申请
        leave_data = {
            "applicantId": "EMP001",
            "leaveType": "年假",
            "startDate": (datetime.now() + timedelta(days=7)).strftime("%Y-%m-%d"),
            "endDate": (datetime.now() + timedelta(days=9)).strftime("%Y-%m-%d"),
            "reason": "测试请假申请",
            "status": "PENDING"
        }
        
        leave_id = None
        
        # 3.1 提交申请
        print("\n[3.1] 提交请假申请...")
        
        try:
            response = requests.post(
                f"{SERVICES['OrgMgt']}/api/leave/apply",
                json=leave_data,
                headers=headers,
                verify=False,
                timeout=10
            )
            
            if response.status_code in [200, 201]:
                leave_id = response.json().get("data", {}).get("id") or response.json().get("id")
                self.log_result(
                    "State Machine - Submit Application",
                    True,
                    f"Leave application submitted: {leave_id}"
                )
            else:
                self.log_result(
                    "State Machine - Submit Application",
                    False,
                    f"Failed with status {response.status_code}"
                )
                return
        
        except Exception as e:
            self.log_result("State Machine - Submit Application", False, f"Exception: {str(e)}")
            return
        
        if not leave_id:
            print("Failed to create leave application, skipping subsequent tests")
            return
        
        # 3.2 撤回申请
        print("\n[3.2] 测试撤回申请...")
        
        try:
            response = requests.post(
                f"{SERVICES['OrgMgt']}/api/leave/{leave_id}/withdraw",
                headers=headers,
                verify=False,
                timeout=10
            )
            
            if response.status_code == 200:
                # 检查状态是否变为WITHDRAWN
                status_response = requests.get(
                    f"{SERVICES['OrgMgt']}/api/leave/{leave_id}",
                    headers=headers,
                    verify=False,
                    timeout=10
                )
                
                if status_response.status_code == 200:
                    current_status = status_response.json().get("data", {}).get("status")
                    
                    if current_status == "WITHDRAWN":
                        self.log_result(
                            "State Machine - Withdraw Application",
                            True,
                            f"Application withdrawn successfully, status: {current_status}"
                        )
                    else:
                        self.log_result(
                            "State Machine - Withdraw Application",
                            False,
                            f"Unexpected status after withdrawal: {current_status}"
                        )
        
        except Exception as e:
            self.log_result("State Machine - Withdraw Application", False, f"Exception: {str(e)}")
        
        # 3.3 重新提交
        print("\n[3.3] 测试重新提交...")
        
        try:
            response = requests.post(
                f"{SERVICES['OrgMgt']}/api/leave/{leave_id}/resubmit",
                headers=headers,
                verify=False,
                timeout=10
            )
            
            if response.status_code == 200:
                status_response = requests.get(
                    f"{SERVICES['OrgMgt']}/api/leave/{leave_id}",
                    headers=headers,
                    verify=False,
                    timeout=10
                )
                
                if status_response.status_code == 200:
                    current_status = status_response.json().get("data", {}).get("status")
                    
                    if current_status == "PENDING":
                        self.log_result(
                            "State Machine - Resubmit Application",
                            True,
                            f"Application resubmitted successfully, status: {current_status}"
                        )
                    else:
                        self.log_result(
                            "State Machine - Resubmit Application",
                            False,
                            f"Unexpected status after resubmission: {current_status}"
                        )
        
        except Exception as e:
            self.log_result("State Machine - Resubmit Application", False, f"Exception: {str(e)}")
        
        # 3.4 驳回申请
        print("\n[3.4] 测试驳回申请...")
        
        try:
            reject_data = {
                "leaveId": leave_id,
                "reason": "测试驳回",
                "approver": "ADMIN"
            }
            
            response = requests.post(
                f"{SERVICES['OrgMgt']}/api/leave/{leave_id}/reject",
                json=reject_data,
                headers=headers,
                verify=False,
                timeout=10
            )
            
            if response.status_code == 200:
                status_response = requests.get(
                    f"{SERVICES['OrgMgt']}/api/leave/{leave_id}",
                    headers=headers,
                    verify=False,
                    timeout=10
                )
                
                if status_response.status_code == 200:
                    current_status = status_response.json().get("data", {}).get("status")
                    
                    if current_status == "REJECTED":
                        self.log_result(
                            "State Machine - Reject Application",
                            True,
                            f"Application rejected successfully, status: {current_status}"
                        )
                    else:
                        self.log_result(
                            "State Machine - Reject Application",
                            False,
                            f"Unexpected status after rejection: {current_status}"
                        )
        
        except Exception as e:
            self.log_result("State Machine - Reject Application", False, f"Exception: {str(e)}")

    def run_all_tests(self):
        """运行所有数据一致性测试"""
        print("\n" + "="*60)
        print("Data Consistency and Regression Testing Suite")
        print("="*60)
        
        # 先登录
        print("\nLogging in...")
        if not self.login():
            print("Failed to login. Tests cannot proceed.")
            return
        else:
            print("Login successful!")
        
        # 执行测试
        self.test_1_cross_module_data_flow()
        self.test_2_data_migration_regression()
        self.test_3_state_machine_workflow()
        
        # 生成报告
        self.generate_report()

    def generate_report(self):
        """生成测试报告"""
        print("\n" + "="*60)
        print("Data Consistency Test Summary Report")
        print("="*60)
        
        total_tests = len(self.test_results)
        passed_tests = sum(1 for r in self.test_results if r['passed'])
        failed_tests = total_tests - passed_tests
        
        print(f"\nTotal Tests: {total_tests}")
        print(f"Passed: {passed_tests}")
        print(f"Failed: {failed_tests}")
        print(f"Pass Rate: {(passed_tests/total_tests*100):.2f}%")
        
        # 保存报告
        report_file = "d:\\EMgt\\test-scripts\\data-consistency\\data_consistency_test_report.json"
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
    tester = DataConsistencyTest()
    tester.run_all_tests()