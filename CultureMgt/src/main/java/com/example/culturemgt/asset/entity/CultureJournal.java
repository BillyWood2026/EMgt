package com.example.culturemgt.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 电子内刊
 */
@Data
@TableName("cul_journal")
public class CultureJournal {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 期号 */
    private String issueNo;

    /** 刊名 */
    private String journalName;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    /** 正文富文本 */
    private String content;

    /** 附件PDF URL */
    private String fileUrl;

    /** 前台公开 1/0 */
    private Integer isPublic;

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
