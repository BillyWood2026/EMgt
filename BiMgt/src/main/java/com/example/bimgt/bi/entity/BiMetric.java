package com.example.bimgt.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** BI指标管理 */
@Data
@TableName("bi_metric")
public class BiMetric {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String metricCode;
    private String metricName;
    /** 指标口径 */
    private String caliber;
    private String unit;
    /** 聚合方式:SUM/AVG/COUNT/MAX/MIN */
    private String aggType;
    private String owner;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
