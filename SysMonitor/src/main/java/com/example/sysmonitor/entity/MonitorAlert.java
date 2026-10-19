package com.example.sysmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("monitor_alert")
public class MonitorAlert {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("level")
    private String level;

    @TableField("title")
    private String title;

    @TableField("source")
    private String source;

    @TableField("source_name")
    private String sourceName;

    @TableField("message")
    private String message;

    @TableField("status")
    private String status;

    @TableField("details")
    private String details;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;
}
