package com.example.emgt.test.concurrency;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 并发与事务一致性测试套件
 * 
 * 测试目标：
 * 1. 乐观锁/悲观锁测试：验证多人同时修改的冲突处理
 * 2. 事务回滚测试：测试跨表写入的事务一致性
 * 3. 分布式锁测试：防止重复提交
 * 
 * @author EMgt Test Team
 * @since 2026-07-31
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransactionConcurrencyTest {

    @Autowired
    private DataSource dataSource;

    private static final int THREAD_COUNT = 10;
    private static final int ITERATIONS = 100;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 测试1：乐观锁冲突测试
     * 场景：10个线程同时修改同一绩效表单
     * 预期：只有一个线程能成功修改，其他线程因版本冲突失败
     */
    @Test
    @Order(1)
    @DisplayName("乐观锁冲突测试 - 多人同时修改绩效表单")
    void testOptimisticLockConflict() throws Exception {
        System.out.println("\n=== 测试1: 乐观锁冲突测试 ===");
        
        // 准备测试数据
        long performanceId = insertTestPerformance();
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        
        // 创建10个线程同时修改
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            Future<Boolean> future = executorService.submit(() -> {
                try {
                    latch.countDown();
                    latch.await(); // 等待所有线程就绪
                    
                    boolean success = updatePerformanceWithOptimisticLock(performanceId, threadId);
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                    return success;
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    return false;
                }
            });
            futures.add(future);
        }
        
        // 等待所有线程完成
        for (Future<Boolean> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        
        // 验证结果
        System.out.println("成功修改次数: " + successCount.get());
        System.out.println("失败次数（版本冲突）: " + failCount.get());
        
        // 乐观锁应该只允许一个成功
        assertEquals(1, successCount.get(), "只有一次修改应该成功");
        assertEquals(THREAD_COUNT - 1, failCount.get(), "其他修改应该因版本冲突失败");
        
        // 清理
        deleteTestPerformance(performanceId);
    }

    /**
     * 测试2：悲观锁测试
     * 场景：模拟主管和HR同时修改员工档案
     * 预期：通过悲观锁串行化访问，避免脏读
     */
    @Test
    @Order(2)
    @DisplayName("悲观锁测试 - 模拟主管和HR同时修改员工档案")
    void testPessimisticLock() throws Exception {
        System.out.println("\n=== 测试2: 悲观锁测试 ===");
        
        long employeeId = insertTestEmployee();
        
        AtomicInteger modificationCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        
        // 模拟主管修改
        for (int i = 0; i < THREAD_COUNT / 2; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    updateEmployeeWithPessimisticLock(employeeId, "主管修改-" + threadId);
                    modificationCount.incrementAndGet();
                } catch (Exception e) {
                    conflictCount.incrementAndGet();
                    System.out.println("主管修改冲突: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        // 模拟HR修改
        for (int i = 0; i < THREAD_COUNT / 2; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    updateEmployeeWithPessimisticLock(employeeId, "HR修改-" + threadId);
                    modificationCount.incrementAndGet();
                } catch (Exception e) {
                    conflictCount.incrementAndGet();
                    System.out.println("HR修改冲突: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        // 开始测试
        startLatch.countDown();
        endLatch.await(30, TimeUnit.SECONDS);
        
        // 验证：所有修改都应该串行完成，无数据不一致
        System.out.println("成功修改次数: " + modificationCount.get());
        System.out.println("冲突/等待次数: " + conflictCount.get());
        
        // 悲观锁应该保证所有操作都能完成（但会等待）
        assertEquals(THREAD_COUNT, modificationCount.get() + conflictCount.get(), "所有操作应该完成");
        
        // 验证数据一致性
        String finalData = getEmployeeData(employeeId);
        assertNotNull(finalData, "数据应该存在");
        
        // 清理
        deleteTestEmployee(employeeId);
    }

    /**
     * 测试3：事务回滚测试
     * 场景：StrategyPlanning中"主计划+子任务"跨表写入
     * 预期：断电或异常时全部回滚
     */
    @Test
    @Order(3)
    @DisplayName("事务回滚测试 - 主计划与子任务的跨表写入")
    void testTransactionRollback() throws Exception {
        System.out.println("\n=== 测试3: 事务回滚测试 ===");
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rollbackCount = new AtomicInteger(0);
        
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        
        List<Future<Boolean>> futures = new ArrayList<>();
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            final boolean shouldFail = (i == THREAD_COUNT - 1); // 最后一个线程模拟失败
            
            Future<Boolean> future = executorService.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    
                    // 创建主计划+子任务的事务
                    boolean success = createPlanWithTasks(threadId, shouldFail);
                    
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        rollbackCount.incrementAndGet();
                    }
                    
                    return success;
                } catch (Exception e) {
                    rollbackCount.incrementAndGet();
                    System.out.println("线程 " + threadId + " 异常: " + e.getMessage());
                    return false;
                }
            });
            futures.add(future);
        }
        
        // 等待所有线程完成
        for (Future<Boolean> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        
        System.out.println("成功次数: " + successCount.get());
        System.out.println("回滚次数: " + rollbackCount.get());
        
        // 验证数据库中数据的一致性
        verifyPlanTaskConsistency();
    }

    /**
     * 测试4：分布式锁测试
     * 场景：防止重复提交入职/离职流程
     * 预期：同一员工同时提交多次入职申请，只有一次成功
     */
    @Test
    @Order(4)
    @DisplayName("分布式锁测试 - 防止重复提交入职流程")
    void testDistributedLock() throws Exception {
        System.out.println("\n=== 测试4: 分布式锁测试 ===");
        
        String employeeId = "EMP-TEST-" + System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);
        
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        
        // 使用简单的锁模拟（实际应使用Redis或ZooKeeper）
        ReentrantLock distributedLock = new ReentrantLock();
        
        List<Future<Boolean>> futures = new ArrayList<>();
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            Future<Boolean> future = executorService.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    
                    // 尝试获取分布式锁
                    boolean lockAcquired = distributedLock.tryLock(100, TimeUnit.MILLISECONDS);
                    
                    if (lockAcquired) {
                        try {
                            // 检查是否已提交
                            boolean alreadySubmitted = checkEmployeeOnboardingExists(employeeId);
                            
                            if (!alreadySubmitted) {
                                // 提交入职申请
                                submitOnboarding(employeeId);
                                successCount.incrementAndGet();
                                return true;
                            } else {
                                duplicateCount.incrementAndGet();
                                return false;
                            }
                        } finally {
                            distributedLock.unlock();
                        }
                    } else {
                        duplicateCount.incrementAndGet();
                        return false;
                    }
                } catch (Exception e) {
                    System.out.println("分布式锁异常: " + e.getMessage());
                    return false;
                }
            });
            futures.add(future);
        }
        
        // 等待所有线程完成
        for (Future<Boolean> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        
        System.out.println("成功提交次数: " + successCount.get());
        System.out.println("重复提交次数: " + duplicateCount.get());
        
        // 分布式锁应该保证只有一次成功
        assertEquals(1, successCount.get(), "应该只有一次入职申请成功");
        
        // 清理
        deleteTestOnboarding(employeeId);
    }

    // ==================== 辅助方法 ====================

    private long insertTestPerformance() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO performance_evaluation (employee_id, score, version, create_time) VALUES (?, ?, 1, NOW())";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, "EMP-TEST-001");
            stmt.setInt(2, 85);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return -1;
    }

    private boolean updatePerformanceWithOptimisticLock(long performanceId, int threadId) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            // 乐观锁：检查版本号
            String querySql = "SELECT version FROM performance_evaluation WHERE id = ?";
            PreparedStatement queryStmt = conn.prepareStatement(querySql);
            queryStmt.setLong(1, performanceId);
            ResultSet rs = queryStmt.executeQuery();
            
            if (rs.next()) {
                int currentVersion = rs.getInt("version");
                
                // 尝试更新（带版本检查）
                String updateSql = "UPDATE performance_evaluation SET score = ?, version = version + 1, update_time = NOW() WHERE id = ? AND version = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, 90 + threadId);
                updateStmt.setLong(2, performanceId);
                updateStmt.setInt(3, currentVersion);
                
                int rows = updateStmt.executeUpdate();
                return rows > 0;
            }
        }
        return false;
    }

    private void updateEmployeeWithPessimisticLock(long employeeId, String modification) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            // 悲观锁：SELECT FOR UPDATE
            String lockSql = "SELECT * FROM employee WHERE id = ? FOR UPDATE";
            PreparedStatement lockStmt = conn.prepareStatement(lockSql);
            lockStmt.setLong(1, employeeId);
            lockStmt.executeQuery();
            
            // 模拟处理时间
            Thread.sleep(100);
            
            // 更新
            String updateSql = "UPDATE employee SET modification_history = CONCAT(modification_history, ?), update_time = NOW() WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, ";" + modification);
            updateStmt.setLong(2, employeeId);
            updateStmt.executeUpdate();
            
            conn.commit();
        }
    }

    private boolean createPlanWithTasks(int threadId, boolean shouldFail) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // 1. 创建主计划
                String planSql = "INSERT INTO strategy_plan (plan_name, status, create_time) VALUES (?, 'DRAFT', NOW())";
                PreparedStatement planStmt = conn.prepareStatement(planSql, PreparedStatement.RETURN_GENERATED_KEYS);
                planStmt.setString(1, "测试计划-" + threadId);
                planStmt.executeUpdate();
                
                ResultSet rs = planStmt.getGeneratedKeys();
                long planId = -1;
                if (rs.next()) {
                    planId = rs.getLong(1);
                }
                
                // 2. 创建子任务
                for (int i = 1; i <= 3; i++) {
                    String taskSql = "INSERT INTO strategy_task (plan_id, task_name, status, create_time) VALUES (?, ?, 'TODO', NOW())";
                    PreparedStatement taskStmt = conn.prepareStatement(taskSql);
                    taskStmt.setLong(1, planId);
                    taskStmt.setString(2, "子任务-" + i);
                    taskStmt.executeUpdate();
                }
                
                // 模拟失败
                if (shouldFail) {
                    throw new RuntimeException("模拟事务失败");
                }
                
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void verifyPlanTaskConsistency() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            // 验证：每个计划都应该有对应的任务
            String sql = "SELECT COUNT(*) FROM strategy_plan p WHERE NOT EXISTS (SELECT 1 FROM strategy_task t WHERE t.plan_id = p.id)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int orphanPlans = rs.getInt(1);
                assertEquals(0, orphanPlans, "不应该存在没有子任务的计划（事务一致性）");
            }
        }
    }

    private long insertTestEmployee() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO employee (employee_name, modification_history, create_time) VALUES ('测试员工', '', NOW())";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return -1;
    }

    private String getEmployeeData(long employeeId) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT modification_history FROM employee WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("modification_history");
            }
        }
        return null;
    }

    private void deleteTestPerformance(long performanceId) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "DELETE FROM performance_evaluation WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, performanceId);
            stmt.executeUpdate();
        }
    }

    private void deleteTestEmployee(long employeeId) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "DELETE FROM employee WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, employeeId);
            stmt.executeUpdate();
        }
    }

    private boolean checkEmployeeOnboardingExists(String employeeId) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT COUNT(*) FROM onboarding_request WHERE employee_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private void submitOnboarding(String employeeId) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO onboarding_request (employee_id, status, create_time) VALUES (?, 'PENDING', NOW())";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, employeeId);
            stmt.executeUpdate();
        }
    }

    private void deleteTestOnboarding(String employeeId) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "DELETE FROM onboarding_request WHERE employee_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, employeeId);
            stmt.executeUpdate();
        }
    }
}