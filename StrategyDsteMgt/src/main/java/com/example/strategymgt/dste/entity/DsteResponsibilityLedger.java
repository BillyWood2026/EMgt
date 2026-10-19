package com.example.strategymgt.dste.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dste_responsibility_ledger")
public class DsteResponsibilityLedger {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String responsibleSubject;
    private String strategyLevel;
    private String responsibleGoal;
    private String responsibleKpi;
    private String responsibleTask;
    private BigDecimal responsibilityWeight;
    private String assessmentStandard;
    private BigDecimal performanceScore;
    private String dutyStatus;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
