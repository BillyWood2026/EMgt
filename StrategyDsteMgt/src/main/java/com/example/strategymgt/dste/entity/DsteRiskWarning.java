package com.example.strategymgt.dste.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dste_risk_warning")
public class DsteRiskWarning {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String riskCode;
    private String riskSource;
    private String riskType;
    private String riskDesc;
    private String riskLevel;
    private String impactScope;
    private LocalDateTime triggerTime;
    private String pushTargets;
    private String handler;
    private String handleMeasure;
    private BigDecimal handleProgress;
    private String closeStatus;
    private String reviewConclusion;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
