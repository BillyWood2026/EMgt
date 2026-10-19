package com.example.iam.entity.audit;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Permission Audit Log
 * Records all permission-related operations
 */
@Data
@TableName("iam_permission_audit_log")
public class IamPermissionAuditLog {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** Ticket number or request ID */
    private String ticketNumber;
    
    /** Operation type: GRANT, REVOKE, MODIFY, QUERY, LOGIN, LOGOUT, PASSWORD_CHANGE */
    private String operationType;
    
    /** Target type: USER, ROLE, PERMISSION, ORG, SYSTEM_CONFIG */
    private String targetType;
    
    /** Target ID */
    private Long targetId;
    
    /** Target code */
    private String targetCode;
    
    /** Target name */
    private String targetName;
    
    /** Actor type: USER, SYSTEM, API */
    private String actorType;
    
    /** Actor user ID (if actor is USER) */
    private Long actorUserId;
    
    /** Actor employee ID */
    private String actorEmployeeId;
    
    /** Actor username */
    private String actorUsername;
    
    /** Actor role IDs (JSON array) */
    private String actorRoleIds;
    
    /** Actor organization ID */
    private Long actorOrgId;
    
    /** Operation details (JSON) */
    private String operationDetails;
    
    /** Old value (JSON, for modifications) */
    private String oldValue;
    
    /** New value (JSON) */
    private String StringewValue;
    
    /** Change fields (comma separated) */
    private String changeFields;
    
    /** Reason for operation */
    private String reason;
    
    /** Approval ticket (if approval required) */
    private String approvalTicket;
    
    /** Approver employee ID */
    private String approverEmployeeId;
    
    /** Client IP */
    private String clientIp;
    
    /** Client location */
    private String clientLocation;
    
    /** User agent */
    private String userAgent;
    
    /** Request ID (for trace) */
    private String requestId;
    
    /** Session ID */
    private String sessionId;
    
    /** Module code (which module performed the operation) */
    private String moduleCode;
    
    /** Operation status: SUCCESS, FAILURE, PENDING_APPROVAL */
    private String status;
    
    /** Error message (if failure) */
    private String errorMessage;
    
    /** Risk level: LOW, MEDIUM, HIGH, CRITICAL */
    private String riskLevel;
    
    /** Risk indicators (JSON array) */
    private String riskIndicators;
    
    /** Operation timestamp */
    private LocalDateTime operationTime;
    
    /** Processing duration (ms) */
    private Long processingDuration;
    
    /** Audit fields */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @Version
    private Long version;
}
