package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * COMM-001: Operation Log Entity
 * Unified operation log for all organization management activities
 * Features:
 * - Immutable records (cannot be deleted or modified)
 * - Full-text search support
 * - Comprehensive audit trail
 * - Permanent storage
 * 
 * Usage:
 * - Record all CRUD operations
 * - Record approval activities
 * - Record status changes
 * - Record data exports/imports
 * - Record system configuration changes
 */
@Data
@TableName("operation_log")
public class OperationLog {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("log_no")
    private String logNo;
    
    @TableField("business_module")
    private String businessModule;
    
    @TableField("business_id")
    private Long businessId;
    
    @TableField("business_code")
    private String businessCode;
    
    @TableField("business_name")
    private String businessName;
    
    @TableField("operation_type")
    private String operationType;
    
    @TableField("operation_content")
    private String operationContent;
    
    @TableField("operator_id")
    private String operatorId;
    
    @TableField("operator_name")
    private String operatorName;
    
    @TableField("operator_org_id")
    private Long operatorOrgId;
    
    @TableField("operator_org_code")
    private String operatorOrgCode;
    
    @TableField("operator_ip")
    private String operatorIp;
    
    @TableField("before_data")
    private String beforeData;
    
    @TableField("after_data")
    private String afterData;
    
    @TableField("change_summary")
    private String changeSummary;
    
    @TableField("attachment_path")
    private String attachmentPath;
    
    @TableField("result_status")
    private String resultStatus;
    
    @TableField("result_message")
    private String resultMessage;
    
    @TableField("operation_time")
    private LocalDateTime operationTime;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    // Business module constants
    public static final String MODULE_ORG = "ORGANIZATION";
    public static final String MODULE_POSITION = "POSITION";
    public static final String MODULE_POSITION_HIERARCHY = "POSITION_HIERARCHY";
    public static final String MODULE_COMMAND_CHAIN = "COMMAND_CHAIN";
    public static final String MODULE_STAFFING = "STAFFING";
    public static final String MODULE_VIRTUAL_ORG = "VIRTUAL_ORG";
    public static final String MODULE_JOB_DESCRIPTION = "JOB_DESCRIPTION";
    public static final String MODULE_APPROVAL = "APPROVAL";
    public static final String MODULE_PERMISSION = "PERMISSION";
    public static final String MODULE_SYSTEM = "SYSTEM";
    
    // Operation type constants
    public static final String OP_CREATE = "CREATE";
    public static final String OP_UPDATE = "UPDATE";
    public static final String OP_DELETE = "DELETE";
    public static final String OP_QUERY = "QUERY";
    public static final String OP_APPROVE = "APPROVE";
    public static final String OP_REJECT = "REJECT";
    public static final String OP_SUBMIT = "SUBMIT";
    public static final String OP_CANCEL = "CANCEL";
    public static final String OP_ENABLE = "ENABLE";
    public static final String OP_DISABLE = "DISABLE";
    public static final String OP_EXPORT = "EXPORT";
    public static final String OP_IMPORT = "IMPORT";
    public static final String OP_SYNC = "SYNC";
    public static final String OP_ARCHIVE = "ARCHIVE";
    public static final String OP_EXECUTE = "EXECUTE";
    
    // Result status constants
    public static final String RESULT_SUCCESS = "SUCCESS";
    public static final String RESULT_FAILED = "FAILED";
    public static final String RESULT_PENDING = "PENDING";
}