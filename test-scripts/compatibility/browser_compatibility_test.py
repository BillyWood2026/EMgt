#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Cross-Browser and Device Compatibility Testing Suite
测试目标：
1. 浏览器兼容性：Chrome、Edge、Firefox最新版
2. 分辨率适配测试：1920x1080、1366x768、1280x720
3. 重点测试：组织架构树形图、甘特图式策略计划

依赖安装：
pip install selenium webdriver-manager
"""

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.service import Service as ChromeService
from selenium.webdriver.edge.service import Service as EdgeService
from selenium.webdriver.firefox.service import Service as FirefoxService
from webdriver_manager.chrome import ChromeDriverManager
from webdriver_manager.microsoft import EdgeChromiumDriverManager
from webdriver_manager.firefox import GeckoDriverManager
from selenium.common.exceptions import TimeoutException, NoSuchElementException
import time
import json
from datetime import datetime
from PIL import Image
import io

class BrowserCompatibilityTest:
    def __init__(self):
        self.test_results = []
        self.base_url = "http://localhost:5173"
        self.browsers = {}
        
        # 测试分辨率
        self.resolutions = [
            {"name": "Full HD", "width": 1920, "height": 1080},
            {"name": "HD", "width": 1366, "height": 768},
            {"name": "WXGA", "width": 1280, "height": 720}
        ]
        
        # 测试浏览器
        self.browser_configs = [
            {"name": "Chrome", "driver": "chrome"},
            {"name": "Edge", "driver": "edge"},
            {"name": "Firefox", "driver": "firefox"}
        ]

    def log_result(self, test_name, browser, passed, details, screenshot_path=None):
        """记录测试结果"""
        result = {
            "test_name": test_name,
            "browser": browser,
            "passed": passed,
            "details": details,
            "screenshot": screenshot_path,
            "timestamp": datetime.now().isoformat()
        }
        self.test_results.append(result)
        
        status = "✓ PASS" if passed else "✗ FAIL"
        print(f"{status} | {browser} | {test_name} | {details}")

    def setup_driver(self, browser_name):
        """初始化浏览器驱动"""
        try:
            if browser_name == "chrome":
                options = webdriver.ChromeOptions()
                options.add_argument('--headless')
                options.add_argument('--no-sandbox')
                options.add_argument('--disable-dev-shm-usage')
                options.add_argument('--window-size=1920,1080')
                
                driver = webdriver.Chrome(
                    service=ChromeService(ChromeDriverManager().install()),
                    options=options
                )
                return driver
            
            elif browser_name == "edge":
                options = webdriver.EdgeOptions()
                options.add_argument('--headless')
                options.add_argument('--no-sandbox')
                options.add_argument('--disable-dev-shm-usage')
                options.add_argument('--window-size=1920,1080')
                
                driver = webdriver.Edge(
                    service=EdgeService(EdgeChromiumDriverManager().install()),
                    options=options
                )
                return driver
            
            elif browser_name == "firefox":
                options = webdriver.FirefoxOptions()
                options.add_argument('--headless')
                options.add_argument('--width=1920')
                options.add_argument('--height=1080')
                
                driver = webdriver.Firefox(
                    service=FirefoxService(GeckoDriverManager().install()),
                    options=options
                )
                return driver
        
        except Exception as e:
            print(f"Failed to setup {browser_name} driver: {e}")
            return None

    def login(self, driver, username="admin", password="admin123"):
        """登录系统"""
        try:
            # 访问登录页
            driver.get(self.base_url)
            time.sleep(2)
            
            # 输入用户名密码
            username_input = WebDriverWait(driver, 10).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, "input[type='text']"))
            )
            username_input.send_keys(username)
            
            password_input = driver.find_element(By.CSS_SELECTOR, "input[type='password']")
            password_input.send_keys(password)
            
            # 点击登录按钮
            login_button = driver.find_element(By.CSS_SELECTOR, "button[type='submit']")
            login_button.click()
            
            # 等待登录成功
            WebDriverWait(driver, 10).until(
                EC.url_contains("/home")
            )
            
            return True
        
        except Exception as e:
            print(f"Login failed: {e}")
            return False

    def test_org_chart_rendering(self, driver, browser_name):
        """测试组织架构树形图渲染"""
        test_name = "Organization Chart Rendering"
        
        try:
            # 访问组织架构页面
            driver.get(f"{self.base_url}/org-mgt/org-chart")
            time.sleep(3)
            
            # 等待树形图加载
            org_chart = WebDriverWait(driver, 10).until(
                EC.presence_of_element_located((By.CLASS_NAME, "org-chart"))
            )
            
            # 检查节点是否正确渲染
            nodes = driver.find_elements(By.CLASS_NAME, "org-node")
            
            if len(nodes) > 0:
                # 截图
                screenshot_path = f"d:\\EMgt\\test-scripts\\compatibility\\screenshots\\{browser_name}_org_chart.png"
                driver.save_screenshot(screenshot_path)
                
                self.log_result(
                    test_name,
                    browser_name,
                    True,
                    f"Organization chart rendered with {len(nodes)} nodes",
                    screenshot_path
                )
            else:
                self.log_result(
                    test_name,
                    browser_name,
                    False,
                    "Organization chart nodes not found"
                )
        
        except TimeoutException:
            self.log_result(
                test_name,
                browser_name,
                False,
                "Organization chart component not loaded"
            )
        
        except Exception as e:
            self.log_result(
                test_name,
                browser_name,
                False,
                f"Exception: {str(e)}"
            )

    def test_strategy_gantt_chart(self, driver, browser_name):
        """测试策略甘特图渲染"""
        test_name = "Strategy Gantt Chart Rendering"
        
        try:
            # 访问策略计划页面
            driver.get(f"{self.base_url}/strategy-mgt/strategy-planning")
            time.sleep(3)
            
            # 等待甘特图加载
            gantt_chart = WebDriverWait(driver, 10).until(
                EC.presence_of_element_located((By.CLASS_NAME, "gantt-chart"))
            )
            
            # 检查时间轴和任务条
            timeline = driver.find_elements(By.CLASS_NAME, "gantt-timeline")
            tasks = driver.find_elements(By.CLASS_NAME, "gantt-task")
            
            if len(tasks) > 0:
                screenshot_path = f"d:\\EMgt\\test-scripts\\compatibility\\screenshots\\{browser_name}_gantt_chart.png"
                driver.save_screenshot(screenshot_path)
                
                self.log_result(
                    test_name,
                    browser_name,
                    True,
                    f"Gantt chart rendered with {len(tasks)} tasks",
                    screenshot_path
                )
            else:
                self.log_result(
                    test_name,
                    browser_name,
                    False,
                    "Gantt chart tasks not found"
                )
        
        except TimeoutException:
            self.log_result(
                test_name,
                browser_name,
                False,
                "Gantt chart component not loaded"
            )
        
        except Exception as e:
            self.log_result(
                test_name,
                browser_name,
                False,
                f"Exception: {str(e)}"
            )

    def test_form_layout(self, driver, browser_name):
        """测试表单布局"""
        test_name = "Form Layout Test"
        
        try:
            # 访问员工管理页面
            driver.get(f"{self.base_url}/org-mgt/employee")
            time.sleep(2)
            
            # 点击新增按钮
            add_button = WebDriverWait(driver, 10).until(
                EC.element_to_be_clickable((By.CSS_SELECTOR, "button.add-button"))
            )
            add_button.click()
            
            time.sleep(2)
            
            # 检查表单字段是否正确对齐
            form_fields = driver.find_elements(By.CSS_SELECTOR, ".el-form-item")
            
            if len(form_fields) > 0:
                # 检查表单是否溢出
                form_container = driver.find_element(By.CSS_SELECTOR, ".el-dialog__body")
                
                screenshot_path = f"d:\\EMgt\\test-scripts\\compatibility\\screenshots\\{browser_name}_form.png"
                driver.save_screenshot(screenshot_path)
                
                self.log_result(
                    test_name,
                    browser_name,
                    True,
                    f"Form rendered with {len(form_fields)} fields",
                    screenshot_path
                )
            else:
                self.log_result(
                    test_name,
                    browser_name,
                    False,
                    "Form fields not found"
                )
        
        except TimeoutException:
            self.log_result(
                test_name,
                browser_name,
                False,
                "Form dialog not loaded"
            )
        
        except Exception as e:
            self.log_result(
                test_name,
                browser_name,
                False,
                f"Exception: {str(e)}"
            )

    def test_responsive_layout(self, driver, browser_name, resolution):
        """测试响应式布局"""
        test_name = f"Responsive Layout ({resolution['name']})"
        
        try:
            # 设置窗口大小
            driver.set_window_size(resolution['width'], resolution['height'])
            time.sleep(1)
            
            # 访问首页
            driver.get(self.base_url)
            time.sleep(2)
            
            # 检查布局是否正常
            # 1. 检查导航栏
            navbar = WebDriverWait(driver, 10).until(
                EC.presence_of_element_located((By.CLASS_NAME, "navbar"))
            )
            
            # 2. 检查内容区域
            content = driver.find_element(By.CLASS_NAME, "main-content")
            
            # 3. 检查是否有水平滚动条（不应该有）
            has_horizontal_scroll = driver.execute_script(
                "return document.body.scrollWidth > document.body.clientWidth;"
            )
            
            screenshot_path = f"d:\\EMgt\\test-scripts\\compatibility\\screenshots\\{browser_name}_{resolution['name']}.png"
            driver.save_screenshot(screenshot_path)
            
            if not has_horizontal_scroll:
                self.log_result(
                    test_name,
                    browser_name,
                    True,
                    f"Layout fits {resolution['width']}x{resolution['height']} without horizontal scroll",
                    screenshot_path
                )
            else:
                self.log_result(
                    test_name,
                    browser_name,
                    False,
                    f"Horizontal scroll detected at {resolution['width']}x{resolution['height']}",
                    screenshot_path
                )
        
        except Exception as e:
            self.log_result(
                test_name,
                browser_name,
                False,
                f"Exception: {str(e)}"
            )

    def test_chinese_character_rendering(self, driver, browser_name):
        """测试中文字符渲染"""
        test_name = "Chinese Character Rendering"
        
        try:
            # 访问组织架构页面（包含中文）
            driver.get(f"{self.base_url}/org-mgt/org-chart")
            time.sleep(3)
            
            # 检查中文是否正确显示（不乱码）
            page_source = driver.page_source
            
            # 检查常见中文
            chinese_keywords = ["组织", "部门", "员工", "职位"]
            found_keywords = [kw for kw in chinese_keywords if kw in page_source]
            
            if len(found_keywords) > 0:
                self.log_result(
                    test_name,
                    browser_name,
                    True,
                    f"Chinese characters correctly displayed: {', '.join(found_keywords)}"
                )
            else:
                self.log_result(
                    test_name,
                    browser_name,
                    False,
                    "Chinese characters not found or incorrectly displayed"
                )
        
        except Exception as e:
            self.log_result(
                test_name,
                browser_name,
                False,
                f"Exception: {str(e)}"
            )

    def run_tests_for_browser(self, browser_config):
        """为单个浏览器运行所有测试"""
        browser_name = browser_config['name']
        driver_type = browser_config['driver']
        
        print(f"\n{'='*60}")
        print(f"Testing on {browser_name}")
        print('='*60)
        
        driver = self.setup_driver(driver_type)
        if not driver:
            print(f"Failed to initialize {browser_name} driver, skipping tests")
            return
        
        try:
            # 登录
            if not self.login(driver):
                print(f"Failed to login on {browser_name}, skipping tests")
                driver.quit()
                return
            
            # 浏览器兼容性测试
            self.test_org_chart_rendering(driver, browser_name)
            self.test_strategy_gantt_chart(driver, browser_name)
            self.test_form_layout(driver, browser_name)
            self.test_chinese_character_rendering(driver, browser_name)
            
            # 分辨率适配测试
            for resolution in self.resolutions:
                self.test_responsive_layout(driver, browser_name, resolution)
        
        finally:
            driver.quit()

    def run_all_tests(self):
        """运行所有浏览器兼容性测试"""
        print("\n" + "="*60)
        print("Cross-Browser Compatibility Testing Suite")
        print("="*60)
        
        # 为每个浏览器运行测试
        for browser_config in self.browser_configs:
            self.run_tests_for_browser(browser_config)
        
        # 生成报告
        self.generate_report()

    def generate_report(self):
        """生成测试报告"""
        print("\n" + "="*60)
        print("Browser Compatibility Test Summary Report")
        print("="*60)
        
        total_tests = len(self.test_results)
        passed_tests = sum(1 for r in self.test_results if r['passed'])
        failed_tests = total_tests - passed_tests
        
        print(f"\nTotal Tests: {total_tests}")
        print(f"Passed: {passed_tests}")
        print(f"Failed: {failed_tests}")
        print(f"Pass Rate: {(passed_tests/total_tests*100):.2f}%")
        
        # 按浏览器统计
        browser_stats = {}
        for result in self.test_results:
            browser = result['browser']
            if browser not in browser_stats:
                browser_stats[browser] = {"total": 0, "passed": 0}
            browser_stats[browser]["total"] += 1
            if result['passed']:
                browser_stats[browser]["passed"] += 1
        
        print("\nResults by Browser:")
        for browser, stats in browser_stats.items():
            pass_rate = (stats['passed'] / stats['total'] * 100) if stats['total'] > 0 else 0
            print(f"  {browser}: {stats['passed']}/{stats['total']} ({pass_rate:.2f}%)")
        
        # 保存报告
        report_file = "d:\\EMgt\\test-scripts\\compatibility\\browser_compatibility_test_report.json"
        with open(report_file, 'w', encoding='utf-8') as f:
            json.dump({
                "summary": {
                    "total": total_tests,
                    "passed": passed_tests,
                    "failed": failed_tests,
                    "pass_rate": f"{(passed_tests/total_tests*100):.2f}%"
                },
                "browser_stats": browser_stats,
                "results": self.test_results
            }, f, indent=2, ensure_ascii=False)
        
        print(f"\nReport saved to: {report_file}")

if __name__ == "__main__":
    # 创建截图目录
    import os
    os.makedirs("d:\\EMgt\\test-scripts\\compatibility\\screenshots", exist_ok=True)
    
    tester = BrowserCompatibilityTest()
    tester.run_all_tests()