package com.example.iotmgt.iot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("iot_alert")
public class IotAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceCode;
    private String alertLevel;
    private String content;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
