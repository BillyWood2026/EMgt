package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Organization Change Approval Entity
 * Tracks organization change approval requests and workflow status
 */
@Data
@TableName("org_change_approval")
public class OrgChangeApproval {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("change_no")
    private String changeNo;
    
    @TableField("org_id")
    private Long orgId;
    
    @TableField("org_code")
    private String orgCode;
    
    @TableField("org_name")
    private String orgName;
    
    @TableField("change_type")
    private String changeType;
    
    @TableField("change_content")
    private String changeContent;
    
    @TableField("before_data")
    private String beforeData;
    
    @TableField("after_data")
    private String afterData;
    
    @TableField("attachment_path")
    private String attachmentPath;
    
    @TableField("initiator_id")
    private String initiatorId;
    
    @TableField("initiator_name")
    private String initiatorName;
    
    @TableField("submit_time")
    private LocalDateTime submitTime;
    
    @TableField("approval_status")
    private String approvalStatus;
    
    @TableField("current_node")
    private String currentNode;
    
    @TableField("next_node")
    private String nextNode;
    
    @TableField("approval_history")
    private String approvalHistory;
    
    @TableField("approval_time")
    private LocalDateTime approvalTime;
    
    @TableField("reject_reason")
    private String rejectReason;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    // Change type constants
    public static final String CHANGE_TYPE_CREATE = "CREATE";
    public static final String CHANGE_TYPE_UPDATE = "UPDATE";
    public static final String CHANGE_TYPE_DELETE = "DELETE";
    public static final String CHANGE_TYPE_MIGRATE = "MIGRATE";
    
    // Approval status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CANCELLED = "CANCELLED";
}