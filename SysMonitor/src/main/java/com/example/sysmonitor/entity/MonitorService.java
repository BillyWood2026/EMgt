package com.example.sysmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("monitor_service")
public class MonitorService {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("service_name")
    private String serviceName;

    @TableField("service_type")
    private String serviceType;

    @TableField("service_ip")
    private String serviceIp;

    @TableField("service_port")
    private Integer servicePort;

    @TableField("health_url")
    private String healthUrl;

    @TableField("status")
    private String status;

    @TableField("last_check_time")
    private LocalDateTime lastCheckTime;

    @TableField("fail_count")
    private Integer failCount;

    @TableField("description")
    private String description;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
