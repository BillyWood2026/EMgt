package com.example.culturemgt.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新闻稿件（子模块9）
 */
@Data
@TableName("cul_news")
public class News {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 编号 XW+yyyyMMdd+4位 */
    private String newsNo;

    /** 标题 10-120字 */
    private String title;

    /** 摘要 30-300字 */
    private String summary;

    /** 正文富文本 */
    private String content;

    /** 封面图URL */
    private String coverImg;

    /** 文化动态/榜样事迹/高管寄语/文化专题/活动报道 */
    private String newsType;

    /** 来源所属组织（集团总部=集团稿件） */
    private String sourceOrg;

    /** 价值观标签 JSON数组 */
    private String valueTags;

    /** 关联引用案例ID */
    private Long relCaseId;

    /** 关联引用榜样ID(honor_apply) */
    private Long relModelId;

    /** 移动端展示 1/0 */
    private Integer isMobileShow;

    /** 置顶 1/0（每组织上限5条） */
    private Integer isTop;

    /** 计划发布时间（定时发布） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planPublishTime;

    /** 内部采编备注（前台不展示） */
    private String innerNote;

    /** 阅读量 */
    private Integer readCount;

    /** 移动端阅读量 */
    private Integer mobileReadCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 草稿/待审核/已发布/已下线归档 */
    private String status;

    /** 审批节点 0草稿/1初审/2复核/3终审 */
    private Integer stage;

    /** 最近提交审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    /** 下线原因 */
    private String offlineReason;

    /** 创建人（撰稿人） */
    private String creator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    /** 重复标题预警确认标志（不落库） */
    @TableField(exist = false)
    private Boolean confirmDuplicate;
}
