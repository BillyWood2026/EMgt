package com.example.strategymgt.blm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("blm_investment_portfolio")
public class BlmInvestmentPortfolio {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String portfolioCode;
    private Long spId;
    private String investmentType;
    private String investmentName;
    private BigDecimal investmentAmount;
    private BigDecimal expectedReturn;
    private String riskLevel;
    private String priority;
    private String ownerDept;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
