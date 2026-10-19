package com.example.screenmgt.screen.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 大屏模板管理 */
@Data
@TableName("screen_template")
public class ScreenTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateName;
    private String thumbnail;
    /** 布局JSON */
    private String layoutJson;
    /** 分辨率:1920x1080等 */
    private String resolution;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
