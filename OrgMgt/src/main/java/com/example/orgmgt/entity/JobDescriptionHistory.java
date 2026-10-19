package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Job Description Change History Entity
 * Tracks all changes to job descriptions for audit and traceability
 */
@Data
@TableName("job_description_history")
public class JobDescriptionHistory {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("job_description_id")
    private Long jobDescriptionId;
    
    @TableField("version_no")
    private String versionNo;
    
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
    
    @TableField("initiator")
    private String initiator;
    
    @TableField("approval_nodes")
    private String approvalNodes;
    
    @TableField("approval_time")
    private LocalDateTime approvalTime;
    
    @TableField("operation_record")
    private String operationRecord;
    
    @TableField("attachment_path")
    private String attachmentPath;
    
    @TableField("change_time")
    private LocalDateTime changeTime;
    
    // Change type constants
    public static final String CHANGE_TYPE_CREATE = "CREATE";
    public static final String CHANGE_TYPE_UPDATE = "UPDATE";
    public static final String CHANGE_TYPE_ARCHIVE = "ARCHIVE";
}