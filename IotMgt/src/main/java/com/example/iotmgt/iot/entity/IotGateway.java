package com.example.iotmgt.iot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("iot_gateway")
public class IotGateway {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String gatewayCode;
    private String gatewayName;
    private String ipAddress;
    private Integer deviceCount;
    private String onlineStatus;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
