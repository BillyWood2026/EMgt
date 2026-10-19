package com.example.iam.dto;

import lombok.Data;

@Data
public class PermissionCheckResultDTO {
    
    private boolean allowed;
    private String reason;
    private String permissionCode;
    private String[] grantedPermissions;
    private String dataScopeFilter;
    private String rowFilterExpression;
    private String columnPermissions;
    private String timeRestriction;
    private String riskLevel;
    private Boolean mfaVerified;
    private String decision;
    private Long evaluationTimeMs;
    private String requestId;
}