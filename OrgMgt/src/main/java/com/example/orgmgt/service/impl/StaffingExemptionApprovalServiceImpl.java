package com.example.orgmgt.service.impl;

import com.example.orgmgt.entity.StaffingExemptionApproval;
import com.example.orgmgt.mapper.StaffingExemptionApprovalMapper;
import com.example.orgmgt.service.StaffingExemptionApprovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PLAN-007: Staffing Exemption Approval Service Implementation
 * Implements exemption approval workflow for staffing quota and management span violations
 * 
 * Workflow:
 * 1. Submit exemption request with justification
 * 2. Multi-level approval (DEPT_HEAD -> HR_REVIEW -> FINAL_APPROVAL)
 * 3. Set exemption period and monitoring requirements
 * 4. Track compliance during exemption period
 * 5. Auto-expire after exemption period ends
 */
@Service
public class StaffingExemptionApprovalServiceImpl implements StaffingExemptionApprovalService {

    private static final Logger logger = LoggerFactory.getLogger(StaffingExemptionApprovalServiceImpl.class);

    @Autowired
    private StaffingExemptionApprovalMapper exemptionApprovalMapper;

    /**
     * Generate exemption number with timestamp
     */
    private String generateExemptionNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "EXM-" + timestamp + "-" + uuid;
    }

    @Override
    @Transactional
    public StaffingExemptionApproval submitExemptionRequest(StaffingExemptionApproval exemption) {
        // Generate exemption number
        exemption.setExemptionNo(generateExemptionNo());
        
        // Set initial status
        exemption.setApprovalStatus(StaffingExemptionApproval.STATUS_PENDING);
        exemption.setCurrentNode("DEPT_HEAD");
        exemption.setComplianceStatus(StaffingExemptionApproval.COMPLIANCE_MONITORING);
        
        // Set timestamps
        exemption.setSubmitTime(LocalDateTime.now());
        exemption.setCreateTime(LocalDateTime.now());
        
        // Validate required fields
        if (exemption.getOrgId() == null) {
            throw new IllegalArgumentException("Organization ID is required");
        }
        if (exemption.getExemptionType() == null) {
            throw new IllegalArgumentException("Exemption type is required");
        }
        if (exemption.getBusinessJustification() == null || exemption.getBusinessJustification().isEmpty()) {
            throw new IllegalArgumentException("Business justification is required");
        }
        if (exemption.getEffectiveDate() == null) {
            throw new IllegalArgumentException("Effective date is required");
        }
        if (exemption.getExpireDate() == null) {
            throw new IllegalArgumentException("Expire date is required");
        }
        
        // Validate date range
        if (exemption.getExpireDate().isBefore(exemption.getEffectiveDate())) {
            throw new IllegalArgumentException("Expire date must be after effective date");
        }
        
        // Insert exemption request
        exemptionApprovalMapper.insert(exemption);
        
        logger.info("Submitted exemption request: {} for organization: {}", 
            exemption.getExemptionNo(), exemption.getOrgCode());
        
        // TODO: Send notification to first approver (DEPT_HEAD)
        // notificationService.sendExemptionApprovalNotification(exemption);
        
        return exemption;
    }

    @Override
    @Transactional
    public StaffingExemptionApproval approveExemption(Long id, String approverId, String approverName) {
        StaffingExemptionApproval exemption = exemptionApprovalMapper.selectById(id);
        if (exemption == null) {
            throw new IllegalArgumentException("Exemption approval not found");
        }
        
        // Check if already processed
        if (!StaffingExemptionApproval.STATUS_PENDING.equals(exemption.getApprovalStatus())) {
            throw new IllegalStateException("Exemption approval is not in pending status");
        }
        
        // Update approval information
        exemption.setApprovalStatus(StaffingExemptionApproval.STATUS_APPROVED);
        exemption.setApproverId(approverId);
        exemption.setApproverName(approverName);
        exemption.setApprovalTime(LocalDateTime.now());
        exemption.setUpdateTime(LocalDateTime.now());
        
        // Update exemption approval
        exemptionApprovalMapper.updateById(exemption);
        
        logger.info("Approved exemption request: {} by approver: {}", 
            exemption.getExemptionNo(), approverName);
        
        // TODO: Send approval notification to initiator
        // notificationService.sendExemptionApprovedNotification(exemption);
        
        return exemption;
    }

    @Override
    @Transactional
    public StaffingExemptionApproval rejectExemption(Long id, String rejectReason) {
        StaffingExemptionApproval exemption = exemptionApprovalMapper.selectById(id);
        if (exemption == null) {
            throw new IllegalArgumentException("Exemption approval not found");
        }
        
        // Check if already processed
        if (!StaffingExemptionApproval.STATUS_PENDING.equals(exemption.getApprovalStatus())) {
            throw new IllegalStateException("Exemption approval is not in pending status");
        }
        
        // Update rejection information
        exemption.setApprovalStatus(StaffingExemptionApproval.STATUS_REJECTED);
        exemption.setRejectReason(rejectReason);
        exemption.setUpdateTime(LocalDateTime.now());
        
        // Update exemption approval
        exemptionApprovalMapper.updateById(exemption);
        
        logger.info("Rejected exemption request: {} with reason: {}", 
            exemption.getExemptionNo(), rejectReason);
        
        // TODO: Send rejection notification to initiator
        // notificationService.sendExemptionRejectedNotification(exemption);
        
        return exemption;
    }

    @Override
    @Transactional
    public StaffingExemptionApproval cancelExemption(Long id) {
        StaffingExemptionApproval exemption = exemptionApprovalMapper.selectById(id);
        if (exemption == null) {
            throw new IllegalArgumentException("Exemption approval not found");
        }
        
        // Check if can be cancelled
        if (!StaffingExemptionApproval.STATUS_PENDING.equals(exemption.getApprovalStatus())) {
            throw new IllegalStateException("Only pending exemptions can be cancelled");
        }
        
        // Update status
        exemption.setApprovalStatus(StaffingExemptionApproval.STATUS_CANCELLED);
        exemption.setUpdateTime(LocalDateTime.now());
        
        // Update exemption approval
        exemptionApprovalMapper.updateById(exemption);
        
        logger.info("Cancelled exemption request: {}", exemption.getExemptionNo());
        
        return exemption;
    }

    @Override
    public StaffingExemptionApproval getById(Long id) {
        return exemptionApprovalMapper.selectById(id);
    }

    @Override
    public StaffingExemptionApproval getByExemptionNo(String exemptionNo) {
        return exemptionApprovalMapper.findByExemptionNo(exemptionNo);
    }

    @Override
    public List<StaffingExemptionApproval> getActiveExemptionsByOrg(Long orgId) {
        return exemptionApprovalMapper.findByOrgId(orgId).stream()
            .filter(e -> StaffingExemptionApproval.STATUS_APPROVED.equals(e.getApprovalStatus()))
            .filter(e -> e.getExpireDate().isAfter(LocalDateTime.now()))
            .toList();
    }

    @Override
    public List<StaffingExemptionApproval> getActiveExemptionsByPosition(Long positionId) {
        return exemptionApprovalMapper.findByPositionId(positionId).stream()
            .filter(e -> StaffingExemptionApproval.STATUS_APPROVED.equals(e.getApprovalStatus()))
            .filter(e -> e.getExpireDate().isAfter(LocalDateTime.now()))
            .toList();
    }

    @Override
    public List<StaffingExemptionApproval> getAllActiveExemptions() {
        return exemptionApprovalMapper.findActiveExemptions();
    }

    @Override
    public List<StaffingExemptionApproval> getExpiredExemptions() {
        return exemptionApprovalMapper.findExpiredExemptions();
    }

    @Override
    public boolean hasActiveExemption(Long positionId, String exemptionType) {
        List<StaffingExemptionApproval> exemptions = getActiveExemptionsByPosition(positionId);
        return exemptions.stream()
            .anyMatch(e -> exemptionType.equals(e.getExemptionType()));
    }

    @Override
    public Integer getExemptionValue(Long positionId, String exemptionType) {
        List<StaffingExemptionApproval> exemptions = getActiveExemptionsByPosition(positionId);
        return exemptions.stream()
            .filter(e -> exemptionType.equals(e.getExemptionType()))
            .findFirst()
            .map(StaffingExemptionApproval::getExemptionValue)
            .orElse(null);
    }

    @Override
    @Transactional
    public StaffingExemptionApproval updateComplianceStatus(Long id, String complianceStatus, String complianceNotes) {
        StaffingExemptionApproval exemption = exemptionApprovalMapper.selectById(id);
        if (exemption == null) {
            throw new IllegalArgumentException("Exemption approval not found");
        }
        
        // Update compliance information
        exemption.setComplianceStatus(complianceStatus);
        exemption.setComplianceNotes(complianceNotes);
        exemption.setUpdateTime(LocalDateTime.now());
        
        // Update exemption approval
        exemptionApprovalMapper.updateById(exemption);
        
        logger.info("Updated compliance status for exemption: {} to {}", 
            exemption.getExemptionNo(), complianceStatus);
        
        return exemption;
    }

    @Override
    @Transactional
    public int processExpiredExemptions() {
        List<StaffingExemptionApproval> expiredExemptions = getExpiredExemptions();
        int processedCount = 0;
        
        for (StaffingExemptionApproval exemption : expiredExemptions) {
            try {
                // Update status to expired
                exemption.setApprovalStatus(StaffingExemptionApproval.STATUS_EXPIRED);
                exemption.setUpdateTime(LocalDateTime.now());
                exemptionApprovalMapper.updateById(exemption);
                
                logger.info("Expired exemption: {} for organization: {}", 
                    exemption.getExemptionNo(), exemption.getOrgCode());
                
                // TODO: Send expiration notification
                // notificationService.sendExemptionExpiredNotification(exemption);
                
                processedCount++;
                
            } catch (Exception e) {
                logger.error("Failed to process expired exemption: {}", exemption.getExemptionNo(), e);
            }
        }
        
        if (processedCount > 0) {
            logger.info("Processed {} expired exemptions", processedCount);
        }
        
        return processedCount;
    }

    @Override
    public Map<String, Object> generateStatistics(Long orgId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // Get all exemptions (filtered by org if provided)
        List<StaffingExemptionApproval> allExemptions = (orgId != null) 
            ? exemptionApprovalMapper.findByOrgId(orgId)
            : exemptionApprovalMapper.selectList(null);
        
        // Count by status
        long pendingCount = allExemptions.stream()
            .filter(e -> StaffingExemptionApproval.STATUS_PENDING.equals(e.getApprovalStatus()))
            .count();
        
        long approvedCount = allExemptions.stream()
            .filter(e -> StaffingExemptionApproval.STATUS_APPROVED.equals(e.getApprovalStatus()))
            .count();
        
        long rejectedCount = allExemptions.stream()
            .filter(e -> StaffingExemptionApproval.STATUS_REJECTED.equals(e.getApprovalStatus()))
            .count();
        
        long expiredCount = allExemptions.stream()
            .filter(e -> StaffingExemptionApproval.STATUS_EXPIRED.equals(e.getApprovalStatus()))
            .count();
        
        // Count by type
        long quotaExemptions = allExemptions.stream()
            .filter(e -> StaffingExemptionApproval.EXEMPTION_TYPE_QUOTA.equals(e.getExemptionType()))
            .count();
        
        long spanExemptions = allExemptions.stream()
            .filter(e -> StaffingExemptionApproval.EXEMPTION_TYPE_SPAN.equals(e.getExemptionType()))
            .count();
        
        // Count by compliance status
        long compliantCount = allExemptions.stream()
            .filter(e -> StaffingExemptionApproval.COMPLIANCE_COMPLIANT.equals(e.getComplianceStatus()))
            .count();
        
        long violatedCount = allExemptions.stream()
            .filter(e -> StaffingExemptionApproval.COMPLIANCE_VIOLATED.equals(e.getComplianceStatus()))
            .count();
        
        // Build statistics map
        statistics.put("totalExemptions", allExemptions.size());
        statistics.put("pendingCount", pendingCount);
        statistics.put("approvedCount", approvedCount);
        statistics.put("rejectedCount", rejectedCount);
        statistics.put("expiredCount", expiredCount);
        statistics.put("quotaExemptions", quotaExemptions);
        statistics.put("spanExemptions", spanExemptions);
        statistics.put("compliantCount", compliantCount);
        statistics.put("violatedCount", violatedCount);
        statistics.put("orgId", orgId);
        
        return statistics;
    }
}