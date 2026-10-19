package com.example.orgmgt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orgmgt.entity.ManagementSpanAnomaly;
import java.util.List;
import java.util.Map;

/**
 * Management Span Anomaly Service Interface
 */
public interface ManagementSpanAnomalyService extends IService<ManagementSpanAnomaly> {
    
    /**
     * Query anomalies by warning level
     */
    List<ManagementSpanAnomaly> getByWarningLevel(String warningLevel);
    
    /**
     * Query anomalies by status
     */
    List<ManagementSpanAnomaly> getByStatus(String status);
    
    /**
     * Query anomalies by organization
     */
    List<ManagementSpanAnomaly> getByOrgId(Long orgId);
    
    /**
     * Query all unresolved anomalies
     */
    List<ManagementSpanAnomaly> getUnresolved();
    
    /**
     * Create new anomaly record
     */
    boolean createAnomaly(ManagementSpanAnomaly anomaly);
    
    /**
     * Update anomaly status
     */
    boolean updateStatus(Long id, String status, String remark);
    
    /**
     * Resolve anomaly
     */
    boolean resolveAnomaly(Long id, String rectificationDesc);
    
    /**
     * Apply exemption
     */
    boolean applyExemption(Long id, String exemptionReason, String approvalNo);
    
    /**
     * Get anomaly statistics
     */
    Map<String, Object> getAnomalyStats();
    
    /**
     * Validate position against span standards and create anomaly if violated
     */
    boolean validateAndCreateAnomaly(Long positionId, String positionLevel, Integer currentSpan);
}
