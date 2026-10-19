package com.example.sysmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_oper_log")
public class SysOperLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("business_type")
    private String businessType;

    @TableField("method")
    private String method;

    @TableField("request_method")
    private String requestMethod;

    @TableField("operator_type")
    private String operatorType;

    @TableField("oper_name")
    private String operName;

    @TableField("dept_name")
    private String deptName;

    @TableField("oper_url")
    private String operUrl;

    @TableField("oper_ip")
    private String operIp;

    @TableField("oper_location")
    private String operLocation;

    @TableField("oper_param")
    private String operParam;

    @TableField("json_result")
    private String jsonResult;

    @TableField("status")
    private Integer status;

    @TableField("error_msg")
    private String errorMsg;

    @TableField("oper_time")
    private LocalDateTime operTime;

    @TableField("cost_time")
    private Long costTime;
}
