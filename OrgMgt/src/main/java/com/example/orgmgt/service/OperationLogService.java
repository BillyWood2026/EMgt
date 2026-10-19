package com.example.orgmgt.service;

import com.example.orgmgt.entity.OperationLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * COMM-001: Operation Log Service
 * Unified service interface for recording and querying operation logs
 * 
 * Features:
 * - Thread-safe logging
 * - Batch logging support
 * - Full-text search
 * - Statistics reporting
 * - Permanent storage
 */
public interface OperationLogService {
    
    /**
     * Record operation log
     * 
     * @param businessModule Business module
     * @param businessId Business entity ID
     * @param businessCode Business entity code
     * @param businessName Business entity name
     * @param operationType Operation type
     * @param operationContent Operation content description
     * @param operatorId Operator ID
     * @param operatorName Operator name
     * @param beforeData Serialized before data
     * @param afterData Serialized after data
     */
    void record(String businessModule, Long businessId, String businessCode, String businessName,
            String operationType, String operationContent, String operatorId, String operatorName,
            String beforeData, String afterData);
    
    /**
     * Record operation log with result
     * 
     * @param businessModule Business module
     * @param businessId Business entity ID
     * @param businessCode Business entity code
     * @param businessName Business entity name
     * @param operationType Operation type
     * @param operationContent Operation content description
     * @param operatorId Operator ID
     * @param operatorName Operator name
     * @param beforeData Serialized before data
     * @param afterData Serialized after data
     * @param resultStatus Result status
     * @param resultMessage Result message
     */
    void recordWithResult(String businessModule, Long businessId, String businessCode, String businessName,
            String operationType, String operationContent, String operatorId, String operatorName,
            String beforeData, String afterData, String resultStatus, String resultMessage);
    
    /**
     * Batch record operation logs
     * 
     * @param logs List of operation logs
     */
    void batchRecord(List<OperationLog> logs);
    
    /**
     * Get log by ID
     * 
     * @param id Log ID
     * @return Operation log
     */
    OperationLog getById(Long id);
    
    /**
     * Get logs by business ID
     * 
     * @param businessModule Business module
     * @param businessId Business entity ID
     * @return List of operation logs
     */
    List<OperationLog> getByBusinessId(String businessModule, Long businessId);
    
    /**
     * Get logs by operator
     * 
     * @param operatorId Operator ID
     * @return List of operation logs
     */
    List<OperationLog> getByOperator(String operatorId);
    
    /**
     * Get logs by module
     * 
     * @param businessModule Business module
     * @return List of operation logs
     */
    List<OperationLog> getByModule(String businessModule);
    
    /**
     * Search logs by keyword
     * 
     * @param keyword Search keyword
     * @return List of operation logs
     */
    List<OperationLog> search(String keyword);
    
    /**
     * Get logs with pagination
     * 
     * @param businessModule Business module (optional)
     * @param operationType Operation type (optional)
     * @param operatorId Operator ID (optional)
     * @param startTime Start time (optional)
     * @param endTime End time (optional)
     * @param page Page number
     * @param pageSize Page size
     * @return List of operation logs
     */
    List<OperationLog> getLogsWithPagination(String businessModule, String operationType, String operatorId,
            LocalDateTime startTime, LocalDateTime endTime, Integer page, Integer pageSize);
    
    /**
     * Count logs by conditions
     * 
     * @param businessModule Business module (optional)
     * @param operationType Operation type (optional)
     * @param operatorId Operator ID (optional)
     * @param startTime Start time (optional)
     * @param endTime End time (optional)
     * @return Count of logs
     */
    int countLogs(String businessModule, String operationType, String operatorId,
            LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Generate log statistics report
     * 
     * @return Statistics map
     */
    Map<String, Object> generateStatistics();
    
    /**
     * Generate log statistics by module
     * 
     * @return Statistics map by module
     */
    Map<String, Object> generateStatisticsByModule();
}