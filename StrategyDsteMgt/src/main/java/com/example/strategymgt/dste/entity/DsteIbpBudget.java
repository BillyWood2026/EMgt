package com.example.strategymgt.dste.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dste_ibp_budget")
public class DsteIbpBudget {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String budgetCode;
    private Long relatedTaskId;
    private Integer budgetYear;
    private BigDecimal budgetAmount;
    private BigDecimal usedAmount;
    private BigDecimal remainingAmount;
    private String budgetType;
    private String bindStatus;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
