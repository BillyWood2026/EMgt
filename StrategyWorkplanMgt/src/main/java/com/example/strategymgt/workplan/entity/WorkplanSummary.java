package com.example.strategymgt.workplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("workplan_summary")
public class WorkplanSummary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private String summaryPeriod;
    private String overallDesc;
    private String highlights;
    private String problems;
    private String improvement;
    /** 整改责任人 */
    @TableField("rectify_owner")
    private String rectifyOwner;
    /** 整改时限（YYYY-MM-DD） */
    @TableField("rectify_deadline")
    private String rectifyDeadline;
    private String nextPlan;
    private String summaryStatus;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
