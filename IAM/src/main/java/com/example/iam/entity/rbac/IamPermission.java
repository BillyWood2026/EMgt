package com.example.iam.entity.rbac;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Permission Entity
 * Defines granular permissions for system resources
 */
@Data
@TableName("iam_permission")
public class IamPermission {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** Permission code (unique identifier) */
    private String permissionCode;
    
    /** Permission name */
    private String permissionName;
    
    /** Permission name in English */
    private String permissionNameEn;
    
    /** Permission description */
    private String description;
    
    /** Permission type: MENU, BUTTON, API, DATA, FIELD */
    private String permissionType;
    
    /** Resource type this permission protects */
    private String resourceType;
    
    /** Resource identifier */
    private String resourceCode;
    
    /** Action: CREATE, READ, UPDATE, DELETE, APPROVE, EXECUTE, EXPORT, IMPORT */
    private String action;
    
    /** Parent permission ID (for hierarchical permissions) */
    private Long parentId;
    
    /** Permission path */
    private String permissionPath;
    
    /** Module code this permission belongs to */
    private String moduleCode;
    
    /** Sub-module code */
    private String subModuleCode;
    
    /** Sort order */
    private Integer sortOrder;
    
    /** Icon (for menu permissions) */
    private String icon;
    
    /** Route path (for menu permissions) */
    private String routePath;
    
    /** Component path (for frontend) */
    private String componentPath;
    
    /** Whether this is a dangerous permission */
    private Boolean isDangerous;
    
    /** Requires confirmation for this permission */
    private Boolean requiresConfirmation;
    
    /** Status: ACTIVE, INACTIVE */
    private String status;
    
    /** System built-in permission (cannot be modified) */
    private Boolean isSystemBuiltIn;
    
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
