package com.example.orgmgt.service;

import com.example.orgmgt.entity.OrgChangeApproval;
import com.example.orgmgt.entity.OrgChangeHistory;
import java.util.List;
import java.util.Map;

/**
 * Organization Change Service Interface
 * Handles change approval workflows and history tracking
 */
public interface OrgChangeService {
    
    /**
     * Get change approval list
     */
    List<OrgChangeApproval> getApprovalList(String orgCode, String changeType, String status, Integer page, Integer pageSize);
    
    /**
     * Get change approval by ID
     */
    OrgChangeApproval getApprovalById(Long id);
    
    /**
     * Create change approval request
     */
    Map<String, Object> createApproval(Long orgId, String changeType, String changeContent, 
                                       String beforeData, String afterData, String initiator);
    
    /**
     * Approve change request
     */
    Map<String, Object> approve(Long id, String opinion, String operator);
    
    /**
     * Reject change request
     */
    Map<String, Object> reject(Long id, String reason, String operator);
    
    /**
     * Recall change request
     */
    Map<String, Object> recall(Long id, String operator);
    
    /**
     * Get change history list
     */
    List<OrgChangeHistory> getHistoryList(String orgCode, String changeType, String startTime, 
                                          String endTime, Integer page, Integer pageSize);
    
    /**
     * Get change history by ID
     */
    OrgChangeHistory getHistoryById(Long id);
    
    /**
     * Compare versions
     */
    Map<String, Object> compareVersions(Long historyId1, Long historyId2);
    
    /**
     * Get all change types
     */
    List<Map<String, Object>> getChangeTypes();
    
    /**
     * Get approval statistics
     */
    Map<String, Object> getApprovalStats();
    
    /**
     * Batch export change history
     */
    List<OrgChangeHistory> batchExportHistory(List<Long> historyIds);
}