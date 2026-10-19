package com.example.iam.sdk;

public interface IamPermissionAdapter {

    boolean checkPermission(Long userId, String moduleCode, String permissionCode);
    boolean checkPermissionWithOrg(Long userId, String moduleCode, String permissionCode, Long orgId);
    PermissionSummary getPermissionSummary(Long userId);
    String getDataScopeFilter(Long userId, String moduleCode, String entityName);
    String getColumnPermissions(Long userId, String moduleCode, String entityName);
    boolean canAccessRecord(Long userId, String moduleCode, String entityName, String recordId, Long recordOrgId);
    boolean evaluateCondition(Long userId, String conditionExpression);
    Integer getUserRiskScore(Long userId);
    boolean checkRiskAlert(Long userId, String moduleCode, String permissionCode, String resourceType);
    void recordAccess(Long userId, String moduleCode, String permissionCode, String resourceType, String resourceId, String result);

    class PermissionSummary {
        private Long userId;
        private String employeeId;
        private String[] directRoles;
        private String[] effectiveRoles;
        private String[] directPermissions;
        private String[] effectivePermissions;
        private Integer dataPermissionLevel;
        private String orgScope;
        private Long primaryOrgId;

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
