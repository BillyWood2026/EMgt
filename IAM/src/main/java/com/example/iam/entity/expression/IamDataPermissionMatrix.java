package com.example.iam.entity.expression;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Data Permission Matrix Configuration
 * Defines data-level permissions per module and role
 */
@Data
@TableName("iam_data_permission_matrix")
public class IamDataPermissionMatrix {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** Matrix code (unique per module) */
    private String matrixCode;
    
    /** Matrix name */
    private String matrixName;
    
    /** Module code (e.g., SALARY, STAFF, ATTENDANCE) */
    private String moduleCode;
    
    /** Sub-module code */
    private String subModuleCode;
    
    /** Entity name (database table or object) */
    private String entityName;
    
    /** Role ID (null for default matrix) */
    private Long roleId;
    
    /** Role code */
    private String roleCode;
    
    /** Organization scope type: GLOBAL, SPECIFIC, HIERARCHY, CUSTOM */
    private String orgScopeType;
    
    /** Organization scope value (JSON array or expression) */
    private String orgScopeValue;
    
    /** Data scope fields (JSON array of field names) */
    private String dataScopeFields;
    
    /** Row filter expression (SpEL) */
    private String rowFilterExpression;
    
    /** Column permissions (JSON map of field -> permission level) */
    private String columnPermissions;
    
    /** Condition rules (JSON array) */
    private String conditionRules;
    
    /** Time range restriction (JSON) */
    private String timeRangeRestriction;
    
    /** Status restriction (JSON array of allowed statuses) */
    private String statusRestriction;
    
    /** Amount range (for financial data) */
    private String amountRange;
    
    /** Priority (higher = takes precedence) */
    private Integer priority;
    
    /** Whether to deny by default (whitelist vs blacklist) */
    private Boolean denyByDefault;
    
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
