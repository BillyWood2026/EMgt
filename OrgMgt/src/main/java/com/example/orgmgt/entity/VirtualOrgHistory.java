package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Virtual Organization History Entity
 * Tracks full lifecycle of virtual organizations
 * Permanent record for audit and compliance
 */
@Data
@TableName("virtual_org_history")
public class VirtualOrgHistory {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("change_no")
    private String changeNo;
    
    @TableField("virtual_org_id")
    private Long virtualOrgId;
    
    @TableField("virtual_org_code")
    private String virtualOrgCode;
    
    @TableField("virtual_org_name")
    private String virtualOrgName;
    
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
    
    @TableField("attachment_path")
    private String attachmentPath;
    
    @TableField("remarks")
    private String remarks;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    // Change type constants
    public static final String CHANGE_TYPE_CREATE = "CREATE";
    public static final String CHANGE_TYPE_MEMBER_CHANGE = "MEMBER_CHANGE";
    public static final String CHANGE_TYPE_LEADER_CHANGE = "LEADER_CHANGE";
    public static final String CHANGE_TYPE_EXTEND = "EXTEND";
    public static final String CHANGE_TYPE_DISBAND = "DISBAND";
    
    // Approval result constants
    public static final String RESULT_PENDING = "PENDING";
    public static final String RESULT_APPROVED = "APPROVED";
    public static final String RESULT_REJECTED = "REJECTED";
}