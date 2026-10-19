package com.example.orgmgt.adapter;

import java.util.List;
import java.util.Map;

/**
 * IAM Permission Adapter Interface
 *
 * Unified interface for delegating permission checks to the platform IAM middleware.
 * The actual implementation can be:
 * - Local fallback (LocalIamPermissionAdapter) for standalone deployment
 * - Feign client for remote IAM service in microservices architecture
 * - gRPC client for high-performance scenarios
 *
 * This adapter follows the platform unified permission specification:
 * - Function permission: role/user-based operation authorization
 * - Data permission: organization hierarchy-based data isolation
 * - Permission refresh: real-time sync on org/position/personnel changes
 */
public interface IamPermissionAdapter {

    /**
     * Check if user has specific function permission
     *
     * @param userId User ID
     * @param moduleCode Module code (e.g., ORG, POSITION, STAFFING)
     * @param functionCode Function code (e.g., CREATE, EDIT, DELETE, APPROVE)
     * @return true if user has permission
     */
    boolean checkPermission(String userId, String moduleCode, String functionCode);

    /**
     * Get user's effective permission summary
     *
     * @param userId User ID
     * @return Permission summary including effective permissions and data scope
     */
    PermissionSummary getPermissionSummary(String userId);

    /**
     * Get user's data scope filter for organization data isolation
     *
     * @param userId User ID
     * @param moduleCode Module code
     * @return Data scope: ALL, OWN_DEPT, OWN_SECTION, PERSONAL
     */
    String getDataScopeFilter(String userId, String moduleCode);

    /**
     * Get list of organization IDs accessible to the user
     *
     * @param userId User ID
     * @return List of accessible organization IDs
     */
    List<Long> getAccessibleOrgIds(String userId);

    /**
     * Refresh user permissions after org/position changes
     *
     * @param userId User ID
     * @param changeType Change type (ORG_CHANGE, POSITION_CHANGE, PERSONNEL_CHANGE)
     */
    void refreshPermission(String userId, String changeType);

    /**
     * Get role list from IAM
     *
     * @return List of roles with role code, name, and data scope
     */
    List<Map<String, Object>> getRoleList();

    /**
     * Get module function list for permission configuration
     *
     * @return List of modules with their functions
     */
    List<Map<String, Object>> getModuleFunctionList();

    /**
     * Permission Summary DTO
     */
    class PermissionSummary {
        private String userId;
        private List<String> effectivePermissions;
        private String dataScope;
        private List<String> roles;

        public PermissionSummary(String userId, List<String> effectivePermissions,
                                  String dataScope, List<String> roles) {
            this.userId = userId;
            this.effectivePermissions = effectivePermissions;
            this.dataScope = dataScope;
            this.roles = roles;
        }

        public String getUserId() { return userId; }
        public List<String> getEffectivePermissions() { return effectivePermissions; }
        public String getDataScope() { return dataScope; }
        public List<String> getRoles() { return roles; }
    }
}
