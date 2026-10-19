package com.example.sysmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("monitor_api_stats")
public class MonitorApiStats {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("api_path")
    private String apiPath;

    @TableField("api_method")
    private String apiMethod;

    @TableField("service_name")
    private String serviceName;

    @TableField("call_count")
    private Long callCount;

    @TableField("success_count")
    private Long successCount;

    @TableField("fail_count")
    private Long failCount;

    @TableField("avg_response_time")
    private Long avgResponseTime;

    @TableField("max_response_time")
    private Long maxResponseTime;

    @TableField("min_response_time")
    private Long minResponseTime;

    @TableField("stats_date")
    private LocalDate statsDate;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
