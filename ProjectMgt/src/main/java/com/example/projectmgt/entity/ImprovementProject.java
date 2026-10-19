package com.example.projectmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("improvement_project")
public class ImprovementProject {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("project_no")
    private String projectNo;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("description")
    private String description;

    @TableField("manager")
    private String manager;

    @TableField("manager_name")
    private String managerName;

    @TableField("department")
    private String department;

    @TableField("budget")
    private BigDecimal budget;

    @TableField("actual_cost")
    private BigDecimal actualCost;

    @TableField("expected_benefit")
    private BigDecimal expectedBenefit;

    @TableField("actual_benefit")
    private BigDecimal actualBenefit;

    @TableField("priority")
    private String priority;

    @TableField("progress")
    private Integer progress;

    @TableField("status")
    private String status;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("actual_start_date")
    private LocalDate actualStartDate;

    @TableField("actual_end_date")
    private LocalDate actualEndDate;

    @TableField("created_by")
    private String createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private String updatedBy;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @Version
    @TableField("version")
    private Long version;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
