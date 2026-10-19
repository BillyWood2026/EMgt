package com.example.processmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程审批 Entity
 */
@Data
@TableName("process_approval")
public class ProcessApproval {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("instance_id")
    private Long instanceId;

    @TableField("process_def_id")
    private Long processDefId;

    @TableField("biz_type")
    private String bizType;

    @TableField("process_name")
    private String processName;

    @TableField("applicant")
    private String applicant;

    @TableField("approver")
    private String approver;

    @TableField("node_name")
    private String nodeName;

    @TableField("status")
    private String status;

    @TableField("comment")
    private String comment;

    @TableField("approve_time")
    private LocalDateTime approveTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // 状态常量
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";
}
