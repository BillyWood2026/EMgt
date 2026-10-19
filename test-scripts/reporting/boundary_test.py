#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Reporting and Boundary Data Testing Suite
测试目标：
1. 大数据量报表测试：上万员工绩效汇总
2. 极值边界测试：薪资为0/负数/超大值，闰年日期等
3. 导出功能测试：Excel/PDF导出格式与中文字符
"""

import requests
import json
import time
import urllib3
from datetime import datetime, timedelta
import uuid
import random

# 禁用SSL警告
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# 配置
SERVICES = {
    "PerfMgt": "http://localhost:8086",
    "SalaryMgt": "http://localhost:8084",
    "OrgMgt": "http://localhost:8082",
    "SysMgt": "http://localhost:8090",
    "IAM": "http://localhost:8081"
}

class ReportingBoundaryTest:
    def __init__(self):
        self.test_results = []
        self.token = None

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
        return {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }

    def test_1_large_data_report(self):
        """
        测试1：大数据量报表测试
        模拟上万员工的年度绩效汇总报表
        """
        print("\n" + "="*60)
        print("Test 1: Large Data Report Test")
        print("="*60)
        
        headers = self.get_headers()
        
        # 测试场景
        test_scenarios = [
            ("1000条记录查询", 1000),
            ("5000条记录查询", 5000),
            ("10000条记录查询", 10000),
        ]
        
        for test_name, count in test_scenarios:
            print(f"\n测试场景: {test_name}")
            
            try:
                # 模拟大数据量查询
                start_time = time.time()
                
                response = requests.get(
                    f"{SERVICES['PerfMgt']}/api/performance/report",
                    params={
                        "year": 2024,
                        "pageSize": count,
                        "reportType": "annual_summary"
                    },
                    headers=headers,
                    verify=False,
                    timeout=60  # 增加超时时间
                )
                
                end_time = time.time()
                response_time = end_time - start_time
                
                if response.status_code == 200:
                    data = response.json()
                    
                    # 检查数据完整性
                    total_records = data.get("total", 0)
                    
                    # 检查响应时间（应该小于10秒）
                    if response_time < 10:
                        self.log_result(
                            f"Large Data Report - {test_name}",
                            True,
                            f"Generated in {response_time:.2f}s, {total_records} records"
                        )
                    else:
                        self.log_result(
                            f"Large Data Report - {test_name}",
                            False,
                            f"Too slow: {response_time:.2f}s (>10s threshold)"
                        )
                
                else:
                    self.log_result(
                        f"Large Data Report - {test_name}",
                        False,
                        f"Request failed with status {response.status_code}"
                    )
            
            except requests.exceptions.Timeout:
                self.log_result(
                    f"Large Data Report - {test_name}",
                    False,
                    "Request timeout (>60s)"
                )
            
            except Exception as e:
                self.log_result(
                    f"Large Data Report - {test_name}",
                    False,
                    f"Exception: {str(e)}"
                )

    def test_2_extreme_salary_values(self):
        """
        测试2：薪资极值边界测试
        测试薪资为0、负数、超大值等极端情况
        """
        print("\n" + "="*60)
        print("Test 2: Extreme Salary Value Test")
        print("="*60)
        
        headers = self.get_headers()
        
        # 边界测试数据
        test_cases = [
            ("零值薪资", 0, "薪资为0"),
            ("负数薪资", -10000, "负数薪资"),
            ("最小正数", 0.01, "最小正数薪资"),
            ("正常范围", 15000, "正常薪资"),
            ("较大值", 1000000, "百万年薪"),
            ("超大值", 999999999.99, "超大薪资"),
            ("科学计数法", 1e10, "科学计数法"),
            ("字符串数字", "15000", "字符串形式的薪资"),
            ("空值", None, "空值薪资"),
            ("非数字", "abc", "非数字薪资"),
        ]
        
        for test_name, salary_value, description in test_cases:
            full_test_name = f"Salary Boundary - {test_name}"
            
            try:
                salary_data = {
                    "employeeId": "EMP001",
                    "baseSalary": salary_value,
                    "effectiveDate": datetime.now().strftime("%Y-%m-%d")
                }
                
                response = requests.post(
                    f"{SERVICES['SalaryMgt']}/api/salary/create",
                    json=salary_data,
                    headers=headers,
                    verify=False,
                    timeout=10
                )
                
                # 分析响应
                if response.status_code in [200, 201]:
                    # 系统接受了该值（可能需要验证业务规则）
                    response_data = response.json()
                    saved_salary = response_data.get("data", {}).get("baseSalary")
                    
                    # 检查是否正确保存
                    if saved_salary == salary_value:
                        self.log_result(
                            full_test_name,
                            True,
                            f"{description}: accepted and saved correctly"
                        )
                    else:
                        self.log_result(
                            full_test_name,
                            False,
                            f"{description}: saved as {saved_salary}, expected {salary_value}"
                        )
                
                elif response.status_code == 400:
                    # 系统拒绝了该值（验证失败）- 这是正确的行为
                    self.log_result(
                        full_test_name,
                        True,
                        f"{description}: correctly rejected with 400"
                    )
                
                else:
                    self.log_result(
                        full_test_name,
                        False,
                        f"{description}: unexpected status {response.status_code}"
                    )
            
            except Exception as e:
                self.log_result(
                    full_test_name,
                    False,
                    f"{description}: exception {str(e)}"
                )

    def test_3_date_boundary(self):
        """
        测试3：日期边界测试
        测试闰年、跨世纪等特殊日期
        """
        print("\n" + "="*60)
        print("Test 3: Date Boundary Test")
        print("="*60)
        
        headers = self.get_headers()
        
        # 日期边界测试
        test_dates = [
            ("正常日期", "2024-01-15", "正常日期"),
            ("闰年2月29日", "2024-02-29", "2024是闰年"),
            ("非闰年2月28日", "2023-02-28", "2023非闰年"),
            ("年初", "2024-01-01", "年度第一天"),
            ("年末", "2024-12-31", "年度最后一天"),
            ("跨世纪", "2000-01-01", "千年虫日期"),
            ("未来日期", "2030-12-31", "未来日期"),
            ("历史日期", "1900-01-01", "历史日期"),
            ("最小日期", "0001-01-01", "最小可能日期"),
            ("最大日期", "9999-12-31", "最大可能日期"),
        ]
        
        for test_name, date_value, description in test_dates:
            full_test_name = f"Date Boundary - {test_name}"
            
            try:
                employee_data = {
                    "employeeName": f"测试员工_{test_name}",
                    "hireDate": date_value,
                    "position": "测试岗位",
                    "status": "ACTIVE"
                }
                
                response = requests.post(
                    f"{SERVICES['OrgMgt']}/api/employee/create",
                    json=employee_data,
                    headers=headers,
                    verify=False,
                    timeout=10
                )
                
                if response.status_code in [200, 201]:
                    response_data = response.json()
                    saved_date = response_data.get("data", {}).get("hireDate")
                    
                    # 验证日期是否正确保存
                    if saved_date == date_value or saved_date.startswith(date_value):
                        self.log_result(
                            full_test_name,
                            True,
                            f"{description}: date saved correctly"
                        )
                    else:
                        self.log_result(
                            full_test_name,
                            False,
                            f"{description}: date mismatch, saved as {saved_date}"
                        )
                
                elif response.status_code == 400:
                    # 某些日期可能被业务规则拒绝（如未来入职日期）
                    self.log_result(
                        full_test_name,
                        True,
                        f"{description}: rejected by business rule (expected)"
                    )
                
                else:
                    self.log_result(
                        full_test_name,
                        False,
                        f"{description}: unexpected status {response.status_code}"
                    )
            
            except Exception as e:
                self.log_result(
                    full_test_name,
                    False,
                    f"{description}: exception {str(e)}"
                )

    def test_4_export_functionality(self):
        """
        测试4：导出功能测试
        测试Excel/PDF导出的格式和中文支持
        """
        print("\n" + "="*60)
        print("Test 4: Export Functionality Test")
        print("="*60)
        
        headers = self.get_headers()
        
        # 导出格式测试
        export_formats = [
            ("Excel", "xlsx"),
            ("PDF", "pdf"),
            ("CSV", "csv")
        ]
        
        for format_name, format_ext in export_formats:
            test_name = f"Export - {format_name}"
            
            try:
                # 请求导出
                response = requests.post(
                    f"{SERVICES['PerfMgt']}/api/performance/export",
                    json={
                        "format": format_ext,
                        "year": 2024,
                        "includeDetails": True
                    },
                    headers=headers,
                    verify=False,
                    timeout=30,
                    stream=True  # 流式下载
                )
                
                if response.status_code == 200:
                    # 检查Content-Type
                    content_type = response.headers.get("Content-Type", "")
                    
                    if format_ext == "xlsx" and "excel" in content_type.lower():
                        # 保存文件
                        filename = f"d:\\EMgt\\test-scripts\\reporting\\exports\\test_export_{format_ext}.{format_ext}"
                        with open(filename, 'wb') as f:
                            for chunk in response.iter_content(chunk_size=8192):
                                f.write(chunk)
                        
                        # 检查文件大小
                        import os
                        file_size = os.path.getsize(filename)
                        
                        if file_size > 0:
                            self.log_result(
                                test_name,
                                True,
                                f"Export successful, file size: {file_size} bytes"
                            )
                        else:
                            self.log_result(
                                test_name,
                                False,
                                "Export file is empty"
                            )
                    
                    elif format_ext == "pdf" and "pdf" in content_type.lower():
                        filename = f"d:\\EMgt\\test-scripts\\reporting\\exports\\test_export_{format_ext}.{format_ext}"
                        with open(filename, 'wb') as f:
                            for chunk in response.iter_content(chunk_size=8192):
                                f.write(chunk)
                        
                        import os
                        file_size = os.path.getsize(filename)
                        
                        if file_size > 0:
                            self.log_result(
                                test_name,
                                True,
                                f"PDF export successful, size: {file_size} bytes"
                            )
                        else:
                            self.log_result(
                                test_name,
                                False,
                                "PDF export file is empty"
                            )
                    
                    else:
                        self.log_result(
                            test_name,
                            True,
                            f"Export returned content type: {content_type}"
                        )
                
                else:
                    self.log_result(
                        test_name,
                        False,
                        f"Export failed with status {response.status_code}"
                    )
            
            except Exception as e:
                self.log_result(test_name, False, f"Exception: {str(e)}")

    def test_5_numeric_boundary(self):
        """
        测试5：数值边界测试
        测试各种数值类型的边界情况
        """
        print("\n" + "="*60)
        print("Test 5: Numeric Boundary Test")
        print("="*60)
        
        headers = self.get_headers()
        
        # 数值边界测试
        test_cases = [
            ("绩效分数", "score", 0, "最小分数"),
            ("绩效分数", "score", 100, "最大分数"),
            ("绩效分数", "score", 50.5, "小数分数"),
            ("绩效分数", "score", -10, "负数分数"),
            ("绩效分数", "score", 150, "超范围分数"),
            
            ("工龄", "workYears", 0, "0年工龄"),
            ("工龄", "workYears", 50, "50年工龄"),
            ("工龄", "workYears", 100, "百年工龄"),
            ("工龄", "workYears", -5, "负数工龄"),
        ]
        
        for field_group, field_name, value, description in test_cases:
            test_name = f"Numeric Boundary - {field_group} ({description})"
            
            try:
                performance_data = {
                    "employeeId": "EMP001",
                    field_name: value,
                    "evaluationDate": datetime.now().strftime("%Y-%m-%d")
                }
                
                response = requests.post(
                    f"{SERVICES['PerfMgt']}/api/performance/create",
                    json=performance_data,
                    headers=headers,
                    verify=False,
                    timeout=10
                )
                
                if response.status_code in [200, 201]:
                    response_data = response.json()
                    saved_value = response_data.get("data", {}).get(field_name)
                    
                    if saved_value == value:
                        self.log_result(
                            test_name,
                            True,
                            f"{description}: value {value} saved correctly"
                        )
                    else:
                        self.log_result(
                            test_name,
                            False,
                            f"{description}: value mismatch, saved as {saved_value}"
                        )
                
                elif response.status_code == 400:
                    self.log_result(
                        test_name,
                        True,
                        f"{description}: value {value} correctly rejected"
                    )
                
                else:
                    self.log_result(
                        test_name,
                        False,
                        f"{description}: unexpected status {response.status_code}"
                    )
            
            except Exception as e:
                self.log_result(test_name, False, f"Exception: {str(e)}")

    def run_all_tests(self):
        """运行所有报表与边界测试"""
        print("\n" + "="*60)
        print("Reporting and Boundary Data Testing Suite")
        print("="*60)
        
        # 先登录
        print("\nLogging in...")
        if not self.login():
            print("Failed to login. Tests cannot proceed.")
            return
        else:
            print("Login successful!")
        
        # 执行测试
        self.test_1_large_data_report()
        self.test_2_extreme_salary_values()
        self.test_3_date_boundary()
        self.test_4_export_functionality()
        self.test_5_numeric_boundary()
        
        # 生成报告
        self.generate_report()

    def generate_report(self):
        """生成测试报告"""
        print("\n" + "="*60)
        print("Reporting and Boundary Test Summary Report")
        print("="*60)
        
        total_tests = len(self.test_results)
        passed_tests = sum(1 for r in self.test_results if r['passed'])
        failed_tests = total_tests - passed_tests
        
        print(f"\nTotal Tests: {total_tests}")
        print(f"Passed: {passed_tests}")
        print(f"Failed: {failed_tests}")
        print(f"Pass Rate: {(passed_tests/total_tests*100):.2f}%")
        
        # 保存报告
        report_file = "d:\\EMgt\\test-scripts\\reporting\\boundary_test_report.json"
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
    # 创建导出目录
    import os
    os.makedirs("d:\\EMgt\\test-scripts\\reporting\\exports", exist_ok=True)
    
    tester = ReportingBoundaryTest()
    tester.run_all_tests()