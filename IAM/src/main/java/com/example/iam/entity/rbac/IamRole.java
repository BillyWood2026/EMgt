package com.example.iam.entity.rbac;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Role Entity
 * Role-based access control
 */
@Data
@TableName("iam_role")
public class IamRole {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** Role code (unique identifier) */
    private String roleCode;
    
    /** Role name */
    private String roleName;
    
    /** Role name in English */
    private String roleNameEn;
    
    /** Role description */
    private String description;
    
    /** Role category: SYSTEM, BUSINESS, PROJECT, TEMPORARY */
    private String roleCategory;
    
    /** Role type: MANAGEMENT, FUNCTIONAL, DATA, GUEST */
    private String roleType;
    
    /** Organization scope: GLOBAL, ORG_LEVEL_1, ORG_LEVEL_2, ORG_LEVEL_3, ORG_LEVEL_4, ORG_LEVEL_5 */
    private String orgScope;
    
    /** Data permission level range (min) */
    private Integer dataLevelMin;
    
    /** Data permission level range (max) */
    private Integer dataLevelMax;
    
    /** Whether this role can assign other users */
    private Boolean canAssignUsers;
    
    /** Whether this role can modify permissions */
    private Boolean canModifyPermissions;
    
    /** Whether this role can grant roles */
    private Boolean canGrantRoles;
    
    /** Whether this is a system built-in role */
    private Boolean isSystemBuiltIn;
    
    /** Whether this is a super admin role */
    private Boolean isSuperAdminRole;
    
    /** Whether this is a public role (can be assigned by any manager) */
    private Boolean isPublicRole;
    
    /** Sort order */
    private Integer sortOrder;
    
    /** Status: ACTIVE, INACTIVE, ARCHIVED */
    private String status;
    
    /** Effective date */
    private LocalDateTime effectiveDate;
    
    /** Expire date */
    private LocalDateTime expireDate;
    
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
