package com.example.strategymgt.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("annual_budget")
public class AnnualBudget {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String budgetCode;
    private Integer budgetYear;
    private String subjectCode;
    private String budgetDim;
    private String budgetItem;
    private BigDecimal budgetAmount;
    private BigDecimal usedAmount;
    private BigDecimal occupiedAmount;
    private BigDecimal remainingAmount;
    private Long versionId;
    private String approvalStatus;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
