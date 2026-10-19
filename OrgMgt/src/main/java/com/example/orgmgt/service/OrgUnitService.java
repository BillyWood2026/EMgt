package com.example.orgmgt.service;

import com.example.orgmgt.entity.OrgUnit;
import java.util.List;
import java.util.Map;

/**
 * Organization Unit Service Interface
 * Provides five-level organization hierarchy management
 */
public interface OrgUnitService {
    
    /**
     * Get organization tree data
     */
    List<Map<String, Object>> getOrgTree();
    
    /**
     * Get organization list with filters
     */
    List<OrgUnit> getOrgList(String keyword, String orgLevel, String status, Integer page, Integer pageSize);
    
    /**
     * Get organization by ID
     */
    OrgUnit getOrgById(Long id);
    
    /**
     * Get organization by code
     */
    OrgUnit getOrgByCode(String orgCode);
    
    /**
     * Create new organization
     * Validates: parent level constraint, code uniqueness, name uniqueness
     */
    Map<String, Object> createOrg(OrgUnit orgUnit, String initiator);
    
    /**
     * Update organization
     */
    Map<String, Object> updateOrg(Long id, OrgUnit orgUnit, String operator);
    
    /**
     * Delete organization (only allowed for empty childless orgs)
     */
    Map<String, Object> deleteOrg(Long id, String operator);
    
    /**
     * Toggle organization status
     */
    Map<String, Object> toggleStatus(Long id, String newStatus, String operator);
    
    /**
     * Move organization to new parent
     */
    Map<String, Object> moveOrg(Long id, Long newParentId, String operator);
    
    /**
     * Validate organization creation constraints
     */
    Map<String, Object> validateCreate(OrgUnit orgUnit);
    
    /**
     * Generate organization code based on parent and level
     */
    String generateOrgCode(Long parentId);
    
    /**
     * Check if organization has children
     */
    boolean hasChildren(Long orgId);
    
    /**
     * Get organization statistics
     */
    Map<String, Object> getOrgStats();
    
    /**
     * Batch import organizations
     */
    Map<String, Object> batchImport(List<Map<String, Object>> dataList);
    
    /**
     * Export organizations
     */
    List<OrgUnit> batchExport(List<Long> orgIds);
}