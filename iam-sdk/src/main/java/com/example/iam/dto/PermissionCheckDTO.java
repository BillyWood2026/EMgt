package com.example.iam.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PermissionCheckDTO {
    
    private Long userId;
    private String employeeId;
    private String permissionCode;
    private String moduleCode;
    private String action;
    private String resourceType;
    private String resourceId;
    private Long orgId;
    private String requestIp;
    private Map<String, Object> attributes;
}