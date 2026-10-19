package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Organization Change History Entity
 * Permanent record of all organization changes, immutable once created
 */
@Data
@TableName("org_change_history")
public class OrgChangeHistory {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("change_no")
    private String changeNo;

    @TableField("change_title")
    private String changeTitle;

    @TableField("initiator_name")
    private String initiatorName;

    @TableField("effective_date")
    private LocalDate effectiveDate;

    @TableField("remark")
    private String remark;
    
    @TableField("org_id")
    private Long orgId;
    
    @TableField("org_code")
    private String orgCode;
    
    @TableField("org_name")
    private String orgName;
    
    @TableField("change_type")
    private String changeType;
    
    @TableField("before_content")
    private String beforeContent;
    
    @TableField("after_content")
    private String afterContent;
    
    @TableField("change_reason")
    private String changeReason;
    
    @TableField("attachments")
    private String attachments;
    
    @TableField("approval_status")
    private String approvalStatus;
    
    @TableField("approval_process")
    private String approvalProcess;
    
    @TableField("version_snapshot")
    private String versionSnapshot;
    
    @TableField("approval_time")
    private LocalDateTime approvalTime;
    
    @TableField("created_by")
    private String createdBy;
    
    @TableField("created_time")
    private LocalDateTime createdTime;
    
    @TableField("updated_by")
    private String updatedBy;
    
    @TableField("updated_time")
    private LocalDateTime updatedTime;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;
    
    // Change type constants
    public static final String CHANGE_TYPE_CREATE = "CREATE";
    public static final String CHANGE_TYPE_UPDATE = "UPDATE";
    public static final String CHANGE_TYPE_MERGE = "MERGE";
    public static final String CHANGE_TYPE_SPLIT = "SPLIT";
    public static final String CHANGE_TYPE_TRANSFER = "TRANSFER";
    public static final String CHANGE_TYPE_DISABLE = "DISABLE";
    
    // Approval status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
}