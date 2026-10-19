package com.example.culturemgt.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 新闻标签字典 */
@Data
@TableName("cul_news_tag")
public class NewsTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tagName;

    /** 集团全局/子公司私有 */
    private String tagScope;

    /** 启用 1/0（停用仅新建稿件不可选） */
    private Integer enabled;

    /** 引用稿件数量（冗余展示，列表实时计算） */
    private Integer refCount;

    private String creator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
