package com.example.iam.adapter;

/**
 * Module Permission Adapter Interface
 * 
 * This interface defines the unified permission checking API
 * that all modules should use instead of their local implementations.
 * 
 * Modules should call IamPermissionAdapter.checkPermission() rather than
 * implementing their own hardcoded permission logic.
 * 
 * Integration Methods:
 * 1. Direct Spring Bean injection (same JVM)
 * 2. Feign Client remote call (microservices)
 * 3. REST API call via API Gateway
 */
public interface IamModulePermissionAdapter {
    
    // ==================== Phase 1: Basic RBAC ====================
    
    /**
     * Check if user has permission for a specific operation
     * 
     * @param userId User ID
     * @param moduleCode Module code (e.g., ORG, STAFF, SALARY, HOLIDAY)
     * @param permissionCode Permission code (e.g., VIEW, CREATE, EDIT, DELETE, APPROVE)
     * @return true if allowed, false otherwise
     */
    boolean checkPermission(Long userId, String moduleCode, String permissionCode);
    
    /**
     * Check if user has permission with organization context
     * 
     * @param userId User ID
     * @param moduleCode Module code
     * @param permissionCode Permission code
     * @param orgId Organization ID (for data scope check)
     * @return true if allowed, false otherwise
     */
    boolean checkPermissionWithOrg(Long userId, String moduleCode, String permissionCode, Long orgId);
    
    /**
     * Get user permission summary
     * 
     * @param userId User ID
     * @return Permission summary including roles and effective permissions
     */
    PermissionSummary getPermissionSummary(Long userId);
    
    // ==================== Phase 2: Data Permissions ====================
    
    /**
     * Get data scope filter for query
     * Returns SQL WHERE clause fragment or expression
     * 
     * @param userId User ID
     * @param moduleCode Module code
     * @param entityName Entity/table name
     * @return Data scope filter or null if no restriction
     */
    String getDataScopeFilter(Long userId, String moduleCode, String entityName);
    
    /**
     * Get column-level permissions
     * Returns JSON map of field name -> permission level (READ, WRITE, HIDDEN)
     * 
     * @param userId User ID
     * @param moduleCode Module code
     * @param entityName Entity/table name
     * @return Column permissions JSON or null
     */
    String getColumnPermissions(Long userId, String moduleCode, String entityName);
    
    /**
     * Check if user can access specific data record
     * 
     * @param userId User ID
     * @param moduleCode Module code
     * @param entityName Entity name
     * @param recordId Record ID
     * @param recordOrgId Record's organization ID
     * @return true if allowed
     */
    boolean canAccessRecord(Long userId, String moduleCode, String entityName, String recordId, Long recordOrgId);
    
    /**
     * Evaluate conditional permission
     * 
     * @param userId User ID
     * @param conditionExpression SpEL expression
     * @return true if condition is satisfied
     */
    boolean evaluateCondition(Long userId, String conditionExpression);
    
    // ==================== Phase 3: Audit & Risk ====================
    
    /**
     * Get user risk score
     * 
     * @param userId User ID
     * @return Risk score 0-100
     */
    Integer getUserRiskScore(Long userId);
    
    /**
     * Check if current operation triggers risk alert
     * 
     * @param userId User ID
     * @param moduleCode Module code
     * @param permissionCode Permission code
     * @param resourceType Resource type being accessed
     * @return true if risk is detected
     */
    boolean checkRiskAlert(Long userId, String moduleCode, String permissionCode, String resourceType);
    
    /**
     * Record permission access for audit
     * 
     * @param userId User ID
     * @param moduleCode Module code
     * @param permissionCode Permission code
     * @param resourceType Resource type
     * @param resourceId Resource ID
     * @param result Access result (ALLOWED, DENIED)
     */
    void recordAccess(Long userId, String moduleCode, String permissionCode, 
                      String resourceType, String resourceId, String result);
    
    // ==================== Data Classes ====================
    
    /**
     * Permission Summary
     */
    class PermissionSummary {
        private Long userId;
        private String employeeId;
        private String[] directRoles;
        private String[] effectiveRoles;
        private String[] directPermissions;
        private String[] effectivePermissions;
        private Integer dataPermissionLevel;
        private String orgScope; // GLOBAL, OWN_DEPT, OWN_SECTION, PERSONAL
        private Long primaryOrgId;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public String[] getDirectRoles() { return directRoles; }
        public void setDirectRoles(String[] directRoles) { this.directRoles = directRoles; }
        public String[] getEffectiveRoles() { return effectiveRoles; }
        public void setEffectiveRoles(String[] effectiveRoles) { this.effectiveRoles = effectiveRoles; }
        public String[] getDirectPermissions() { return directPermissions; }
        public void setDirectPermissions(String[] directPermissions) { this.directPermissions = directPermissions; }
        public String[] getEffectivePermissions() { return effectivePermissions; }
        public void setEffectivePermissions(String[] effectivePermissions) { this.effectivePermissions = effectivePermissions; }
        public Integer getDataPermissionLevel() { return dataPermissionLevel; }
        public void setDataPermissionLevel(Integer dataPermissionLevel) { this.dataPermissionLevel = dataPermissionLevel; }
        public String getOrgScope() { return orgScope; }
        public void setOrgScope(String orgScope) { this.orgScope = orgScope; }
        public Long getPrimaryOrgId() { return primaryOrgId; }
        public void setPrimaryOrgId(Long primaryOrgId) { this.primaryOrgId = primaryOrgId; }
    }
}
