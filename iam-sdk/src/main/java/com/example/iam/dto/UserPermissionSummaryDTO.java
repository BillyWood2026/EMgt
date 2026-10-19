package com.example.iam.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class UserPermissionSummaryDTO {
    
    private Long userId;
    private String employeeId;
    private String username;
    
    private List<RoleAssignmentDTO> directRoles;
    private List<RoleAssignmentDTO> effectiveRoles;
    private List<String> directPermissions;
    private List<String> effectivePermissions;
    private Integer dataPermissionLevel;
    private String dataScopeFilter;
    private String orgPath;
    private Map<String, List<String>> modulePermissions;
    private String orgScope;
    private Long primaryOrgId;
    
    @Data
    public static class RoleAssignmentDTO {
        private Long roleId;
        private String roleCode;
        private String roleName;
        private String scopeType;
        private Long scopeOrgId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String status;
        private String assignmentType;
    }
}