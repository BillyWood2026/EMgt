package com.example.partyaffairsmgt.opinion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 舆情采集信息（需求 8.1/8.2：URL指纹、互动量、事件聚类）。 */
@Data
@TableName("opinion_collection")
public class OpinionCollection {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    /** 来源渠道 */
    private String source;
    private String url;
    /** URL指纹MD5（去重） */
    private String urlMd5;
    private String author;
    private String region;
    private String keywords;
    private String content;
    private LocalDateTime collectTime;
    /** 情感预判 */
    private String sentimentHint;
    /** 待分析/已分析/已预警 */
    private String status;
    /** 信源平台：新闻网站/论坛/微博/微信/短视频平台/本地媒体 */
    private String sourcePlatform;
    /** 转发量（热度因子） */
    private Integer fwdCount;
    private Integer cmtCount;
    private Integer likeCount;
    private Integer readCount;
    /** 事件聚类ID */
    private String clusterId;
    /** 是否被去重合并 */
    private Integer merged;
    private Long taskId;
    /** 是否重大突发事件 */
    private Integer majorEvent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
