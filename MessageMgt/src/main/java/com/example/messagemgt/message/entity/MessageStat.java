package com.example.messagemgt.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("message_stat")
public class MessageStat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private String channel;
    private Integer sendCount;
    private Integer successCount;
    private Integer failCount;
    private BigDecimal successRate;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
