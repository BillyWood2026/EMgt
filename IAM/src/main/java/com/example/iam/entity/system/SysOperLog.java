package com.example.iam.entity.system;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_oper_log")
public class SysOperLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String title;

    private String businessType;

    private String method;

    private String requestMethod;

    private String operatorType;

    private String operName;

    private String deptName;

    private String operUrl;

    private String operIp;

    private String operLocation;

    private String operParam;

    private String jsonResult;

    private Integer status;

    private String errorMsg;

    private LocalDateTime operTime;

    private Long costTime;

    @TableLogic
    private Integer deleted;
}
