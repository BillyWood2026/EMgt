package com.example.sysmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("monitor_job")
public class MonitorJob {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("job_name")
    private String jobName;

    @TableField("job_group")
    private String jobGroup;

    @TableField("job_class")
    private String jobClass;

    @TableField("cron_expression")
    private String cronExpression;

    @TableField("invoke_target")
    private String invokeTarget;

    @TableField("params")
    private String params;

    @TableField("status")
    private String status;

    @TableField("misfire_policy")
    private String misfirePolicy;

    @TableField("concurrent")
    private String concurrent;

    @TableField("remark")
    private String remark;

    @TableField("create_by")
    private String createBy;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_by")
    private String updateBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
