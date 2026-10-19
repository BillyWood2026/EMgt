package com.example.sysmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
public class SysLoginLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("login_name")
    private String loginName;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("login_location")
    private String loginLocation;

    @TableField("browser")
    private String browser;

    @TableField("os")
    private String os;

    @TableField("status")
    private String status;

    @TableField("msg")
    private String msg;

    @TableField("login_time")
    private LocalDateTime loginTime;
}
