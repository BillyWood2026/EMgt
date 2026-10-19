package com.example.flowengine.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("flow_node")
public class FlowNode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long defId;
    private String nodeKey;
    private String nodeName;
    private String nodeType;
    private String approverType;
    private String approverValue;
    private Integer timeoutHours;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
