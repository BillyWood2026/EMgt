package com.example.flowengine.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("flow_instance")
public class FlowInstance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String instNo;
    private Long defId;
    private String defCode;
    private String bizCode;
    private String bizId;
    private String bizJson;
    private String attachmentIds;
    private String title;
    private String initiator;
    private String currentNode;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
