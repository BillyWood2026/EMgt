package com.example.strategymgt.dste.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dste_kpi_monitor")
public class DsteKpiMonitor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long kpiId;
    private String monitorPeriod;
    private BigDecimal targetValue;
    private BigDecimal actualValue;
    private BigDecimal achievementRate;
    private BigDecimal yoyRate;
    private BigDecimal momRate;
    private BigDecimal deviationRate;
    private String kpiStatus;
    private String anomalyReason;
    private String rectifyPlan;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
