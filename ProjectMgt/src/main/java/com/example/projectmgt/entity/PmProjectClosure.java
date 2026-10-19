package com.example.projectmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pm_project_closure")
public class PmProjectClosure {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("closure_no")
    private String closureNo;

    @TableField("project_id")
    private Long projectId;

    @TableField("project_no")
    private String projectNo;

    @TableField("manager")
    private String manager;

    @TableField("type")
    private String type;

    @TableField("accept_date")
    private LocalDate acceptDate;

    @TableField("score")
    private Integer score;

    @TableField("conclusion")
    private String conclusion;

    @TableField("status")
    private String status;

    @TableField("opinion")
    private String opinion;

    @TableField("task_closed_flag")
    private Boolean taskClosedFlag;

    @TableField("expense_settled_flag")
    private Boolean expenseSettledFlag;

    @TableField("contract_settled_flag")
    private Boolean contractSettledFlag;

    @TableField("budget_executed_flag")
    private Boolean budgetExecutedFlag;

    @TableField("unsettled_amount")
    private BigDecimal unsettledAmount;

    @TableField("review_report")
    private String reviewReport;

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

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_REVIEWING = "reviewing";
    public static final String STATUS_PASSED = "passed";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_ARCHIVED = "archived";
}
