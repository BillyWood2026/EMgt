package com.example.messagemgt.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_template")
public class MessageTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateCode;
    private String templateName;
    private String channel;
    private String titleTemplate;
    private String contentTemplate;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
