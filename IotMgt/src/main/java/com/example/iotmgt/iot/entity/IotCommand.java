package com.example.iotmgt.iot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("iot_command")
public class IotCommand {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceCode;
    private String commandType;
    private String paramsJson;
    private LocalDateTime issueTime;
    private String execStatus;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
