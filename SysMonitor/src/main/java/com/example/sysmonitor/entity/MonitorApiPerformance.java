package com.example.sysmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("monitor_api_performance")
public class MonitorApiPerformance {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("api")
    private String api;

    @TableField("method")
    private String method;

    @TableField("path")
    private String path;

    @TableField("avg_time")
    private Long avgTime;

    @TableField("max_time")
    private Long maxTime;

    @TableField("qps")
    private Integer qps;

    @TableField("error_rate")
    private Double errorRate;

    @TableField("monitor_date")
    private LocalDate monitorDate;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;
}
