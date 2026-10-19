package com.example.iam.entity.rbac;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * User-Role Assignment
 * Direct role assignment to user
 */
@Data
@TableName("iam_user_role")
public class IamUserRole {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** User ID */
    private Long userId;
    
    /** Employee ID */
    private String employeeId;
    
    /** Role ID */
    private Long roleId;
    
    /** Role code */
    private String roleCode;
    
    /** Scope type: GLOBAL, ORG, PROJECT */
    private String scopeType;
    
    /** Scope organization ID (if scope is ORG) */
    private Long scopeOrgId;
    
    /** Scope project code (if scope is PROJECT) */
    private String scopeProjectCode;
    
    /** Data permission scope (JSON array of org IDs or ranges) */
    private String dataScope;
    
    /** Assignment type: DIRECT, INHERITED, MANAGER_ASSIGNED, SYSTEM */
    private String assignmentType;
    
    /** Assigned by (manager or system) */
    private String assignedBy;
    
    /** Start date */
    private LocalDateTime startDate;
    
    /** End date (null for permanent) */
    private LocalDateTime endDate;
    
    /** Status: ACTIVE, INACTIVE, EXPIRED, REVOKED */
    private String status;
    
    /** Reason for assignment */
    private String reason;
    
    /** Ticket number (if approval required) */
    private String ticketNumber;
    
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
