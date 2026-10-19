package com.example.strategymgt.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("rolling_forecast")
public class RollingForecast {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String forecastCode;
    private String forecastPeriod;
    private Long budgetId;
    private BigDecimal forecastAmount;
    private BigDecimal actualAmount;
    private BigDecimal varianceAmount;
    private String forecastBasis;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
