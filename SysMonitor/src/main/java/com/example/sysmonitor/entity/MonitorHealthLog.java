package com.example.sysmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("monitor_health_log")
public class MonitorHealthLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("service_name")
    private String serviceName;

    @TableField("service_ip")
    private String serviceIp;

    @TableField("service_port")
    private Integer servicePort;

    @TableField("status")
    private String status;

    @TableField("response_time")
    private Integer responseTime;

    @TableField("error_msg")
    private String errorMsg;

    @TableField("check_time")
    private LocalDateTime checkTime;
}
