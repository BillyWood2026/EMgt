package com.example.strategymgt.blm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("blm_market_insight")
public class BlmMarketInsight {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String insightCode;
    private Integer planYear;
    private String insightDim;
    private String insightTopic;
    private String envAnalysis;
    private BigDecimal marketSize;
    private BigDecimal growthRate;
    private BigDecimal penetrationRate;
    private String competitors;
    private String customerPain;
    private String capabilityGap;
    private String opportunity;
    private String riskPoint;
    private String strategyInit;
    private BigDecimal opportunityScore;
    private Long relatedPlanId;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
