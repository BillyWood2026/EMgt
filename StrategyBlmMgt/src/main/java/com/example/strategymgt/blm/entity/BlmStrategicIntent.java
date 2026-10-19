package com.example.strategymgt.blm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("blm_strategic_intent")
public class BlmStrategicIntent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String intentCode;
    private String strategyCycle;
    private String mission;
    private String vision;
    private String coreValues;
    private BigDecimal ltRevenue;
    private BigDecimal ltProfit;
    private BigDecimal ltMarketShare;
    private String ltTechBreakthrough;
    private String ltScale;
    private String annualPosition;
    private String strategicDemands;
    private String constraints;
    private String versionNo;
    private String approvalStatus;
    private LocalDate effectiveTime;
    private LocalDate archiveTime;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
