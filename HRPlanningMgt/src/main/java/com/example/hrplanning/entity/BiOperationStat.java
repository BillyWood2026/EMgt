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
 * BI 经营视图统计 hr_bi_operation_stat
 */
@Data
@TableName("hr_bi_operation_stat")
public class BiOperationStat {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("org_name")
    private String orgName;

    @TableField("statistics_period")
    private String statisticsPeriod;

    @TableField("planning_headcount")
    private Integer planningHeadcount;

    @TableField("actual_headcount")
    private Integer actualHeadcount;

    @TableField("headcount_deviation_value")
    private Integer headcountDeviationValue;

    @TableField("headcount_deviation_rate")
    private BigDecimal headcountDeviationRate;

    @TableField("headcount_compliance_rate")
    private BigDecimal headcountComplianceRate;

    @TableField("supply_demand_gap")
    private Integer supplyDemandGap;

    @TableField("planning_hr_cost")
    private BigDecimal planningHrCost;

    @TableField("actual_hr_cost")
    private BigDecimal actualHrCost;

    @TableField("cost_deviation_rate")
    private BigDecimal costDeviationRate;

    @TableField("per_capita_revenue")
    private BigDecimal perCapitaRevenue;

    @TableField("per_capita_cost")
    private BigDecimal perCapitaCost;

    @TableField("recruitment_achievement_rate")
    private BigDecimal recruitmentAchievementRate;

    @TableField("key_talent_retention_rate")
    private BigDecimal keyTalentRetentionRate;

    @TableField("hr_health_score")
    private BigDecimal hrHealthScore;

    @TableField("health_warning_level")
    private String healthWarningLevel;

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
    private Integer deleted = 0;

    @Version
    @TableField("version")
    private Integer version = 1;
}
