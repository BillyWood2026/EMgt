package com.example.strategymgt.sixs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sixs_s2_business")
public class SixsS2Business {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String monitorCode;
    private String monitorPeriod;
    private BigDecimal bpGoal;
    private BigDecimal bpActual;
    private BigDecimal completionRate;
    private BigDecimal yoyRate;
    private String businessStatus;
    private String coreIndicator;
    private String marketChange;
    private String adjustmentMeasure;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
