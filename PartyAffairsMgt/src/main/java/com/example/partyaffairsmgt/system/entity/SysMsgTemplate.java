package com.example.partyaffairsmgt.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 消息模板（需求 12.4）。 */
@Data
@TableName("sys_msg_template")
public class SysMsgTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String tplCode;
    private String tplName;
    /** 预警/待办/通知 */
    private String msgType;
    /** 站内信/短信/邮件/移动推送 逗号分隔 */
    private String channels;
    private String content;
    /** 夜间22:00-07:00非紧急静默延迟 */
    private Integer nightSilent;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
