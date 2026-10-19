package com.example.strategymgt.dste.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("dste_key_task")
public class DsteKeyTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskCode;
    private String taskLevel;
    private Long relatedSpId;
    private BigDecimal strategyWeight;
    private String allocationType;
    private String primaryOrgLevel;
    private String assignee;
    private String collaborateOrgs;
    private String collaboratePersons;
    private String bpAcceptanceStandard;
    private String budgetBindCode;
    private String strategyTag;
    private String allocationStatus;
    private BigDecimal alignmentScore;
    private String taskName;
    private String taskType;
    private String priority;
    private String taskGoal;
    private LocalDate startTime;
    private LocalDate planEndTime;
    private LocalDate actualEndTime;
    private String primaryDept;
    private String dependencies;
    private String milestones;
    private BigDecimal taskWeight;
    private String resourceNeed;
    private BigDecimal progress;
    private Integer overdueDays;
    private String taskStatus;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
