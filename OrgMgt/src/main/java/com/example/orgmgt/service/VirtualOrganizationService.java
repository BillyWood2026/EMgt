package com.example.orgmgt.service;

import com.example.orgmgt.entity.VirtualOrganization;
import com.example.orgmgt.entity.VirtualOrgMember;
import com.example.orgmgt.entity.VirtualOrgHistory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Virtual Organization Service Interface
 * Manages cross-department virtual organizations for flexible collaboration
 */
public interface VirtualOrganizationService {
    
    /**
     * Get virtual organization tree
     */
    List<Map<String, Object>> getVirtualOrgTree();
    
    /**
     * Get virtual organization list with filters
     */
    List<VirtualOrganization> getVirtualOrgList(String keyword, String orgType, String status, Integer page, Integer pageSize);
    
    /**
     * Get virtual organization by ID
     */
    VirtualOrganization getVirtualOrgById(Long id);
    
    /**
     * Get virtual organization by code
     */
    VirtualOrganization getVirtualOrgByCode(String virtualOrgCode);
    
    /**
     * Create new virtual organization
     */
    Map<String, Object> createVirtualOrg(VirtualOrganization virtualOrg, List<VirtualOrgMember> members, String initiator);
    
    /**
     * Update virtual organization
     */
    Map<String, Object> updateVirtualOrg(Long id, VirtualOrganization virtualOrg, String operator);
    
    /**
     * Add member to virtual organization
     */
    Map<String, Object> addMember(Long virtualOrgId, VirtualOrgMember member, String operator);
    
    /**
     * Remove member from virtual organization
     */
    Map<String, Object> removeMember(Long virtualOrgId, String employeeId, String reason, String operator);
    
    /**
     * Change leader
     */
    Map<String, Object> changeLeader(Long virtualOrgId, String newLeaderId, String newLeaderName, String reason, String operator);
    
    /**
     * Extend expiration date
     */
    Map<String, Object> extendExpiration(Long virtualOrgId, LocalDateTime newExpireDate, String reason, String operator);
    
    /**
     * Disband virtual organization
     */
    Map<String, Object> disbandVirtualOrg(Long id, String reason, String workSummaryPath, String operator);
    
    /**
     * Get members of virtual organization
     */
    List<VirtualOrgMember> getMembers(Long virtualOrgId);
    
    /**
     * Get expiring virtual organizations
     */
    List<VirtualOrganization> getExpiringOrgs(int daysThreshold);
    
    /**
     * Get history
     */
    List<VirtualOrgHistory> getHistory(String virtualOrgCode, String changeType, Integer page, Integer pageSize);
    
    /**
     * Get history by ID
     */
    VirtualOrgHistory getHistoryById(Long id);
    
    /**
     * Get virtual organizations for employee
     */
    List<VirtualOrganization> getVirtualOrgsByEmployee(String employeeId);
    
    /**
     * Get statistics
     */
    Map<String, Object> getVirtualOrgStats();
    
    /**
     * Batch import members
     */
    Map<String, Object> batchImportMembers(Long virtualOrgId, List<Map<String, Object>> memberList, String operator);
    
    /**
     * Batch export
     */
    List<VirtualOrganization> batchExport(List<Long> virtualOrgIds);
    
    /**
     * Generate virtual org code
     */
    String generateVirtualOrgCode(String orgType);
}