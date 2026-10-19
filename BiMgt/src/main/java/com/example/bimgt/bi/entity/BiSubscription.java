package com.example.bimgt.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** BI定时订阅 */
@Data
@TableName("bi_subscription")
public class BiSubscription {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reportId;
    private String subscriber;
    /** 周期:日/周/月 */
    private String period;
    /** 推送渠道:邮件/企业微信/站内信 */
    private String pushChannel;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
