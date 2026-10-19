package com.example.talentdev.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dev Project Entity (人才培养项目)
 */
@Data
@TableName("dev_project")
public class DevProject {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("project_name")
    private String projectName;

    @TableField("project_type")
    private String projectType;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("participant_count")
    private Integer participantCount;

    @TableField("budget")
    private BigDecimal budget;

    @TableField("description")
    private String description;

    @TableField("status")
    private String status;

    /** 计划人数 */
    @TableField("plan_count")
    private Integer planCount;

    /** 项目负责人 */
    @TableField("manager")
    private String manager;

    /** 导师组长 */
    @TableField("mentor_lead")
    private String mentorLead;

    /** 721权重-学习(%) */
    @TableField("theory_weight")
    private Integer theoryWeight;

    /** 721权重-实践(%) */
    @TableField("practice_weight")
    private Integer practiceWeight;

    /** 721权重-帮带(%) */
    @TableField("mentor_weight")
    private Integer mentorWeight;

    /** 战略培养目标 */
    @TableField("goal")
    private String goal;

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
