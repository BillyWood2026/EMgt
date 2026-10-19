package com.example.sysmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("monitor_online_user")
public class MonitorOnlineUser {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("token_id")
    private String tokenId;

    @TableField("username")
    private String username;

    @TableField("user_id")
    private String userId;

    @TableField("dept_name")
    private String deptName;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("browser")
    private String browser;

    @TableField("os")
    private String os;

    @TableField("login_time")
    private LocalDateTime loginTime;

    @TableField("last_active_time")
    private LocalDateTime lastActiveTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("status")
    private String status;
}
