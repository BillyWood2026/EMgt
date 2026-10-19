package com.example.iam.entity.rbac;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Role-Permission Assignment
 * Defines which permissions are granted to a role
 */
@Data
@TableName("iam_role_permission")
public class IamRolePermission {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** Role ID */
    private Long roleId;
    
    /** Role code */
    private String roleCode;
    
    /** Permission ID */
    private Long permissionId;
    
    /** Permission code */
    private String permissionCode;
    
    /** Permission type (from permission entity) */
    private String permissionType;
    
    /** Grant type: GRANT, DENY */
    private String grantType;
    
    /** Scope type: GLOBAL, ORG, PROJECT */
    private String scopeType;
    
    /** Scope organization ID */
    private Long scopeOrgId;
    
    /** Scope project code */
    private String scopeProjectCode;
    
    /** Data scope expression (JSON) */
    private String dataScopeExpression;
    
    /** Conditions (JSON array of conditional expressions) */
    private String conditions;
    
    /** Priority (higher = takes precedence) */
    private Integer priority;
    
    /** Status: ACTIVE, INACTIVE */
    private String status;
    
    /** Is inherited from parent role */
    private Boolean isInherited;
    
    /** Parent role permission ID (if inherited) */
    private Long inheritedFromId;
    
    /** Audit fields */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    @Version
    private Long version;
    
    @TableLogic
    private Integer deleted;
}
