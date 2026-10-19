package com.example.projectmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pm_project")
public class PmProject {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("project_no")
    private String projectNo;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("dept_code")
    private String deptCode;

    @TableField("manager")
    private String manager;

    @TableField("manager_name")
    private String managerName;

    @TableField("priority")
    private String priority;

    @TableField("status")
    private String status;

    @TableField("plan_start_date")
    private LocalDate planStartDate;

    @TableField("plan_end_date")
    private LocalDate planEndDate;

    @TableField("partner")
    private String partner;

    @TableField("project_goal")
    private String projectGoal;

    @TableField("deliverable")
    private String deliverable;

    @TableField("approval_file_url")
    private String approvalFileUrl;

    @TableField("total_budget")
    private BigDecimal totalBudget;

    @TableField("used_budget")
    private BigDecimal usedBudget;

    @TableField("budget_usage_rate")
    private BigDecimal budgetUsageRate;

    @TableField("baseline_version")
    private String baselineVersion;

    @TableField("archive_status")
    private String archiveStatus;

    @TableField("archive_reason")
    private String archiveReason;

    @TableField("archive_time")
    private LocalDate archiveTime;

    @Version
    @TableField("version")
    private Long version;

    @TableField("snapshot_json")
    private String snapshotJson;

    @TableField("monthly_lock_flag")
    private Boolean monthlyLockFlag;

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

    @TableField("contract_amount")
    private BigDecimal contractAmount;

    public static final String STATUS_PLANNING = "planning";
    public static final String STATUS_ONGOING = "ongoing";
    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_ARCHIVED = "archived";

    public static final String ARCHIVE_UNARCHIVED = "unarchived";
    public static final String ARCHIVE_ARCHIVED = "archived";
}
