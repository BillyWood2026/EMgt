package com.example.iotmgt.iot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("iot_thing_model")
public class IotThingModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String modelCode;
    private String modelName;
    private String propertiesJson;
    private String eventsJson;
    private String servicesJson;
    private String version;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
