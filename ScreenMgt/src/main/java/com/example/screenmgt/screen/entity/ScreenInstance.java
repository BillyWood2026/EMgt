package com.example.screenmgt.screen.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 大屏实例管理 */
@Data
@TableName("screen_instance")
public class ScreenInstance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String screenName;
    private Long templateId;
    /** 组件配置JSON */
    private String componentsJson;
    /** 访问令牌 */
    private String accessToken;
    /** 状态:设计中/已发布/下线 */
    private String status;
    private LocalDateTime publishTime;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
