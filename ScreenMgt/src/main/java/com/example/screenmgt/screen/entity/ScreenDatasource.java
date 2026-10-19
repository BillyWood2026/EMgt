package com.example.screenmgt.screen.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 大屏数据对接 */
@Data
@TableName("screen_datasource")
public class ScreenDatasource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instanceId;
    private String datasetCode;
    /** 刷新间隔秒 */
    private Integer refreshInterval;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
