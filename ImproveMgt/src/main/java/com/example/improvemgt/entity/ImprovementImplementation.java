package com.example.improvemgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("improve_implementation")
public class ImprovementImplementation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("impl_no")
    private String implNo;

    @TableField("proposal_id")
    private Long proposalId;

    @TableField("name")
    private String name;

    @TableField("owner")
    private String owner;

    @TableField("department")
    private String department;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("priority")
    private String priority;

    @TableField("progress")
    private Integer progress;

    @TableField("status")
    private String status;

    @TableField("description")
    private String description;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ONGOING = "ongoing";
    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_COMPLETED = "completed";
}
