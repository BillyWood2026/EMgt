package com.example.sysmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("monitor_sync_log")
public class MonitorSyncLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("sync_type")
    private String syncType;

    @TableField("source_system")
    private String sourceSystem;

    @TableField("target_system")
    private String targetSystem;

    @TableField("sync_count")
    private Integer syncCount;

    @TableField("success_count")
    private Integer successCount;

    @TableField("fail_count")
    private Integer failCount;

    @TableField("status")
    private String status;

    @TableField("error_msg")
    private String errorMsg;

    @TableField("sync_time")
    private LocalDateTime syncTime;

    @TableField("cost_time")
    private Long costTime;
}
