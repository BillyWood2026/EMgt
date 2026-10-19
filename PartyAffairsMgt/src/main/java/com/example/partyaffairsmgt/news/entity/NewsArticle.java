package com.example.partyaffairsmgt.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("news_article")
public class NewsArticle {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String subtitle;
    private Long columnId;
    private String author;
    private String source;
    private String summary;
    private String content;
    private String coverImage;
    private String tags;
    /** 敏感词状态：未检测/命中/已处理 */
    private String sensitiveStatus;
    /** 命中的敏感词 */
    private String sensitiveWords;
    private LocalDateTime submitTime;
    private LocalDateTime scheduledTime;
    private Integer isTop;
    private LocalDateTime publishTime;
    private LocalDateTime offlineTime;
    private String withdrawReason;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private String attachment;
    private String creatorId;
    /** 当前审核级别 */
    private String currentReviewLevel;
    /** 状态：草稿/待审/已发布/已退回/已下线 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
