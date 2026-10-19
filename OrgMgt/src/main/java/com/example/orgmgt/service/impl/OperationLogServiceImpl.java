package com.example.orgmgt.service.impl;

import com.example.orgmgt.adapter.PlatformLogCenterAdapter;
import com.example.orgmgt.entity.OperationLog;
import com.example.orgmgt.mapper.OperationLogMapper;
import com.example.orgmgt.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * COMM-001: Operation Log Service Implementation
 * Unified operation logging service with permanent storage
 * 
 * Features:
 * - Thread-safe logging
 * - Immutable records (insert only)
 * - Full-text search support
 * - Batch logging
 * - Statistics reporting
 * - Permanent archiving
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogServiceImpl.class);

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * Platform Log Center Adapter - syncs logs to platform unified log center
     * for centralized log aggregation, full-text search, and compliance audit.
     */
    @Autowired
    private PlatformLogCenterAdapter platformLogCenterAdapter;

    /**
     * Thread-safe counter for log number generation
     */
    private static final AtomicLong LOG_COUNTER = new AtomicLong(0);

    /**
     * Cache for recent logs to avoid duplicate logging
     */
    private static final ConcurrentHashMap<String, LocalDateTime> RECENT_LOGS = new ConcurrentHashMap<>();

    /**
     * Cache cleanup interval in milliseconds (5 minutes)
     */
    private static final long CACHE_CLEANUP_INTERVAL = 5 * 60 * 1000;

    static {
        // Start cache cleanup thread
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(CACHE_CLEANUP_INTERVAL);
                    LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
                    RECENT_LOGS.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    /**
     * Generate unique log number
     */
    private String generateLogNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long sequence = LOG_COUNTER.incrementAndGet() % 10000;
        String uuid = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "LOG-" + timestamp + "-" + String.format("%04d", sequence) + "-" + uuid;
    }

    /**
     * Build unique key for duplicate detection
     */
    private String buildLogKey(String businessModule, Long businessId, String operationType) {
        return businessModule + ":" + businessId + ":" + operationType;
    }

    /**
     * Check if log was recently recorded (duplicate detection)
     */
    private boolean isDuplicate(String businessModule, Long businessId, String operationType) {
        String key = buildLogKey(businessModule, businessId, operationType);
        LocalDateTime lastLogTime = RECENT_LOGS.get(key);
        
        if (lastLogTime != null) {
            // Allow same operation after 1 second
            if (LocalDateTime.now().isBefore(lastLogTime.plusSeconds(1))) {
                return true;
            }
        }
        
        RECENT_LOGS.put(key, LocalDateTime.now());
        return false;
    }

    @Override
    @Transactional
    public void record(String businessModule, Long businessId, String businessCode, String businessName,
            String operationType, String operationContent, String operatorId, String operatorName,
            String beforeData, String afterData) {
        
        // Skip duplicate logs within 1 second
        if (isDuplicate(businessModule, businessId, operationType)) {
            logger.debug("Skipping duplicate log for {}:{}:{}", businessModule, businessId, operationType);
            return;
        }
        
        OperationLog log = new OperationLog();
        log.setLogNo(generateLogNo());
        log.setBusinessModule(businessModule);
        log.setBusinessId(businessId);
        log.setBusinessCode(businessCode);
        log.setBusinessName(businessName);
        log.setOperationType(operationType);
        log.setOperationContent(operationContent);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setBeforeData(beforeData);
        log.setAfterData(afterData);
        log.setResultStatus(OperationLog.RESULT_SUCCESS);
        log.setOperationTime(LocalDateTime.now());
        log.setCreateTime(LocalDateTime.now());
        
        // Build change summary
        if (beforeData != null || afterData != null) {
            log.setChangeSummary(buildChangeSummary(beforeData, afterData));
        }
        
        // Insert log (immutable, cannot be updated or deleted)
        operationLogMapper.insert(log);

        // Sync to platform unified log center for centralized audit
        syncToPlatformLogCenter(log);

        logger.debug("Recorded operation log: {} - {} - {}", log.getLogNo(), businessModule, operationType);
    }

    @Override
    @Transactional
    public void recordWithResult(String businessModule, Long businessId, String businessCode, String businessName,
            String operationType, String operationContent, String operatorId, String operatorName,
            String beforeData, String afterData, String resultStatus, String resultMessage) {
        
        // Skip duplicate logs within 1 second
        if (isDuplicate(businessModule, businessId, operationType)) {
            logger.debug("Skipping duplicate log for {}:{}:{}", businessModule, businessId, operationType);
            return;
        }
        
        OperationLog log = new OperationLog();
        log.setLogNo(generateLogNo());
        log.setBusinessModule(businessModule);
        log.setBusinessId(businessId);
        log.setBusinessCode(businessCode);
        log.setBusinessName(businessName);
        log.setOperationType(operationType);
        log.setOperationContent(operationContent);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setBeforeData(beforeData);
        log.setAfterData(afterData);
        log.setResultStatus(resultStatus);
        log.setResultMessage(resultMessage);
        log.setOperationTime(LocalDateTime.now());
        log.setCreateTime(LocalDateTime.now());
        
        // Build change summary
        if (beforeData != null || afterData != null) {
            log.setChangeSummary(buildChangeSummary(beforeData, afterData));
        }
        
        // Insert log (immutable)
        operationLogMapper.insert(log);

        // Sync to platform unified log center for centralized audit
        syncToPlatformLogCenter(log);

        if (OperationLog.RESULT_FAILED.equals(resultStatus)) {
            logger.warn("Recorded failed operation log: {} - {} - {} - {}",
                log.getLogNo(), businessModule, operationType, resultMessage);
        } else {
            logger.debug("Recorded operation log: {} - {} - {}", log.getLogNo(), businessModule, operationType);
        }
    }

    @Override
    @Transactional
    public void batchRecord(List<OperationLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        
        for (OperationLog log : logs) {
            if (log.getLogNo() == null || log.getLogNo().isEmpty()) {
                log.setLogNo(generateLogNo());
            }
            if (log.getOperationTime() == null) {
                log.setOperationTime(LocalDateTime.now());
            }
            if (log.getCreateTime() == null) {
                log.setCreateTime(LocalDateTime.now());
            }
            if (log.getResultStatus() == null) {
                log.setResultStatus(OperationLog.RESULT_SUCCESS);
            }
            
            // Build change summary
            if ((log.getBeforeData() != null || log.getAfterData() != null) && log.getChangeSummary() == null) {
                log.setChangeSummary(buildChangeSummary(log.getBeforeData(), log.getAfterData()));
            }
        }
        
        // Batch insert using individual inserts
        for (OperationLog log : logs) {
            operationLogMapper.insert(log);
        }

        // Batch sync to platform unified log center
        List<PlatformLogCenterAdapter.LogEntry> platformLogs = new ArrayList<>();
        for (OperationLog log : logs) {
            platformLogs.add(convertToPlatformLogEntry(log));
        }
        try {
            platformLogCenterAdapter.batchRecordLogs(platformLogs);
        } catch (Exception e) {
            logger.warn("Failed to batch sync logs to platform log center: {}", e.getMessage());
        }

        logger.info("Batch recorded {} operation logs", logs.size());
    }

    /**
     * Sync a single log to platform unified log center
     * Wrapped in try-catch to ensure platform sync failure does not affect business logic
     */
    private void syncToPlatformLogCenter(OperationLog log) {
        try {
            PlatformLogCenterAdapter.LogEntry entry = convertToPlatformLogEntry(log);
            platformLogCenterAdapter.recordLog(entry);
        } catch (Exception e) {
            logger.warn("Failed to sync log to platform log center: {}", e.getMessage());
        }
    }

    /**
     * Convert local OperationLog to platform LogEntry
     */
    private PlatformLogCenterAdapter.LogEntry convertToPlatformLogEntry(OperationLog log) {
        PlatformLogCenterAdapter.LogEntry entry = new PlatformLogCenterAdapter.LogEntry(
            log.getBusinessModule(),
            log.getBusinessId(),
            log.getOperationType(),
            log.getOperatorId(),
            log.getOperatorName()
        );
        entry.setLogId(log.getLogNo());
        entry.setBusinessCode(log.getBusinessCode());
        entry.setBusinessName(log.getBusinessName());
        entry.setOperationContent(log.getOperationContent());
        entry.setBeforeData(log.getBeforeData());
        entry.setAfterData(log.getAfterData());
        entry.setResultStatus(log.getResultStatus());
        entry.setResultMessage(log.getResultMessage());
        entry.setOperateTime(log.getOperationTime());
        return entry;
    }

    /**
     * Builds change summary from before/after data
     */
    private String buildChangeSummary(String beforeData, String afterData) {
        if (beforeData == null && afterData == null) {
            return null;
        }
        
        StringBuilder summary = new StringBuilder();
        
        if (beforeData == null && afterData != null) {
            summary.append("新增记录");
        } else if (beforeData != null && afterData == null) {
            summary.append("删除记录");
        } else {
            // Simple comparison - count fields
            int beforeLength = beforeData.length();
            int afterLength = afterData.length();
            
            if (beforeLength < afterLength) {
                summary.append("数据增加");
            } else if (beforeLength > afterLength) {
                summary.append("数据减少");
            } else {
                summary.append("数据修改");
            }
        }
        
        return summary.toString();
    }

    @Override
    public OperationLog getById(Long id) {
        return operationLogMapper.selectById(id);
    }

    @Override
    public List<OperationLog> getByBusinessId(String businessModule, Long businessId) {
        return operationLogMapper.findByBusinessId(businessModule, businessId);
    }

    @Override
    public List<OperationLog> getByOperator(String operatorId) {
        return operationLogMapper.findByOperatorId(operatorId);
    }

    @Override
    public List<OperationLog> getByModule(String businessModule) {
        return operationLogMapper.findByModule(businessModule);
    }

    @Override
    public List<OperationLog> search(String keyword) {
        return operationLogMapper.searchByKeyword(keyword);
    }

    @Override
    public List<OperationLog> getLogsWithPagination(String businessModule, String operationType, String operatorId,
            LocalDateTime startTime, LocalDateTime endTime, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
        return operationLogMapper.findWithPagination(businessModule, operationType, operatorId,
            startTime, endTime, safePage, safeSize, (safePage - 1) * safeSize);
    }

    @Override
    public int countLogs(String businessModule, String operationType, String operatorId,
            LocalDateTime startTime, LocalDateTime endTime) {
        return operationLogMapper.countByConditions(businessModule, operationType, operatorId, 
            startTime, endTime);
    }

    @Override
    public Map<String, Object> generateStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Total logs
        long totalLogs = operationLogMapper.selectCount(null);
        statistics.put("totalLogs", totalLogs);
        
        // Success/failure count
        long successCount = operationLogMapper.countByConditions(null, null, null, null, null);
        // Need to implement separate count queries for success/failure
        
        // Statistics by module
        statistics.put("statisticsByModule", generateStatisticsByModule());
        
        // Statistics by operation type
        Map<String, Object> typeStats = new HashMap<>();
        for (String opType : new String[]{OperationLog.OP_CREATE, OperationLog.OP_UPDATE, 
                OperationLog.OP_DELETE, OperationLog.OP_APPROVE, OperationLog.OP_QUERY}) {
            typeStats.put(opType, operationLogMapper.countByConditions(null, opType, null, null, null));
        }
        statistics.put("statisticsByOperationType", typeStats);
        
        return statistics;
    }

    @Override
    public Map<String, Object> generateStatisticsByModule() {
        Map<String, Object> statistics = new HashMap<>();
        
        String[] modules = {
            OperationLog.MODULE_ORG,
            OperationLog.MODULE_POSITION,
            OperationLog.MODULE_POSITION_HIERARCHY,
            OperationLog.MODULE_COMMAND_CHAIN,
            OperationLog.MODULE_STAFFING,
            OperationLog.MODULE_VIRTUAL_ORG,
            OperationLog.MODULE_JOB_DESCRIPTION,
            OperationLog.MODULE_APPROVAL,
            OperationLog.MODULE_PERMISSION,
            OperationLog.MODULE_SYSTEM
        };
        
        for (String module : modules) {
            int count = operationLogMapper.countByModule(module);
            statistics.put(module, count);
        }
        
        return statistics;
    }
}