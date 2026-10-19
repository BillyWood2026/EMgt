package com.example.strategymgt.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("budget_occupation")
public class BudgetOccupation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String occupationCode;
    private Long relatedBudgetId;
    private String occupationType;
    private BigDecimal amount;
    private String businessNo;
    private LocalDateTime occupationTime;
    private LocalDateTime releaseTime;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
