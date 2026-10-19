package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Job Position Change History Entity
 * Tracks all job position changes with full approval trail
 * Immutable once created, permanent record for state-owned enterprise compliance
 */
@Data
@TableName("job_position_change_history")
public class JobPositionChangeHistory {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("change_no")
    private String changeNo;
    
    @TableField("position_id")
    private Long positionId;
    
    @TableField("position_code")
    private String positionCode;
    
    @TableField("position_name")
    private String positionName;
    
    @TableField("change_type")
    private String changeType;
    
    @TableField("before_data")
    private String beforeData;
    
    @TableField("after_data")
    private String afterData;
    
    @TableField("change_desc")
    private String changeDesc;
    
    @TableField("initiator_id")
    private String initiatorId;
    
    @TableField("initiator_name")
    private String initiatorName;
    
    @TableField("change_time")
    private LocalDateTime changeTime;
    
    @TableField("approval_nodes")
    private String approvalNodes;
    
    @TableField("approval_result")
    private String approvalResult;
    
    @TableField("approval_time")
    private LocalDateTime approvalTime;
    
    @TableField("operator")
    private String operator;
    
    @TableField("ip_address")
    private String ipAddress;
    
    @TableField("version_no")
    private String versionNo;
    
    @TableField("attachment_path")
    private String attachmentPath;

    @TableField("attachment_url")
    private String attachmentUrl;

    @TableField("remarks")
    private String remarks;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    // Change type constants
    public static final String CHANGE_TYPE_CREATE = "CREATE";
    public static final String CHANGE_TYPE_APPOINT = "APPOINT";
    public static final String CHANGE_TYPE_ADJUST = "ADJUST";
    public static final String CHANGE_TYPE_ABOLISH = "ABOLISH";
    public static final String CHANGE_TYPE_PERSONALIZED = "PERSONALIZED";
    
    // Approval result constants
    public static final String RESULT_PENDING = "PENDING";
    public static final String RESULT_APPROVED = "APPROVED";
    public static final String RESULT_REJECTED = "REJECTED";
}