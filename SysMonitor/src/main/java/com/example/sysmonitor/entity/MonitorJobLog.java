package com.example.sysmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("monitor_job_log")
public class MonitorJobLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("job_id")
    private Long jobId;

    @TableField("job_name")
    private String jobName;

    @TableField("job_group")
    private String jobGroup;

    @TableField("invoke_target")
    private String invokeTarget;

    @TableField("job_params")
    private String jobParams;

    @TableField("status")
    private String status;

    @TableField("error_msg")
    private String errorMsg;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("cost_time")
    private Long costTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}
