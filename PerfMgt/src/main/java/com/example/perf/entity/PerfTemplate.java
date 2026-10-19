package com.example.perf.entity;

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
 * KPI指标模板 Entity（对齐前端 Kpi.vue：/api/perf/template）
 */
@Data
@TableName("perf_template")
public class PerfTemplate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 指标编号 */
    @TableField("kpi_no")
    private String kpiNo;

    /** 指标名称 */
    @TableField("name")
    private String name;

    /** 所属维度 finance/customer/internal/learning */
    @TableField("dimension")
    private String dimension;

    /** 保底值 */
    @TableField("floor_value")
    private String floorValue;

    /** 目标值 */
    @TableField("target_value")
    private String targetValue;

    /** 挑战值 */
    @TableField("challenge_value")
    private String challengeValue;

    /** 权重（百分比） */
    @TableField("weight")
    private BigDecimal weight;

    /** 单位 */
    @TableField("unit")
    private String unit;

    /** 考核频率 月度/季度/年度 */
    @TableField("frequency")
    private String frequency;

    /** 数据来源，如 ERP/财务系统/人工填报 */
    @TableField("data_source")
    private String dataSource;

    /** 评分梯度，如 <保底0分；保底~目标线性60-100分；≥挑战120分 */
    @TableField("scoring_gradient")
    private String scoringGradient;

    /** 指标定义 */
    @TableField("definition")
    private String definition;

    /** 计算方法 */
    @TableField("calculation")
    private String calculation;

    /** 状态 启用/停用 */
    @TableField("status")
    private String status;

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
