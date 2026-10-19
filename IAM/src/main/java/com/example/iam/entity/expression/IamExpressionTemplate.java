package com.example.iam.entity.expression;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Permission Expression Template
 * Defines reusable permission expression templates
 */
@Data
@TableName("iam_expression_template")
public class IamExpressionTemplate {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** Template code (unique) */
    private String templateCode;
    
    /** Template name */
    private String templateName;
    
    /** Template description */
    private String description;
    
    /** Expression type: DATA_SCOPE, CONDITIONAL, TIME_RANGE, ORG_HIERARCHY */
    private String expressionType;
    
    /** Expression syntax (SpEL or JSON) */
    private String expressionSyntax;
    
    /** Parameters definition (JSON schema) */
    private String parameterSchema;
    
    /** Default values (JSON) */
    private String defaultValues;
    
    /** Module code this template belongs to */
    private String moduleCode;
    
    /** Sub-module code */
    private String subModuleCode;
    
    /** Use case: DATA_FILTER, ACCESS_CONTROL, VALIDATION */
    private String useCase;
    
    /** Example expression */
    private String example;
    
    /** Status: ACTIVE, INACTIVE */
    private String status;
    
    /** Is system built-in */
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
