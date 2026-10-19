package com.example.partyaffairsmgt.opinion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 舆情信源配置（需求 8.1：新闻网站/论坛/微博/微信/短视频平台/本地媒体）。 */
@Data
@TableName("opinion_source")
public class OpinionSource {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** 类型：新闻网站/论坛/微博/微信/短视频平台/本地媒体 */
    private String type;
    private String url;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
