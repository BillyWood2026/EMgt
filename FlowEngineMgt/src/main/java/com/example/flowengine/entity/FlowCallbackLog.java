package com.example.flowengine.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("flow_callback_log")
public class FlowCallbackLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instId;
    private String bizCode;
    private String bizId;
    private String event;
    private String payload;
    private LocalDateTime callbackTime;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
