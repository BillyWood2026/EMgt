package com.example.iam.entity.org;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * User-Organization Relationship
 * Supports multiple org assignments per user
 */
@Data
@TableName("iam_user_org_relation")
public class IamUserOrgRelation {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** User ID */
    private Long userId;
    
    /** Employee ID */
    private String employeeId;
    
    /** Organization ID */
    private Long orgId;
    
    /** Organization code */
    private String orgCode;
    
    /** Assignment type: PRIMARY, SECONDARY, TEMPORARY, PROJECT */
    private String assignmentType;
    
    /** Job title in this organization */
    private String jobTitle;
    
    /** Job level */
    private String jobLevel;
    
    /** Work type: FULL_TIME, PART_TIME, REMOTE, CONTRACTOR */
    private String workType;
    
    /** Start date in this org */
    private LocalDateTime startDate;
    
    /** End date (null for permanent) */
    private LocalDateTime endDate;
    
    /** Status: ACTIVE, INACTIVE, PENDING */
    private String status;
    
    /** Is current primary org */
    private Boolean isPrimary;
    
    /** Project code (if project assignment) */
    private String projectCode;
    
    /** Sort order */
    private Integer sortOrder;
    
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
