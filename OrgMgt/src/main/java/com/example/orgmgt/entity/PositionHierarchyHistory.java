package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Position Hierarchy History Entity
 * Tracks changes to position hierarchies
 * Immutable once created, permanent record for compliance
 */
@Data
@TableName("position_hierarchy_history")
public class PositionHierarchyHistory {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("change_no")
    private String changeNo;
    
    @TableField("hierarchy_code")
    private String hierarchyCode;
    
    @TableField("hierarchy_name")
    private String hierarchyName;
    
    @TableField("org_level")
    private Integer orgLevel;
    
    @TableField("change_type")
    private String changeType;
    
    @TableField("before_content")
    private String beforeContent;
    
    @TableField("after_content")
    private String afterContent;
    
    @TableField("initiator")
    private String initiator;
    
    @TableField("approval_nodes")
    private String approvalNodes;
    
    @TableField("approval_time")
    private LocalDateTime approvalTime;
    
    @TableField("attachments")
    private String attachments;
    
    @TableField("operation_record")
    private String operationRecord;

    @TableField("created_by")
    private String createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private String updatedBy;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableField("deleted")
    private Integer deleted;

    // Change type constants
    public static final String CHANGE_TYPE_CREATE = "CREATE";
    public static final String CHANGE_TYPE_UPDATE = "UPDATE";
    public static final String CHANGE_TYPE_DELETE = "DELETE";
    
    // Approval result constants
    public static final String RESULT_PENDING = "PENDING";
    public static final String RESULT_APPROVED = "APPROVED";
    public static final String RESULT_REJECTED = "REJECTED";
}