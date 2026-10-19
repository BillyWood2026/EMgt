package com.example.culturemgt.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文化公告栏
 */
@Data
@TableName("cul_bulletin")
public class CultureBulletin {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公告标题 */
    private String title;

    /** 正文 */
    private String content;

    /** 生效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectTime;

    /** 置顶 1/0 */
    private Integer topFlag;

    /** 草稿/已发布/已下线 */
    private String status;

    /** 浏览次数 */
    private Integer viewCount;

    private String creator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
