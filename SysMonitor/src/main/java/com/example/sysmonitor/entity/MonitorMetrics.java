package com.example.sysmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("monitor_metrics")
public class MonitorMetrics {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("metrics_type")
    private String metricsType;

    @TableField("metrics_value")
    private BigDecimal metricsValue;

    @TableField("metrics_unit")
    private String metricsUnit;

    @TableField("instance_id")
    private String instanceId;

    @TableField("collect_time")
    private LocalDateTime collectTime;
}
