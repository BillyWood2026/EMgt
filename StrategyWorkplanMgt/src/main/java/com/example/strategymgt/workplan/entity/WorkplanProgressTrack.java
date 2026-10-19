package com.example.strategymgt.workplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("workplan_progress_track")
public class WorkplanProgressTrack {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Long taskId;
    private String trackPeriod;
    private BigDecimal plannedProgress;
    private BigDecimal actualProgress;
    private BigDecimal deviation;
    private String completionDesc;
    private String riskDesc;
    private String rectifyMeasure;
    /** 当前卡点（影响进度的关键卡点） */
    @TableField("blocker")
    private String blocker;
    /** 资源支撑（需要的资源/协调支撑） */
    @TableField("resource_support")
    private String resourceSupport;
    private String reporter;
    private LocalDateTime reportTime;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
