package com.example.sysmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("monitor_database")
public class MonitorDatabase {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("host")
    private String host;

    @TableField("port")
    private Integer port;

    @TableField("connections")
    private Integer connections;

    @TableField("max_connections")
    private Integer maxConnections;

    @TableField("cpu")
    private Double cpu;

    @TableField("memory")
    private Double memory;

    @TableField("status")
    private String status;

    @TableField("version")
    private String version;

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
