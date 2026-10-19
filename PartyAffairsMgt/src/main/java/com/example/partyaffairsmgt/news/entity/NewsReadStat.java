package com.example.partyaffairsmgt.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("news_read_stat")
public class NewsReadStat {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private String articleTitle;
    private LocalDate statDate;
    private Integer pv;
    private Integer uv;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    /** 来源渠道：官网/公众号/移动端 */
    private String sourceChannel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
