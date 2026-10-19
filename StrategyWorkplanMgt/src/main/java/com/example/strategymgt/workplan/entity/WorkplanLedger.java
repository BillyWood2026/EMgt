package com.example.strategymgt.workplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("workplan_ledger")
public class WorkplanLedger {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private String statPeriod;
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer overdueTasks;
    private BigDecimal completionRate;
    private BigDecimal avgScore;
    /** 归档状态：未归档/已归档 */
    @TableField("archive_status")
    private String archiveStatus;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
