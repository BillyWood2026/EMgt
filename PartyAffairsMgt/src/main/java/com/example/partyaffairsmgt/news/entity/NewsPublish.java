package com.example.partyaffairsmgt.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("news_publish")
public class NewsPublish {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private String articleTitle;
    private String channel;
    /** 渠道类型：官网/公众号/移动端 */
    private String channelType;
    private String publisher;
    private LocalDateTime publishTime;
    private String url;
    private String status;
    private Integer retryCount;
    private String lastError;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
