package com.example.strategymgt.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("budget_variance")
public class BudgetVariance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String varianceCode;
    private Long budgetId;
    private String period;
    private BigDecimal budgetAmount;
    private BigDecimal actualAmount;
    private BigDecimal varianceAmount;
    private BigDecimal varianceRate;
    private String varianceType;
    private String reason;
    private String rectifyMeasure;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
