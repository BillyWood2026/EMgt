package com.example.orgmgt.adapter;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Local Platform Log Center Adapter (Fallback Implementation)
 *
 * Provides local fallback implementation when platform unified log center
 * is unavailable. Stores logs in local memory with permanent retention.
 *
 * In production, this should be replaced by a Feign client that calls
 * the actual platform log center service for centralized log aggregation,
 * full-text search, and compliance audit.
 *
 * Note: All logs are permanently stored and cannot be deleted or modified.
 */
@Component
public class LocalPlatformLogCenterAdapter implements PlatformLogCenterAdapter {

    // Local storage for log entries (fallback only)
    private final Map<String, LogEntry> logStore = new ConcurrentHashMap<>();
    private final AtomicLong logIdCounter = new AtomicLong(1);

    @Override
    public boolean recordLog(LogEntry logEntry) {
        if (logEntry.getLogId() == null) {
            logEntry.setLogId("LOG-" + System.currentTimeMillis() + "-" + logIdCounter.getAndIncrement());
        }
        if (logEntry.getOperateTime() == null) {
            logEntry.setOperateTime(LocalDateTime.now());
        }
        logStore.put(logEntry.getLogId(), logEntry);
        return true;
    }

    @Override
    public boolean batchRecordLogs(List<LogEntry> logEntries) {
        for (LogEntry entry : logEntries) {
            recordLog(entry);
        }
        return true;
    }

    @Override
    public List<LogEntry> searchLogs(LogSearchFilter filter) {
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry log : logStore.values()) {
            if (matchesFilter(log, filter)) {
                result.add(log);
            }
        }
        // Sort by operate time descending
        result.sort((a, b) -> b.getOperateTime().compareTo(a.getOperateTime()));

        // Pagination
        int page = filter.getPage() != null ? filter.getPage() : 1;
        int pageSize = filter.getPageSize() != null ? filter.getPageSize() : 20;
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, result.size());
        if (start >= result.size()) {
            return new ArrayList<>();
        }
        return result.subList(start, end);
    }

    @Override
    public List<LogEntry> getLogsByBusiness(String businessModule, Long businessId) {
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry log : logStore.values()) {
            if (businessModule.equals(log.getBusinessModule()) && businessId.equals(log.getBusinessId())) {
                result.add(log);
            }
        }
        result.sort((a, b) -> b.getOperateTime().compareTo(a.getOperateTime()));
        return result;
    }

    @Override
    public Map<String, Object> generateStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        Map<String, Integer> moduleCount = new HashMap<>();
        Map<String, Integer> operationTypeCount = new HashMap<>();
        Map<String, Integer> operatorCount = new HashMap<>();
        int totalLogs = 0;

        for (LogEntry log : logStore.values()) {
            if (startTime != null && log.getOperateTime().isBefore(startTime)) continue;
            if (endTime != null && log.getOperateTime().isAfter(endTime)) continue;

            totalLogs++;
            moduleCount.merge(log.getBusinessModule(), 1, Integer::sum);
            operationTypeCount.merge(log.getOperationType(), 1, Integer::sum);
            operatorCount.merge(log.getOperatorId(), 1, Integer::sum);
        }

        stats.put("totalLogs", totalLogs);
        stats.put("moduleCount", moduleCount);
        stats.put("operationTypeCount", operationTypeCount);
        stats.put("operatorCount", operatorCount);
        stats.put("startTime", startTime);
        stats.put("endTime", endTime);
        return stats;
    }

    private boolean matchesFilter(LogEntry log, LogSearchFilter filter) {
        if (filter.getBusinessModule() != null && !filter.getBusinessModule().equals(log.getBusinessModule())) {
            return false;
        }
        if (filter.getOperationType() != null && !filter.getOperationType().equals(log.getOperationType())) {
            return false;
        }
        if (filter.getOperatorId() != null && !filter.getOperatorId().equals(log.getOperatorId())) {
            return false;
        }
        if (filter.getStartTime() != null && log.getOperateTime().isBefore(filter.getStartTime())) {
            return false;
        }
        if (filter.getEndTime() != null && log.getOperateTime().isAfter(filter.getEndTime())) {
            return false;
        }
        if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
            String keyword = filter.getKeyword().toLowerCase();
            String content = (log.getOperationContent() != null ? log.getOperationContent() : "").toLowerCase();
            String businessName = (log.getBusinessName() != null ? log.getBusinessName() : "").toLowerCase();
            if (!content.contains(keyword) && !businessName.contains(keyword)) {
                return false;
            }
        }
        return true;
    }
}
