package com.example.orgmgt.service;

import java.util.List;
import java.util.Map;

/**
 * Organization Permission Service Interface
 * Provides role-based access control for organization management module
 */
public interface OrgPermissionService {
    
    /**
     * Check if user has permission for a specific function
     */
    boolean hasPermission(String userId, String moduleCode, String functionCode);
    
    /**
     * Get all permissions for a user
     */
    List<Map<String, Object>> getUserPermissions(String userId);
    
    /**
     * Get all permissions for a role
     */
    List<Map<String, Object>> getRolePermissions(String roleCode);
    
    /**
     * Grant permission to a role
     */
    void grantPermission(String roleCode, String moduleCode, String functionCode, String dataScope);
    
    /**
     * Revoke permission from a role
     */
    void revokePermission(String roleCode, String moduleCode, String functionCode);
    
    /**
     * Batch update permissions for a role
     */
    void batchUpdatePermissions(String roleCode, List<Map<String, Object>> permissions);
    
    /**
     * Get accessible organizations for a user
     */
    List<Long> getAccessibleOrgIds(String userId);
    
    /**
     * Check if user can access a specific organization
     */
    boolean canAccessOrg(String userId, Long orgId);
    
    /**
     * Get role list
     */
    List<Map<String, Object>> getRoleList();
    
    /**
     * Get module list with functions
     */
    List<Map<String, Object>> getModuleFunctionList();
    
    /**
     * Validate permission before operation
     */
    Map<String, Object> validateOperation(String userId, String moduleCode, String functionCode, Long orgId);
}