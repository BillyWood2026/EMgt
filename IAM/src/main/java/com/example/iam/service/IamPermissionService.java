package com.example.iam.service;

import com.example.iam.dto.PermissionCheckDTO;
import com.example.iam.dto.PermissionCheckResultDTO;
import com.example.iam.dto.UserPermissionSummaryDTO;

/**
 * IAM Core Permission Service Interface
 * 
 * Phase 1: Basic RBAC and permission check
 * Phase 2: Data permission with expression engine
 * Phase 3: Audit and risk monitoring
 */
public interface IamPermissionService {
    
    // ==================== Phase 1: RBAC Basic Permissions ====================
    
    /**
     * Check if user has specific permission
     */
    PermissionCheckResultDTO checkPermission(PermissionCheckDTO request);
    
    /**
     * Check if user has any of the specified permissions
     */
    PermissionCheckResultDTO checkAnyPermission(PermissionCheckDTO request, String[] permissionCodes);
    
    /**
     * Check if user has all of the specified permissions
     */
    PermissionCheckResultDTO checkAllPermissions(PermissionCheckDTO request, String[] permissionCodes);
    
    /**
     * Get user permission summary
     */
    UserPermissionSummaryDTO getUserPermissionSummary(Long userId);
    
    /**
     * Get user permission summary by employee ID
     */
    UserPermissionSummaryDTO getUserPermissionSummaryByEmployeeId(String employeeId);
    
    // ==================== Phase 2: Data Permissions ====================
    
    /**
     * Get data scope filter for user and module
     * Returns SQL WHERE clause or expression
     */
    String getDataScopeFilter(Long userId, String moduleCode, String entityName);
    
    /**
     * Get column permissions for user and module
     * Returns map of field -> permission level
     */
    String getColumnPermissions(Long userId, String moduleCode, String entityName);
    
    /**
     * Get row filter expression
     */
    String getRowFilterExpression(Long userId, String moduleCode, String entityName);
    
    /**
     * Evaluate conditional permission
     */
    boolean evaluateConditionalPermission(Long userId, String conditionExpression);
    
    /**
     * Get time-based permission restriction
     */
    String getTimeRestriction(Long userId, String moduleCode);
    
    // ==================== Phase 3: Audit & Risk ====================
    
    /**
     * Record permission access
     */
    void recordPermissionAccess(PermissionCheckDTO request, PermissionCheckResultDTO result);
    
    /**
     * Check for risk events
     */
    boolean checkRiskEvents(Long userId, String action, String resourceType);
    
    /**
     * Get user risk score
     */
    Integer getUserRiskScore(Long userId);
}
