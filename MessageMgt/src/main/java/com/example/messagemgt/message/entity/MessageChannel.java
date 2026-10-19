package com.example.messagemgt.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_channel")
public class MessageChannel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String channelType;
    private String channelName;
    private String configJson;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
