package com.example.hrplanning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人才培养工程（/api/training-projects，hr_planning_mgt.training_project）
 * 状态：DRAFT/IN_PROGRESS/COMPLETED/CANCELLED
 */
@Data
@TableName("training_project")
public class TrainingProject {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("project_code")
    private String projectCode;

    @TableField("project_name")
    private String projectName;

    /** 工程类型，如 精鹰工程/雏鹰工程 */
    @TableField("project_type")
    private String projectType;

    @TableField("start_date")
    private String startDate;

    @TableField("end_date")
    private String endDate;

    @TableField("plan_student_count")
    private Integer planStudentCount;

    @TableField("actual_student_count")
    private Integer actualStudentCount;

    @TableField("budget")
    private BigDecimal budget;

    @TableField("manager")
    private String manager;

    @TableField("status")
    private String status;

    @TableField("description")
    private String description;

    @TableField("deleted")
    @TableLogic
    private Integer deleted = 0;

    @TableField("version")
    @Version
    private Integer version = 1;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;
}
