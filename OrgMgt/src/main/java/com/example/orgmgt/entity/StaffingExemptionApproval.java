package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PLAN-007: Staffing Exemption Approval Entity
 * Handles exemption approval workflow for staffing quota and management span violations
 * 
 * Use cases:
 * - Exceeding standard staffing quota with valid business reasons
 * - Management span exceeding maximum threshold temporarily
 * - Special project requirements requiring exception approval
 * - Organizational restructuring with temporary violations
 * 
 * Workflow:
 * 1. Submit exemption request with justification
 * 2. Multi-level approval (DEPT_HEAD -> HR_REVIEW -> FINAL_APPROVAL)
 * 3. Set exemption period and monitoring requirements
 * 4. Track compliance during exemption period
 * 5. Auto-expire after exemption period ends
 */
@Data
@TableName("staffing_exemption_approval")
public class StaffingExemptionApproval {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("exemption_no")
    private String exemptionNo;
    
    @TableField("org_id")
    private Long orgId;
    
    @TableField("org_code")
    private String orgCode;
    
    @TableField("org_name")
    private String orgName;
    
    @TableField("position_id")
    private Long positionId;
    
    @TableField("position_code")
    private String positionCode;
    
    @TableField("position_name")
    private String positionName;
    
    @TableField("exemption_type")
    private String exemptionType;
    
    @TableField("violation_type")
    private String violationType;
    
    @TableField("standard_value")
    private Integer standardValue;
    
    @TableField("current_value")
    private Integer currentValue;
    
    @TableField("exemption_value")
    private Integer exemptionValue;
    
    @TableField("exemption_reason")
    private String exemptionReason;
    
    @TableField("business_justification")
    private String businessJustification;
    
    @TableField("effective_date")
    private LocalDateTime effectiveDate;
    
    @TableField("expire_date")
    private LocalDateTime expireDate;
    
    @TableField("monitoring_required")
    private Boolean monitoringRequired;
    
    @TableField("monitoring_frequency")
    private String monitoringFrequency;
    
    @TableField("approval_status")
    private String approvalStatus;
    
    @TableField("current_node")
    private String currentNode;
    
    @TableField("initiator_id")
    private String initiatorId;
    
    @TableField("initiator_name")
    private String initiatorName;
    
    @TableField("submit_time")
    private LocalDateTime submitTime;
    
    @TableField("approval_time")
    private LocalDateTime approvalTime;
    
    @TableField("approver_id")
    private String approverId;
    
    @TableField("approver_name")
    private String approverName;
    
    @TableField("reject_reason")
    private String rejectReason;
    
    @TableField("approval_history")
    private String approvalHistory;
    
    @TableField("attachment_path")
    private String attachmentPath;
    
    @TableField("compliance_status")
    private String complianceStatus;
    
    @TableField("compliance_notes")
    private String complianceNotes;
    
    @TableField("created_by")
    private String createdBy;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("updated_by")
    private String updatedBy;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;
    
    // Exemption type constants
    public static final String EXEMPTION_TYPE_QUOTA = "QUOTA";
    public static final String EXEMPTION_TYPE_SPAN = "SPAN";
    
    // Violation type constants
    public static final String VIOLATION_TYPE_EXCEED = "EXCEED";
    public static final String VIOLATION_TYPE_BELOW = "BELOW";
    
    // Approval status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_EXPIRED = "EXPIRED";
    
    // Compliance status constants
    public static final String COMPLIANCE_COMPLIANT = "COMPLIANT";
    public static final String COMPLIANCE_VIOLATED = "VIOLATED";
    public static final String COMPLIANCE_MONITORING = "MONITORING";
    
    // Monitoring frequency constants
    public static final String MONITORING_DAILY = "DAILY";
    public static final String MONITORING_WEEKLY = "WEEKLY";
    public static final String MONITORING_MONTHLY = "MONTHLY";
}