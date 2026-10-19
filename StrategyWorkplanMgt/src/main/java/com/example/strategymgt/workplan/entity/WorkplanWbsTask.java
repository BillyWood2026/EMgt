package com.example.strategymgt.workplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("workplan_wbs_task")
public class WorkplanWbsTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private String taskCode;
    private String taskName;
    private Integer taskLevel;
    private Long parentTaskId;
    private BigDecimal taskWeight;
    private String taskGoal;
    private LocalDate startTime;
    private LocalDate endTime;
    private String milestone;
    private String dependencyIds;
    private String assignee;
    private BigDecimal progress;
    private String taskStatus;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
