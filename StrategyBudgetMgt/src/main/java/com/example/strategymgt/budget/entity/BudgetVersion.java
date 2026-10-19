package com.example.strategymgt.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("budget_version")
public class BudgetVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String versionCode;
    private Integer budgetYear;
    private String versionType;
    private String versionNo;
    private BigDecimal totalBudget;
    private LocalDateTime submitTime;
    private String approveStatus;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
