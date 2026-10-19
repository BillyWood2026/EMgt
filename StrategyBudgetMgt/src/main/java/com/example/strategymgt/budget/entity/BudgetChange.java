package com.example.strategymgt.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("budget_change")
public class BudgetChange {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String changeCode;
    private String changeType;
    private Long relatedBudgetId;
    private BigDecimal beforeAmount;
    private BigDecimal changeAmount;
    private BigDecimal afterAmount;
    private String reason;
    private String approvalStatus;
    private LocalDate effectiveTime;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
