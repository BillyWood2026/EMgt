package com.example.partyaffairsmgt.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("news_recommendation")
public class NewsRecommendation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private String articleTitle;
    private Long columnId;
    /** 推荐类型：置顶/推荐 */
    private String recType;
    private Integer rankScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String operator;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
