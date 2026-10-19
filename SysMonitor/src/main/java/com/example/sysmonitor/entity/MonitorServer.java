package com.example.sysmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("monitor_server")
public class MonitorServer {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("ip")
    private String ip;

    @TableField("type")
    private String type;

    @TableField("cpu")
    private Double cpu;

    @TableField("memory")
    private Double memory;

    @TableField("disk")
    private Double disk;

    @TableField("status")
    private String status;

    @TableField("os")
    private String os;

    @TableField("cpu_cores")
    private Integer cpuCores;

    @TableField("memory_size")
    private String memorySize;

    @TableField("last_update")
    private LocalDateTime lastUpdate;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;
}
