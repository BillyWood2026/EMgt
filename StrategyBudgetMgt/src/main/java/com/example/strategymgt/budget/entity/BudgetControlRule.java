package com.example.strategymgt.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("budget_control_rule")
public class BudgetControlRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String budgetDim;
    private String controlStrategy;
    private BigDecimal warnThreshold;
    private String controlScope;
    private LocalDate effectiveTime;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
