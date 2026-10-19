package com.example.processmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程实例 Entity
 */
@Data
@TableName("process_instance")
public class ProcessInstance {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("process_def_id")
    private Long processDefId;

    @TableField("process_code")
    private String processCode;

    @TableField("process_name")
    private String processName;

    @TableField("applicant")
    private String applicant;

    @TableField("status")
    private String status;

    @TableField("current_node")
    private String currentNode;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("finish_time")
    private LocalDateTime finishTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // 状态常量
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PROCESSING = "processing";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_CANCELLED = "cancelled";
}
