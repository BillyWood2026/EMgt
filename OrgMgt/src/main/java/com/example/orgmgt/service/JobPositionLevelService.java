package com.example.orgmgt.service;

import com.example.orgmgt.entity.JobPositionLevel;
import com.example.orgmgt.entity.JobPositionChangeHistory;
import java.util.List;
import java.util.Map;

/**
 * Job Position Level Service Interface
 * Manages five-level job position hierarchy for state-owned enterprise
 */
public interface JobPositionLevelService {
    
    /**
     * Get job position list by organization tree
     */
    List<Map<String, Object>> getPositionTree();
    
    /**
     * Get job position list with filters
     */
    List<JobPositionLevel> getPositionList(String keyword, Integer orgLevel, String orgCode, String status, Integer page, Integer pageSize);
    
    /**
     * Get position by ID
     */
    JobPositionLevel getPositionById(Long id);
    
    /**
     * Get position by code
     */
    JobPositionLevel getPositionByCode(String positionCode);
    
    /**
     * Create new job position
     * Validates: org level constraint, code uniqueness, standard position mapping
     */
    Map<String, Object> createPosition(JobPositionLevel position, String initiator);
    
    /**
     * Update job position
     */
    Map<String, Object> updatePosition(Long id, JobPositionLevel position, String operator);
    
    /**
     * Appoint person to position
     */
    Map<String, Object> appointPerson(Long positionId, String personId, String personName, String appointReason, String attachmentPath, String operator);
    
    /**
     * Abolish position
     */
    Map<String, Object> abolishPosition(Long id, String reason, String operator);
    
    /**
     * Toggle position status
     */
    Map<String, Object> toggleStatus(Long id, String newStatus, String operator);
    
    /**
     * Create personalized position config for organization
     */
    Map<String, Object> createPersonalizedPosition(Long orgId, String personalizedName, String reason, String operator);
    
    /**
     * Validate position creation
     */
    Map<String, Object> validateCreate(JobPositionLevel position);
    
    /**
     * Generate position code
     */
    String generatePositionCode(Long orgId, Integer orgLevel);
    
    /**
     * Get standard positions for org level
     */
    List<String> getStandardPositions(Integer orgLevel);
    
    /**
     * Get change history
     */
    List<JobPositionChangeHistory> getChangeHistory(String positionCode, String changeType, Integer page, Integer pageSize);
    
    /**
     * Get history by ID
     */
    JobPositionChangeHistory getHistoryById(Long id);
    
    /**
     * Compare versions
     */
    Map<String, Object> compareVersions(Long historyId1, Long historyId2);
    
    /**
     * Get position statistics
     */
    Map<String, Object> getPositionStats();
    
    /**
     * Batch export positions
     */
    List<JobPositionLevel> batchExport(List<Long> positionIds);
    
    /**
     * Batch configure positions for organization
     */
    Map<String, Object> batchConfigure(Long orgId, List<JobPositionLevel> positions, String operator);
    
    /**
     * Sync position to organization architecture
     */
    void syncToOrgArchitecture(Long positionId);
}