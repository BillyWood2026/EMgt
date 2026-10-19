package com.example.strategymgt.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("project_budget")
public class ProjectBudget {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String projectCode;
    private String projectName;
    private String projectType;
    private BigDecimal estimateAmount;
    private BigDecimal budgetAmount;
    private BigDecimal accountingAmount;
    private BigDecimal finalAmount;
    private BigDecimal varianceAmount;
    private BigDecimal varianceRate;
    private String projectStatus;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
