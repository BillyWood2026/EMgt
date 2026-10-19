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
 * Command Chain Entity
 * Standardized command/report chain for work instruction, approval, responsibility tracking
 * Supports: Fixed chain (based on org structure), Special chain (custom), Authorized chain (temporary)
 */
@Data
@TableName("command_chain")
public class CommandChain {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("chain_code")
    private String chainCode;
    
    @TableField("chain_name")
    private String chainName;
    
    @TableField("chain_type")
    private String chainType;
    
    @TableField("applicable_org_scope")
    private String applicableOrgScope;
    
    @TableField("applicable_business")
    private String applicableBusiness;
    
    @TableField("start_position")
    private String startPosition;
    
    @TableField("end_position")
    private String endPosition;
    
    @TableField("level_count")
    private Integer levelCount;
    
    @TableField("effective_start_time")
    private LocalDateTime effectiveStartTime;
    
    @TableField("effective_end_time")
    private LocalDateTime effectiveEndTime;
    
    @TableField("chain_status")
    private String chainStatus;
    
    @TableField("allow_skip_level")
    private Boolean allowSkipLevel;
    
    @TableField("allow_cross_copy")
    private Boolean allowCrossCopy;
    
    @TableField("timeout_warning_hours")
    private Integer timeoutWarningHours;
    
    @TableField("reject_return_rule")
    private String rejectReturnRule;
    
    @TableField("force_chain_flow")
    private Boolean forceChainFlow;
    
    @TableField("related_virtual_org_id")
    private Long relatedVirtualOrgId;
    
    @TableField("authorization_basis")
    private String authorizationBasis;
    
    @TableField("chain_description")
    private String chainDescription;
    
    @TableField("approval_no")
    private String approvalNo;
    
    @TableField("remark")
    private String remark;
    
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
    
    // Chain type constants
    public static final String TYPE_FIXED = "FIXED";
    public static final String TYPE_SPECIAL = "SPECIAL";
    public static final String TYPE_AUTHORIZED = "AUTHORIZED";
    
    // Chain status constants
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_PAUSED = "PAUSED";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_ABANDONED = "ABANDONED";
    
    // Reject return rule constants
    public static final String REJECT_RETURN_START = "RETURN_TO_START";
    public static final String REJECT_RETURN_PREVIOUS = "RETURN_TO_PREVIOUS";
}