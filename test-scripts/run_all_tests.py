#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
EMgt系统全面测试启动脚本
一键执行所有测试套件
"""

import os
import sys
import subprocess
import time
from datetime import datetime

class TestRunner:
    def __init__(self):
        self.test_scripts_dir = r"d:\EMgt\test-scripts"
        self.results = []
        
    def print_header(self):
        """打印测试套件标题"""
        print("\n" + "="*70)
        print("   EMgt Enterprise Management System - Comprehensive Test Suite")
        print("   Generated: 2026-07-31")
        print("="*70 + "\n")
    
    def run_test(self, category, script_name, description):
        """运行单个测试脚本"""
        print(f"\n{'='*70}")
        print(f"  [{category}] {description}")
        print(f"  Script: {script_name}")
        print("="*70)
        
        script_path = os.path.join(self.test_scripts_dir, category, script_name)
        
        if not os.path.exists(script_path):
            print(f"  ⚠️  Script not found: {script_path}")
            return False
        
        start_time = time.time()
        
        try:
            result = subprocess.run(
                [sys.executable, script_path],
                cwd=os.path.dirname(script_path),
                capture_output=True,
                text=True,
                timeout=300  # 5分钟超时
            )
            
            end_time = time.time()
            duration = end_time - start_time
            
            # 打印输出
            if result.stdout:
                print(result.stdout)
            
            if result.returncode == 0:
                print(f"\n  ✅ Test completed successfully (Duration: {duration:.2f}s)")
                self.results.append({
                    "category": category,
                    "script": script_name,
                    "status": "PASSED",
                    "duration": duration
                })
                return True
            else:
                print(f"\n  ❌ Test failed with return code: {result.returncode}")
                if result.stderr:
                    print(f"  Error: {result.stderr}")
                self.results.append({
                    "category": category,
                    "script": script_name,
                    "status": "FAILED",
                    "duration": duration
                })
                return False
        
        except subprocess.TimeoutExpired:
            print(f"\n  ⏱️  Test timed out (>300s)")
            self.results.append({
                "category": category,
                "script": script_name,
                "status": "TIMEOUT",
                "duration": 300
            })
            return False
        
        except Exception as e:
            print(f"\n  ❌ Error running test: {str(e)}")
            self.results.append({
                "category": category,
                "script": script_name,
                "status": "ERROR",
                "duration": 0
            })
            return False
    
    def run_security_tests(self):
        """运行安全测试"""
        print("\n\n" + "🔒"*35)
        print("  SECTION 1: SECURITY TESTING")
        print("🔒"*35)
        
        self.run_test("security", "jwt_auth_penetration_test.py", 
                     "JWT Authentication Penetration Test")
        self.run_test("security", "sql_injection_test.py", 
                     "SQL Injection Test")
        self.run_test("security", "data_masking_test.py", 
                     "Data Masking Test")
    
    def run_performance_tests(self):
        """运行性能测试"""
        print("\n\n" + "⚡"*35)
        print("  SECTION 2: PERFORMANCE TESTING")
        print("⚡"*35)
        
        self.run_test("performance", "stress_test.py", 
                     "Performance and Load Test")
        
        # JMeter测试需要单独运行（需要JMeter安装）
        print("\n  Note: JMeter test requires JMeter installation.")
        print("  Run manually: jmeter -n -t jmeter_test_plan.jmx -l results.jtl")
    
    def run_concurrency_tests(self):
        """运行并发测试"""
        print("\n\n" + "🔄"*35)
        print("  SECTION 3: CONCURRENCY TESTING")
        print("🔄"*35)
        
        print("\n  Note: Java concurrency test requires Maven/Spring Boot environment.")
        print("  Compile and run manually in IDE or with Maven.")
    
    def run_fault_tolerance_tests(self):
        """运行容错测试"""
        print("\n\n" + "🛡️"*35)
        print("  SECTION 4: FAULT TOLERANCE TESTING")
        print("🛡️"*35)
        
        self.run_test("fault-tolerance", "fault_tolerance_test.py", 
                     "Fault Tolerance Test")
    
    def run_compatibility_tests(self):
        """运行兼容性测试"""
        print("\n\n" + "🌐"*35)
        print("  SECTION 5: BROWSER COMPATIBILITY TESTING")
        print("🌐"*35)
        
        self.run_test("compatibility", "browser_compatibility_test.py", 
                     "Browser Compatibility Test")
    
    def run_data_consistency_tests(self):
        """运行数据一致性测试"""
        print("\n\n" + "🔍"*35)
        print("  SECTION 6: DATA CONSISTENCY TESTING")
        print("🔍"*35)
        
        self.run_test("data-consistency", "data_consistency_test.py", 
                     "Data Consistency Test")
    
    def run_localization_tests(self):
        """运行本地化测试"""
        print("\n\n" + "🌍"*35)
        print("  SECTION 7: LOCALIZATION TESTING")
        print("🌍"*35)
        
        self.run_test("localization", "encoding_localization_test.py", 
                     "Encoding and Localization Test")
    
    def run_reporting_tests(self):
        """运行报表测试"""
        print("\n\n" + "📊"*35)
        print("  SECTION 8: REPORTING TESTING")
        print("📊"*35)
        
        self.run_test("reporting", "boundary_test.py", 
                     "Reporting and Boundary Test")
    
    def run_all_tests(self):
        """运行所有测试"""
        self.print_header()
        
        total_start = time.time()
        
        # 按顺序执行所有测试
        self.run_security_tests()
        self.run_performance_tests()
        self.run_concurrency_tests()
        self.run_fault_tolerance_tests()
        self.run_compatibility_tests()
        self.run_data_consistency_tests()
        self.run_localization_tests()
        self.run_reporting_tests()
        
        total_end = time.time()
        total_duration = total_end - total_start
        
        # 打印总结
        self.print_summary(total_duration)
    
    def print_summary(self, total_duration):
        """打印测试总结"""
        print("\n\n" + "="*70)
        print("  TEST EXECUTION SUMMARY")
        print("="*70)
        
        passed = sum(1 for r in self.results if r["status"] == "PASSED")
        failed = sum(1 for r in self.results if r["status"] == "FAILED")
        timeout = sum(1 for r in self.results if r["status"] == "TIMEOUT")
        error = sum(1 for r in self.results if r["status"] == "ERROR")
        total = len(self.results)
        
        print(f"\n  Total Tests Executed: {total}")
        print(f"  ✅ Passed: {passed}")
        print(f"  ❌ Failed: {failed}")
        print(f"  ⏱️  Timeout: {timeout}")
        print(f"  ⚠️  Error: {error}")
        
        if total > 0:
            pass_rate = (passed / total) * 100
            print(f"\n  Pass Rate: {pass_rate:.2f}%")
        
        print(f"\n  Total Duration: {total_duration/60:.2f} minutes")
        
        # 按类别统计
        print("\n  Results by Category:")
        print("  " + "-"*66)
        
        categories = {}
        for result in self.results:
            cat = result["category"]
            if cat not in categories:
                categories[cat] = {"total": 0, "passed": 0}
            categories[cat]["total"] += 1
            if result["status"] == "PASSED":
                categories[cat]["passed"] += 1
        
        for cat, stats in categories.items():
            cat_pass_rate = (stats["passed"] / stats["total"]) * 100
            status_icon = "✅" if cat_pass_rate == 100 else "⚠️" if cat_pass_rate >= 50 else "❌"
            print(f"  {status_icon} {cat:20s} {stats['passed']}/{stats['total']} passed ({cat_pass_rate:.0f}%)")
        
        print("\n" + "="*70)
        
        # 保存结果
        self.save_results(total_duration)
    
    def save_results(self, total_duration):
        """保存测试结果"""
        import json
        from datetime import datetime
        
        report = {
            "execution_time": datetime.now().isoformat(),
            "total_duration_seconds": total_duration,
            "results": self.results
        }
        
        report_file = os.path.join(self.test_scripts_dir, "test_execution_summary.json")
        with open(report_file, 'w', encoding='utf-8') as f:
            json.dump(report, f, indent=2, ensure_ascii=False)
        
        print(f"  Results saved to: {report_file}")

def main():
    """主函数"""
    runner = TestRunner()
    
    print("\n" + "="*70)
    print("  EMgt Comprehensive Test Suite Launcher")
    print("="*70)
    print("\n  Available options:")
    print("  1. Run all tests (default)")
    print("  2. Run security tests only")
    print("  3. Run performance tests only")
    print("  4. Run compatibility tests only")
    print("  5. Run specific test category")
    print("  Q. Quit")
    print()
    
    choice = input("  Enter your choice (1-5/Q): ").strip().upper()
    
    if choice == 'Q':
        print("\n  Exiting...")
        return
    elif choice == '2':
        runner.run_security_tests()
    elif choice == '3':
        runner.run_performance_tests()
    elif choice == '4':
        runner.run_compatibility_tests()
    elif choice == '5':
        category = input("  Enter category name (security/performance/concurrency/fault-tolerance/compatibility/data-consistency/localization/reporting): ").strip()
        print()
        if category == 'security':
            runner.run_security_tests()
        elif category == 'performance':
            runner.run_performance_tests()
        elif category == 'concurrency':
            runner.run_concurrency_tests()
        elif category == 'fault-tolerance':
            runner.run_fault_tolerance_tests()
        elif category == 'compatibility':
            runner.run_compatibility_tests()
        elif category == 'data-consistency':
            runner.run_data_consistency_tests()
        elif category == 'localization':
            runner.run_localization_tests()
        elif category == 'reporting':
            runner.run_reporting_tests()
        else:
            print("  Invalid category!")
            return
    else:  # Default: run all
        runner.run_all_tests()
    
    print("\n  Test execution completed!")
    input("\n  Press Enter to exit...")

if __name__ == "__main__":
    main()