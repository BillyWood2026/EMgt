package com.example.messagemgt.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_record")
public class MessageRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateCode;
    private String channel;
    private String receiver;
    private String title;
    private String content;
    private String bizParam;
    private String sendStatus;
    private LocalDateTime sendTime;
    private String failReason;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
