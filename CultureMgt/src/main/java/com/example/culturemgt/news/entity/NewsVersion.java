package com.example.culturemgt.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 新闻稿件版本快照（每次保存自动生成，永久保存） */
@Data
@TableName("cul_news_version")
public class NewsVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long newsId;

    /** 版本号 从1递增 */
    private Integer verNo;

    private String title;

    private String summary;

    private String content;

    private String editor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime editTime;
}
