package com.example.iotmgt.iot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("iot_telemetry")
public class IotTelemetry {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceCode;
    private String metricName;
    private BigDecimal metricValue;
    private String unit;
    private LocalDateTime reportTime;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
