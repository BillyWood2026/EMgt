package com.example.partyaffairsmgt.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 消息发送记录（需求 12.4：按渠道拆分，失败自动重试上限3次）。 */
@Data
@TableName("sys_msg_record")
public class SysMsgRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String tplCode;
    private String title;
    private String content;
    private String receiver;
    private String channel;
    /** 预警/待办/通知 */
    private String msgType;
    /** 待发送/已发送/发送失败 */
    private String status;
    private Integer retryCount;
    private LocalDateTime scheduledTime;
    private LocalDateTime sendTime;
    private String failReason;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
