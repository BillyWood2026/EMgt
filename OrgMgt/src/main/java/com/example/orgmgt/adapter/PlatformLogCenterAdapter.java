package com.example.orgmgt.adapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Platform Log Center Adapter Interface
 *
 * Syncs operation logs and audit trails to the platform unified log center.
 * All operation logs (create, update, delete, approve, export, etc.) are
 * permanently stored in the platform log center for compliance audit.
 *
 * Features:
 * - Permanent storage (cannot be deleted or modified)
 * - Full-text search and statistics
 * - Compliance audit support
 * - Cross-module log aggregation
 * - Real-time sync (no lag)
 */
public interface PlatformLogCenterAdapter {

    /**
     * Record a single operation log to platform log center
     *
     * @param logEntry Log entry
     * @return true if sync successful
     */
    boolean recordLog(LogEntry logEntry);

    /**
     * Batch record operation logs
     *
     * @param logEntries List of log entries
     * @return true if all sync successful
     */
    boolean batchRecordLogs(List<LogEntry> logEntries);

    /**
     * Search logs with filters
     *
     * @param filter Log search filter
     * @return List of matching log entries
     */
    List<LogEntry> searchLogs(LogSearchFilter filter);

    /**
     * Get logs by business module and ID
     *
     * @param businessModule Business module
     * @param businessId Business ID
     * @return List of log entries
     */
    List<LogEntry> getLogsByBusiness(String businessModule, Long businessId);

    /**
     * Generate log statistics report
     *
     * @param startTime Start time
     * @param endTime End time
     * @return Statistics map
     */
    Map<String, Object> generateStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Log Entry DTO
     */
    class LogEntry {
        private String logId;
        private String businessModule;
        private Long businessId;
        private String businessCode;
        private String businessName;
        private String operationType;
        private String operationContent;
        private String operatorId;
        private String operatorName;
        private String beforeData;
        private String afterData;
        private String resultStatus;
        private String resultMessage;
        private LocalDateTime operateTime;
        private String ipAddress;
        private String userAgent;
        private String traceId;

        public LogEntry() {}

        public LogEntry(String businessModule, Long businessId, String operationType,
                        String operatorId, String operatorName) {
            this.businessModule = businessModule;
            this.businessId = businessId;
            this.operationType = operationType;
            this.operatorId = operatorId;
            this.operatorName = operatorName;
            this.operateTime = LocalDateTime.now();
        }

        // Getters and setters
        public String getLogId() { return logId; }
        public void setLogId(String logId) { this.logId = logId; }
        public String getBusinessModule() { return businessModule; }
        public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
        public Long getBusinessId() { return businessId; }
        public void setBusinessId(Long businessId) { this.businessId = businessId; }
        public String getBusinessCode() { return businessCode; }
        public void setBusinessCode(String businessCode) { this.businessCode = businessCode; }
        public String getBusinessName() { return businessName; }
        public void setBusinessName(String businessName) { this.businessName = businessName; }
        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }
        public String getOperationContent() { return operationContent; }
        public void setOperationContent(String operationContent) { this.operationContent = operationContent; }
        public String getOperatorId() { return operatorId; }
        public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
        public String getBeforeData() { return beforeData; }
        public void setBeforeData(String beforeData) { this.beforeData = beforeData; }
        public String getAfterData() { return afterData; }
        public void setAfterData(String afterData) { this.afterData = afterData; }
        public String getResultStatus() { return resultStatus; }
        public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
        public String getResultMessage() { return resultMessage; }
        public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }
        public LocalDateTime getOperateTime() { return operateTime; }
        public void setOperateTime(LocalDateTime operateTime) { this.operateTime = operateTime; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }
    }

    /**
     * Log Search Filter DTO
     */
    class LogSearchFilter {
        private String businessModule;
        private String operationType;
        private String operatorId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String keyword;
        private Integer page;
        private Integer pageSize;

        public String getBusinessModule() { return businessModule; }
        public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }
        public String getOperatorId() { return operatorId; }
        public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        public Integer getPageSize() { return pageSize; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    }
}
