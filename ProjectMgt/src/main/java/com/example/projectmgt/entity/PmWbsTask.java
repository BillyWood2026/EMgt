package com.example.projectmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pm_wbs_task")
public class PmWbsTask {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("task_no")
    private String taskNo;

    @TableField("wbs_code")
    private String wbsCode;

    @TableField("project_id")
    private Long projectId;

    @TableField("project_no")
    private String projectNo;

    @TableField("phase")
    private String phase;

    @TableField("task_name")
    private String taskName;

    @TableField("parent_task_id")
    private Long parentTaskId;

    @TableField("task_level")
    private Integer taskLevel;

    @TableField("owner")
    private String owner;

    @TableField("owner_name")
    private String ownerName;

    @TableField("plan_start_date")
    private LocalDate planStartDate;

    @TableField("plan_end_date")
    private LocalDate planEndDate;

    @TableField("actual_start_date")
    private LocalDate actualStartDate;

    @TableField("actual_end_date")
    private LocalDate actualEndDate;

    @TableField("plan_duration")
    private Integer planDuration;

    @TableField("actual_duration")
    private Integer actualDuration;

    @TableField("predecessor_tasks")
    private String predecessorTasks;

    @TableField("successor_tasks")
    private String successorTasks;

    @TableField("budget_subject_code")
    private String budgetSubjectCode;

    @TableField("budget_amount")
    private BigDecimal budgetAmount;

    @TableField("available_budget")
    private BigDecimal availableBudget;

    @TableField("progress")
    private Integer progress;

    @TableField("weight")
    private BigDecimal weight;

    @TableField("deliverable_std")
    private String deliverableStd;

    @TableField("remark")
    private String remark;

    @TableField("is_critical_path")
    private Boolean isCriticalPath;

    @TableField("is_milestone")
    private Boolean isMilestone;

    @TableField("status")
    private String status;

    @TableField("archive_status")
    private String archiveStatus;

    @Version
    @TableField("version")
    private Long version;

    @TableField("snapshot_json")
    private String snapshotJson;

    @TableField("data_sensitivity_level")
    private String dataSensitivityLevel;

    @TableField("created_by")
    private String createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private String updatedBy;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
