package com.example.orgmgt.service;

import com.example.orgmgt.entity.StaffingExemptionApproval;

import java.util.List;
import java.util.Map;

/**
 * PLAN-007: Staffing Exemption Approval Service
 * Service interface for handling staffing quota and management span exemption approvals
 * 
 * Features:
 * - Submit exemption requests with justification
 * - Multi-level approval workflow
 * - Exemption period management
 * - Compliance monitoring
 * - Auto-expiration handling
 */
public interface StaffingExemptionApprovalService {
    
    /**
     * Submit staffing exemption request
     * 
     * @param exemption Exemption approval request details
     * @return Created exemption approval record
     */
    StaffingExemptionApproval submitExemptionRequest(StaffingExemptionApproval exemption);
    
    /**
     * Approve exemption request
     * 
     * @param id Exemption approval ID
     * @param approverId Approver user ID
     * @param approverName Approver user name
     * @return Updated exemption approval record
     */
    StaffingExemptionApproval approveExemption(Long id, String approverId, String approverName);
    
    /**
     * Reject exemption request
     * 
     * @param id Exemption approval ID
     * @param rejectReason Rejection reason
     * @return Updated exemption approval record
     */
    StaffingExemptionApproval rejectExemption(Long id, String rejectReason);
    
    /**
     * Cancel exemption request
     * 
     * @param id Exemption approval ID
     * @return Updated exemption approval record
     */
    StaffingExemptionApproval cancelExemption(Long id);
    
    /**
     * Get exemption approval by ID
     * 
     * @param id Exemption approval ID
     * @return Exemption approval record
     */
    StaffingExemptionApproval getById(Long id);
    
    /**
     * Get exemption approval by exemption number
     * 
     * @param exemptionNo Exemption number
     * @return Exemption approval record
     */
    StaffingExemptionApproval getByExemptionNo(String exemptionNo);
    
    /**
     * Get active exemptions for organization
     * 
     * @param orgId Organization ID
     * @return List of active exemptions
     */
    List<StaffingExemptionApproval> getActiveExemptionsByOrg(Long orgId);
    
    /**
     * Get active exemptions for position
     * 
     * @param positionId Position ID
     * @return List of active exemptions
     */
    List<StaffingExemptionApproval> getActiveExemptionsByPosition(Long positionId);
    
    /**
     * Get all active exemptions
     * 
     * @return List of all active exemptions
     */
    List<StaffingExemptionApproval> getAllActiveExemptions();
    
    /**
     * Get expired exemptions
     * 
     * @return List of expired exemptions
     */
    List<StaffingExemptionApproval> getExpiredExemptions();
    
    /**
     * Check if position has active exemption
     * 
     * @param positionId Position ID
     * @param exemptionType Exemption type (QUOTA/SPAN)
     * @return true if active exemption exists
     */
    boolean hasActiveExemption(Long positionId, String exemptionType);
    
    /**
     * Get exemption value for position
     * 
     * @param positionId Position ID
     * @param exemptionType Exemption type (QUOTA/SPAN)
     * @return Exemption value or null if no active exemption
     */
    Integer getExemptionValue(Long positionId, String exemptionType);
    
    /**
     * Update compliance status
     * 
     * @param id Exemption approval ID
     * @param complianceStatus New compliance status
     * @param complianceNotes Compliance notes
     * @return Updated exemption approval record
     */
    StaffingExemptionApproval updateComplianceStatus(Long id, String complianceStatus, String complianceNotes);
    
    /**
     * Process expired exemptions
     * Changes status to EXPIRED and sends notifications
     * 
     * @return Number of exemptions processed
     */
    int processExpiredExemptions();
    
    /**
     * Generate exemption statistics report
     * 
     * @param orgId Organization ID (optional, null for all)
     * @return Statistics map with counts and details
     */
    Map<String, Object> generateStatistics(Long orgId);
}