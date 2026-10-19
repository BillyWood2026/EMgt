package com.example.hrplanning.entity;

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
 * HR Plan Entity (人力规划)
 */
@Data
@TableName("hr_plan")
public class HRPlan {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("plan_name")
    private String planName;

    @TableField("plan_year")
    private Integer planYear;

    @TableField("department")
    private String department;

    @TableField("current_headcount")
    private Integer currentHeadcount;

    @TableField("planned_headcount")
    private Integer plannedHeadcount;

    @TableField("recruitment_target")
    private Integer recruitmentTarget;

    @TableField("budget")
    private BigDecimal budget;

    @TableField("strategy")
    private String strategy;

    @TableField("objectives")
    private String objectives;

    @TableField("timeline")
    private String timeline;

    @TableField("status")
    private String status;

    /** 规划情景：基准/增长/收缩 */
    @TableField("planning_scenario")
    private String planningScenario;

    /** 营收目标（万元） */
    @TableField("revenue_target")
    private BigDecimal revenueTarget;

    /** 人均效能目标（万元） */
    @TableField("per_capita_efficiency_target")
    private BigDecimal perCapitaEfficiencyTarget;

    /** 滚动更新周期 */
    @TableField("update_cycle")
    private String updateCycle;

    /** 偏差预警阈值（%） */
    @TableField("deviation_threshold")
    private BigDecimal deviationThreshold;

    @TableField("approved_by")
    private String approvedBy;

    @TableField("approved_date")
    private LocalDate approvedDate;

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
