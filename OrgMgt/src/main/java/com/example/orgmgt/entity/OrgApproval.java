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
 * Org Approval Entity
 * Unified approval workflow for organization changes
 * Supports multi-level approval, rejection, callback, and signing
 */
@Data
@TableName("org_approval")
public class OrgApproval {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("approval_no")
    private String approvalNo;
    
    @TableField("business_id")
    private Long businessId;
    
    @TableField("business_type")
    private String businessType;
    
    @TableField("approval_type")
    private String approvalType;
    
    @TableField("current_node")
    private String currentNode;
    
    @TableField("current_approver")
    private String currentApprover;
    
    @TableField("approval_status")
    private String approvalStatus;
    
    @TableField("reject_reason")
    private String rejectReason;
    
    @TableField("cc_users")
    private String ccUsers;
    
    @TableField("sign_users")
    private String signUsers;
    
    @TableField("submit_time")
    private LocalDateTime submitTime;
    
    @TableField("approval_time")
    private LocalDateTime approvalTime;
    
    @TableField("operation_log")
    private String operationLog;
    
    @TableField("ip_address")
    private String ipAddress;
    
    @TableField("attachments")
    private String attachments;
    
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
    
    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CANCELLED = "CANCELLED";
}