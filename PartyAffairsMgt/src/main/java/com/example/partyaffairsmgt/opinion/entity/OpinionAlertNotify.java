package com.example.partyaffairsmgt.opinion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 预警通知设置（需求 8.3：短信/APP/站内 + 适用级别）。 */
@Data
@TableName("opinion_alert_notify")
public class OpinionAlertNotify {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 短信/APP/站内 */
    private String notifyType;
    /** 适用级别：红/红,橙/全部 */
    private String levelScope;
    private String receivers;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
