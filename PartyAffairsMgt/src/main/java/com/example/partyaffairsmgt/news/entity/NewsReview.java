package com.example.partyaffairsmgt.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("news_review")
public class NewsReview {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private String articleTitle;
    private String reviewLevel;
    /** 审核类型：初审/复审/终审 */
    private String reviewType;
    private String reviewer;
    private LocalDateTime reviewTime;
    private String result;
    private String opinion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
