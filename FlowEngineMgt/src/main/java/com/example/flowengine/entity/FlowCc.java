package com.example.flowengine.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("flow_cc")
public class FlowCc {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instId;
    private String ccUser;
    private Integer readFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
