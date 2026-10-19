package com.example.strategymgt.sixs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sixs_s6_manager")
public class SixsS6Manager {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String evalCode;
    private String evalPeriod;
    private String managerName;
    private String position;
    private BigDecimal unitPerfScore;
    private BigDecimal dutyScore;
    private BigDecimal complianceScore;
    private BigDecimal capabilityScore;
    private BigDecimal totalScore;
    private String perfLevel;
    private String improvementSuggestion;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
