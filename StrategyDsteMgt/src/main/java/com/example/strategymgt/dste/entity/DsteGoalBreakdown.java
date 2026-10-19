package com.example.strategymgt.dste.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("dste_goal_breakdown")
public class DsteGoalBreakdown {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String breakdownCode;
    private Long relatedSpId;
    private String goalLevel;
    private String goalDim;
    private String topGoal;
    private BigDecimal currentGoal;
    private String goalFormula;
    private BigDecimal annualValue;
    private BigDecimal quarterlyValue;
    private BigDecimal monthlyValue;
    private BigDecimal goalWeight;
    private String responsibleOrg;
    private String responsiblePerson;
    private String collaborators;
    private String achievementStandard;
    private String assessmentCycle;
    private Long parentGoalId;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
