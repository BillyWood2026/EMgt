package com.example.flowengine.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("flow_task")
public class FlowTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instId;
    private String nodeKey;
    private String nodeName;
    private String assignee;
    private String status;
    private String comment;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
