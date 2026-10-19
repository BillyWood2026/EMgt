package com.example.talentdev.entity;

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
 * Dev Tracking Entity (进度跟踪)
 */
@Data
@TableName("dev_tracking")
public class DevTracking {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    private Long projectId;

    @TableField("employee_id")
    private Long employeeId;

    @TableField("employee_name")
    private String employeeName;

    @TableField("milestone")
    private String milestone;

    @TableField("planned_date")
    private LocalDate plannedDate;

    @TableField("actual_date")
    private LocalDate actualDate;

    @TableField("progress")
    private Integer progress;

    @TableField("status")
    private String status;

    @TableField("comment")
    private String comment;

    /** 学习维度进度(%) */
    @TableField("theory_progress")
    private Integer theoryProgress;

    /** 实践维度进度(%) */
    @TableField("practice_progress")
    private Integer practiceProgress;

    /** 帮带维度进度(%) */
    @TableField("mentor_progress")
    private Integer mentorProgress;

    /** 督办跟进人 */
    @TableField("follower")
    private String follower;

    /** 闭环时间 */
    @TableField("close_time")
    private LocalDate closeTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;
}
