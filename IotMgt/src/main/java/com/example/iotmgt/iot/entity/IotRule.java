package com.example.iotmgt.iot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("iot_rule")
public class IotRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleName;
    private String triggerCondition;
    private String action;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
