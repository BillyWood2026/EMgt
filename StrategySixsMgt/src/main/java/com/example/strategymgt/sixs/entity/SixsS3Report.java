package com.example.strategymgt.sixs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sixs_s3_report")
public class SixsS3Report {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String monitorCode;
    private String reportPeriod;
    private String reportType;
    private String businessPerf;
    private String kpiCompletion;
    private String taskProgress;
    private String riskEvent;
    private String resourceUsage;
    private String nextPeriodPlan;
    private String reviewer;
    private LocalDateTime reviewTime;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
