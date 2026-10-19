package com.example.flowengine.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("flow_timeout_log")
public class FlowTimeoutLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long instId;
    private String assignee;
    private Integer timeoutHours;
    private LocalDateTime remindTime;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
