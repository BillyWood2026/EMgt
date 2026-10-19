package com.example.strategymgt.blm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("blm_sp_plan")
public class BlmSpPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String spCode;
    private String spCycle;
    private String baselineVersion;
    private String businessTrack;
    private String investmentDirection;
    private String capabilityGoals;
    private BigDecimal ltRevenue;
    private BigDecimal ltProfit;
    private BigDecimal ltMarketShare;
    private String ltTechBreakthrough;
    private String ltIndustryRank;
    private String keyTasksOutline;
    private String annualBreakdown;
    private BigDecimal totalResourceBudget;
    private String relatedReports;
    private String compileTeam;
    private String reviewTeam;
    private String reviewOpinion;
    private String status;
    private LocalDate effectiveTime;
    private LocalDateTime baselineLockTime;
    private Long prevVersionId;
    private String newVersionNo;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
