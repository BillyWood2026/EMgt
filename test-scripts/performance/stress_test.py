#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Performance and Load Testing Suite
测试目标：
1. 并发压力测试：模拟多用户并发访问
2. 数据库连接池与慢查询测试
3. JWT签发与验证压力测试
使用工具：Python + requests + threading + time
"""

import requests
import json
import time
import threading
import urllib3
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed
from collections import defaultdict
import statistics

# 禁用SSL警告
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# 配置
SERVICES = {
    "IAM": "http://localhost:8081",
    "OrgMgt": "http://localhost:8082",
    "StrategyMgt": "http://localhost:8083",
    "SalaryMgt": "http://localhost:8084",
    "PerfMgt": "http://localhost:8086",
    "WorkPlanMgt": "http://localhost:8087",
    "RecruitmentMgt": "http://localhost:8088"
}

class PerformanceTest:
    def __init__(self):
        self.results = defaultdict(list)
        self.lock = threading.Lock()
        self.tokens = []

    def login(self, username, password):
        """登录获取Token"""
        url = f"{SERVICES['IAM']}/api/auth/login"
        payload = {"username": username, "password": password}
        try:
            response = requests.post(url, json=payload, verify=False, timeout=10)
            if response.status_code == 200:
                data = response.json()
                return data.get("token") or data.get("data", {}).get("token")
        except:
            pass
        return None

    def make_request(self, endpoint, method="GET", data=None, headers=None):
        """发起请求并记录性能数据"""
        start_time = time.time()
        
        try:
            if method == "GET":
                response = requests.get(endpoint, headers=headers, verify=False, timeout=30)
            elif method == "POST":
                response = requests.post(endpoint, json=data, headers=headers, verify=False, timeout=30)
            
            end_time = time.time()
            response_time = (end_time - start_time) * 1000  # 转换为毫秒
            
            return {
                "success": True,
                "status_code": response.status_code,
                "response_time": response_time,
                "endpoint": endpoint
            }
        except Exception as e:
            end_time = time.time()
            response_time = (end_time - start_time) * 1000
            return {
                "success": False,
                "error": str(e),
                "response_time": response_time,
                "endpoint": endpoint
            }

    def test_concurrent_login(self, num_users=50):
        """
        测试1：并发登录压力测试
        模拟50个用户同时登录，测试JWT签发性能
        """
        print("\n" + "="*60)
        print(f"Test 1: Concurrent Login Stress Test ({num_users} users)")
        print("="*60)
        
        def login_task(user_id):
            # 模拟不同的用户登录
            username = f"user{user_id}"
            password = "password123"
            
            result = self.make_request(
                f"{SERVICES['IAM']}/api/auth/login",
                method="POST",
                data={"username": username, "password": password}
            )
            
            with self.lock:
                self.results["login"].append(result)
            
            return result
        
        # 并发执行登录
        start_time = time.time()
        
        with ThreadPoolExecutor(max_workers=num_users) as executor:
            futures = [executor.submit(login_task, i) for i in range(num_users)]
            results = [future.result() for future in as_completed(futures)]
        
        end_time = time.time()
        total_time = end_time - start_time
        
        # 分析结果
        successful_logins = [r for r in results if r.get("success")]
        response_times = [r["response_time"] for r in successful_logins]
        
        print(f"\nTotal Time: {total_time:.2f}s")
        print(f"Successful Logins: {len(successful_logins)}/{num_users}")
        print(f"Avg Response Time: {statistics.mean(response_times):.2f}ms" if response_times else "N/A")
        print(f"Max Response Time: {max(response_times):.2f}ms" if response_times else "N/A")
        print(f"Min Response Time: {min(response_times):.2f}ms" if response_times else "N/A")
        print(f"Throughput: {len(successful_logins)/total_time:.2f} requests/sec")
        
        return {
            "test_name": "Concurrent Login",
            "total_requests": num_users,
            "successful_requests": len(successful_logins),
            "total_time": total_time,
            "avg_response_time": statistics.mean(response_times) if response_times else 0,
            "throughput": len(successful_logins)/total_time
        }

    def test_concurrent_performance_evaluation(self, num_managers=20):
        """
        测试2：并发绩效评估提交测试
        模拟20个部门主管同时提交绩效评估
        """
        print("\n" + "="*60)
        print(f"Test 2: Concurrent Performance Evaluation ({num_managers} managers)")
        print("="*60)
        
        # 先登录获取Token
        token = self.login("admin", "admin123")
        if not token:
            print("Failed to login, skipping test")
            return None
        
        headers = {"Authorization": f"Bearer {token}"}
        
        def submit_evaluation(manager_id):
            # 模拟提交绩效评估
            payload = {
                "employeeId": f"EMP{manager_id:03d}",
                "score": 85 + manager_id % 15,
                "comment": f"Performance evaluation from manager {manager_id}",
                "evaluationDate": datetime.now().isoformat()
            }
            
            result = self.make_request(
                f"{SERVICES['PerfMgt']}/api/performance/submit",
                method="POST",
                data=payload,
                headers=headers
            )
            
            with self.lock:
                self.results["performance"].append(result)
            
            return result
        
        # 并发执行
        start_time = time.time()
        
        with ThreadPoolExecutor(max_workers=num_managers) as executor:
            futures = [executor.submit(submit_evaluation, i) for i in range(num_managers)]
            results = [future.result() for future in as_completed(futures)]
        
        end_time = time.time()
        total_time = end_time - start_time
        
        # 分析结果
        successful_submissions = [r for r in results if r.get("success") and r.get("status_code") == 200]
        response_times = [r["response_time"] for r in successful_submissions]
        
        print(f"\nTotal Time: {total_time:.2f}s")
        print(f"Successful Submissions: {len(successful_submissions)}/{num_managers}")
        print(f"Avg Response Time: {statistics.mean(response_times):.2f}ms" if response_times else "N/A")
        print(f"Throughput: {len(successful_submissions)/total_time:.2f} requests/sec")
        
        return {
            "test_name": "Concurrent Performance Evaluation",
            "total_requests": num_managers,
            "successful_requests": len(successful_submissions),
            "total_time": total_time,
            "avg_response_time": statistics.mean(response_times) if response_times else 0,
            "throughput": len(successful_submissions)/total_time
        }

    def test_concurrent_salary_approval(self, num_requests=30):
        """
        测试3：并发薪资审批测试
        模拟HR批量审批薪资调整
        """
        print("\n" + "="*60)
        print(f"Test 3: Concurrent Salary Approval ({num_requests} requests)")
        print("="*60)
        
        # 先登录获取Token
        token = self.login("admin", "admin123")
        if not token:
            print("Failed to login, skipping test")
            return None
        
        headers = {"Authorization": f"Bearer {token}"}
        
        def approve_salary(request_id):
            payload = {
                "requestId": request_id,
                "approver": "HR_ADMIN",
                "status": "APPROVED",
                "approvedDate": datetime.now().isoformat()
            }
            
            result = self.make_request(
                f"{SERVICES['SalaryMgt']}/api/salary/approve",
                method="POST",
                data=payload,
                headers=headers
            )
            
            with self.lock:
                self.results["salary_approval"].append(result)
            
            return result
        
        # 并发执行
        start_time = time.time()
        
        with ThreadPoolExecutor(max_workers=num_requests) as executor:
            futures = [executor.submit(approve_salary, i) for i in range(num_requests)]
            results = [future.result() for future in as_completed(futures)]
        
        end_time = time.time()
        total_time = end_time - start_time
        
        # 分析结果
        successful_approvals = [r for r in results if r.get("success") and r.get("status_code") == 200]
        response_times = [r["response_time"] for r in successful_approvals]
        
        print(f"\nTotal Time: {total_time:.2f}s")
        print(f"Successful Approvals: {len(successful_approvals)}/{num_requests}")
        print(f"Avg Response Time: {statistics.mean(response_times):.2f}ms" if response_times else "N/A")
        print(f"Throughput: {len(successful_approvals)/total_time:.2f} requests/sec")
        
        return {
            "test_name": "Concurrent Salary Approval",
            "total_requests": num_requests,
            "successful_requests": len(successful_approvals),
            "total_time": total_time,
            "avg_response_time": statistics.mean(response_times) if response_times else 0,
            "throughput": len(successful_approvals)/total_time
        }

    def test_database_query_performance(self, num_queries=50):
        """
        测试4：数据库查询性能测试
        重点测试多表JOIN查询和批量计算
        """
        print("\n" + "="*60)
        print(f"Test 4: Database Query Performance ({num_queries} queries)")
        print("="*60)
        
        # 先登录获取Token
        token = self.login("admin", "admin123")
        if not token:
            print("Failed to login, skipping test")
            return None
        
        headers = {"Authorization": f"Bearer {token}"}
        
        # 测试的查询接口
        query_endpoints = [
            f"{SERVICES['WorkPlanMgt']}/api/weekplan/batch-calculate",
            f"{SERVICES['StrategyMgt']}/api/strategy/multi-table-join",
            f"{SERVICES['OrgMgt']}/api/orgchart/tree-with-details",
            f"{SERVICES['PerfMgt']}/api/performance/aggregate-report"
        ]
        
        results_by_endpoint = defaultdict(list)
        
        def execute_query(endpoint):
            result = self.make_request(endpoint, headers=headers)
            
            with self.lock:
                self.results["db_query"].append(result)
                results_by_endpoint[endpoint].append(result)
            
            return result
        
        # 并发执行
        start_time = time.time()
        
        with ThreadPoolExecutor(max_workers=10) as executor:
            futures = []
            for _ in range(num_queries):
                endpoint = query_endpoints[_ % len(query_endpoints)]
                futures.append(executor.submit(execute_query, endpoint))
            
            results = [future.result() for future in as_completed(futures)]
        
        end_time = time.time()
        total_time = end_time - start_time
        
        # 分析每个接口的结果
        print(f"\nTotal Time: {total_time:.2f}s")
        print("\nPer-Endpoint Performance:")
        
        endpoint_stats = []
        for endpoint, endpoint_results in results_by_endpoint.items():
            successful = [r for r in endpoint_results if r.get("success")]
            if successful:
                avg_time = statistics.mean([r["response_time"] for r in successful])
                max_time = max([r["response_time"] for r in successful])
                
                print(f"\n  {endpoint.split('/')[-1]}:")
                print(f"    Requests: {len(successful)}/{len(endpoint_results)}")
                print(f"    Avg Time: {avg_time:.2f}ms")
                print(f"    Max Time: {max_time:.2f}ms")
                
                endpoint_stats.append({
                    "endpoint": endpoint,
                    "avg_time": avg_time,
                    "max_time": max_time,
                    "success_rate": len(successful)/len(endpoint_results)
                })
        
        return {
            "test_name": "Database Query Performance",
            "total_requests": num_queries,
            "total_time": total_time,
            "endpoint_stats": endpoint_stats
        }

    def test_jwt_verification_stress(self, num_verifications=100):
        """
        测试5：JWT验证压力测试
        高并发下Token验证的稳定性
        """
        print("\n" + "="*60)
        print(f"Test 5: JWT Verification Stress Test ({num_verifications} requests)")
        print("="*60)
        
        # 先登录获取Token
        token = self.login("admin", "admin123")
        if not token:
            print("Failed to login, skipping test")
            return None
        
        headers = {"Authorization": f"Bearer {token}"}
        
        def verify_token():
            # 使用需要JWT验证的接口
            result = self.make_request(
                f"{SERVICES['IAM']}/api/user/current",
                headers=headers
            )
            
            with self.lock:
                self.results["jwt_verify"].append(result)
            
            return result
        
        # 并发执行
        start_time = time.time()
        
        with ThreadPoolExecutor(max_workers=20) as executor:
            futures = [executor.submit(verify_token) for _ in range(num_verifications)]
            results = [future.result() for future in as_completed(futures)]
        
        end_time = time.time()
        total_time = end_time - start_time
        
        # 分析结果
        successful_verifications = [r for r in results if r.get("success") and r.get("status_code") == 200]
        response_times = [r["response_time"] for r in successful_verifications]
        
        print(f"\nTotal Time: {total_time:.2f}s")
        print(f"Successful Verifications: {len(successful_verifications)}/{num_verifications}")
        print(f"Avg Response Time: {statistics.mean(response_times):.2f}ms" if response_times else "N/A")
        print(f"P95 Response Time: {sorted(response_times)[int(len(response_times)*0.95)]:.2f}ms" if response_times else "N/A")
        print(f"Throughput: {len(successful_verifications)/total_time:.2f} requests/sec")
        
        return {
            "test_name": "JWT Verification Stress",
            "total_requests": num_verifications,
            "successful_requests": len(successful_verifications),
            "total_time": total_time,
            "avg_response_time": statistics.mean(response_times) if response_times else 0,
            "p95_response_time": sorted(response_times)[int(len(response_times)*0.95)] if response_times else 0,
            "throughput": len(successful_verifications)/total_time
        }

    def run_all_tests(self):
        """运行所有性能测试"""
        print("\n" + "="*60)
        print("Performance and Load Testing Suite")
        print("="*60)
        
        test_results = []
        
        # 执行各项测试
        test_results.append(self.test_concurrent_login(50))
        test_results.append(self.test_concurrent_performance_evaluation(20))
        test_results.append(self.test_concurrent_salary_approval(30))
        test_results.append(self.test_database_query_performance(50))
        test_results.append(self.test_jwt_verification_stress(100))
        
        # 生成综合报告
        self.generate_comprehensive_report(test_results)

    def generate_comprehensive_report(self, test_results):
        """生成综合性能测试报告"""
        print("\n" + "="*60)
        print("Performance Test Summary Report")
        print("="*60)
        
        print(f"\nTest Execution Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print("\nTest Results Summary:")
        print("-" * 60)
        
        for result in test_results:
            if result:
                print(f"\n{result.get('test_name', 'Unknown Test')}:")
                print(f"  Total Requests: {result.get('total_requests', 0)}")
                print(f"  Successful: {result.get('successful_requests', 0)}")
                print(f"  Total Time: {result.get('total_time', 0):.2f}s")
                if 'avg_response_time' in result:
                    print(f"  Avg Response Time: {result['avg_response_time']:.2f}ms")
                if 'throughput' in result:
                    print(f"  Throughput: {result['throughput']:.2f} req/s")
        
        # 保存报告
        report_file = "d:\\EMgt\\test-scripts\\performance\\performance_test_report.json"
        with open(report_file, 'w', encoding='utf-8') as f:
            json.dump({
                "test_execution_time": datetime.now().isoformat(),
                "test_results": test_results
            }, f, indent=2, ensure_ascii=False)
        
        print(f"\nDetailed report saved to: {report_file}")

if __name__ == "__main__":
    tester = PerformanceTest()
    tester.run_all_tests()