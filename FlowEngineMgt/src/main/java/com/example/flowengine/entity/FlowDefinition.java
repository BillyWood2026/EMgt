package com.example.flowengine.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("flow_definition")
public class FlowDefinition {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String defCode;
    private String defName;
    private String category;
    private Integer version;
    private String bpmnXml;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
