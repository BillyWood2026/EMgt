package com.example.iam.entity.audit;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Risk Event Record
 * Real-time risk monitoring events
 */
@Data
@TableName("iam_risk_event")
public class IamRiskEvent {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** Risk event ID (unique) */
    private String eventId;
    
    /** Risk type: BRUTE_FORCE, PRIVILEGE_ESCALATION, DATA_BREACH, ANOMALY_ACCESS, POLICY_VIOLATION */
    private String riskType;
    
    /** Risk category */
    private String riskCategory;
    
    /** Risk level: LOW, MEDIUM, HIGH, CRITICAL */
    private String riskLevel;
    
    /** User ID */
    private Long userId;
    
    /** Employee ID */
    private String employeeId;
    
    /** Username */
    private String username;
    
    /** Organization ID */
    private Long orgId;
    
    /** Risk source: LOGIN, PERMISSION, DATA_ACCESS, API, BATCH_JOB */
    private String riskSource;
    
    /** Risk description */
    private String description;
    
    /** Risk details (JSON) */
    private String details;
    
    /** IP address */
    private String ipAddress;
    
    /** Location */
    private String location;
    
    /** Device fingerprint */
    private String deviceFingerprint;
    
    /** Session ID */
    private String sessionId;
    
    /** Request ID */
    private String requestId;
    
    /** Module code */
    private String moduleCode;
    
    /** Resource accessed */
    private String resourceAccessed;
    
    /** Action performed */
    private String actionPerformed;
    
    /** Previous risk score */
    private Integer previousRiskScore;
    
    /** Current risk score */
    private Integer currentRiskScore;
    
    /** Recommended action: ALERT, BLOCK, INVESTIGATE, PASS */
    private String recommendedAction;
    
    /** Actual action taken */
    private String actualAction;
    
    /** Status: NEW, INVESTIGATING, RESOLVED, FALSE_POSITIVE, ESCALATED */
    private String status;
    
    /** Assigned to (investigator) */
    private String assignedTo;
    
    /** Resolution notes */
    private String resolutionNotes;
    
    /** Resolved by */
    private String resolvedBy;
    
    /** Resolved time */
    private LocalDateTime resolvedTime;
    
    /** Event timestamp */
    private LocalDateTime eventTime;
    
    /** Related audit log ID */
    private Long relatedAuditLogId;
    
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
