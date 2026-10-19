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
 * 人力规划关键业务指标 hr_key_metrics
 */
@Data
@TableName("hr_key_metrics")
public class KeyMetrics {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("metrics_code")
    private String metricsCode;

    @TableField("metrics_name")
    private String metricsName;

    @TableField("plan_no")
    private String planNo;

    @TableField("base_year_actual")
    private BigDecimal baseYearActual;

    @TableField("target_value")
    private BigDecimal targetValue;

    @TableField("growth_rate")
    private BigDecimal growthRate;

    @TableField("weight_coefficient")
    private BigDecimal weightCoefficient;

    @TableField("metrics_type")
    private String metricsType;

    /** 指标对应岗位序列 */
    @TableField("position_sequence")
    private String positionSequence;

    @TableField("status")
    private String status;

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
