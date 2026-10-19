package com.example.strategymgt.dste.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dste_task_progress")
public class DsteTaskProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String reportPeriod;
    private BigDecimal plannedProgress;
    private BigDecimal actualProgress;
    private BigDecimal progressDeviation;
    private String progressStatus;
    private Integer milestoneCompleted;
    private Integer milestoneTotal;
    private String completionNote;
    private String riskNote;
    private String rectifyMeasure;
    private String reporter;
    private LocalDateTime reportTime;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
