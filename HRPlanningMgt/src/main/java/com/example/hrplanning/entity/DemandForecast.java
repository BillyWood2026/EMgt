package com.example.hrplanning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人力资源需求预测 hr_demand_forecast
 */
@Data
@TableName("hr_demand_forecast")
public class DemandForecast {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("forecast_id")
    private String forecastId;

    @TableField("plan_no")
    private String planNo;

    @TableField("org_code")
    private String orgCode;

    @TableField("position_name")
    private String positionName;

    @TableField("position_sequence")
    private String positionSequence;

    @TableField("current_on_post_count")
    private Integer currentOnPostCount;

    @TableField("growth_demand")
    private Integer growthDemand;

    @TableField("efficiency_reduction")
    private Integer efficiencyReduction;

    @TableField("total_demand")
    private Integer totalDemand;

    @TableField("algorithm_source")
    private String algorithmSource;

    @TableField("gap_flag")
    private String gapFlag;

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
