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
 * Command Chain Node Entity
 * Configurable nodes in command chain for approval, reporting, and command
 * Supports position/hierarchy/person selection dimension
 */
@Data
@TableName("command_chain_node")
public class CommandChainNode {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    // Command chain ID
    @TableField("chain_id")
    private Long chainId;
    
    // Node sequence/order
    @TableField("node_order")
    private Integer nodeOrder;
    
    // Node type: REPORT/APPROVE/COMMAND
    @TableField("node_type")
    private String nodeType;
    
    // Selection dimension: POSITION/HIERARCHY/PERSON
    @TableField("select_by")
    private String selectBy;
    
    // Target position/hierarchy/person name
    @TableField("target_name")
    private String targetName;
    
    // Target ID (position ID, hierarchy ID, or employee ID)
    @TableField("target_id")
    private Long targetId;
    
    // Node title
    @TableField("node_title")
    private String nodeTitle;
    
    // Responsible person name
    @TableField("responsible_person")
    private String responsiblePerson;
    
    // Permissions (comma-separated): View,Approve,Assign,Copy,Reject
    @TableField("permissions")
    private String permissions;
    
    // Timeout hours
    @TableField("timeout_hours")
    private Integer timeoutHours;
    
    // Timeout action: REMIND/ESCALATE/SKIP
    @TableField("timeout_action")
    private String timeoutAction;
    
    // Node status: ACTIVE/PAUSED
    @TableField("node_status")
    private String nodeStatus;
    
    // Remark
    @TableField("remark")
    private String remark;
    
    // Creator
    @TableField("created_by")
    private String createdBy;
    
    // Create time
    @TableField("create_time")
    private LocalDateTime createTime;
    
    // Updater
    @TableField("updated_by")
    private String updatedBy;
    
    // Update time
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;
    
    // Node type constants
    public static final String TYPE_REPORT = "REPORT";
    public static final String TYPE_APPROVE = "APPROVE";
    public static final String TYPE_COMMAND = "COMMAND";
    
    // Select by constants
    public static final String SELECT_BY_POSITION = "POSITION";
    public static final String SELECT_BY_HIERARCHY = "HIERARCHY";
    public static final String SELECT_BY_PERSON = "PERSON";
    
    // Node status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_PAUSED = "PAUSED";
    
    // Timeout action constants
    public static final String TIMEOUT_REMIND = "REMIND";
    public static final String TIMEOUT_ESCALATE = "ESCALATE";
    public static final String TIMEOUT_SKIP = "SKIP";
}