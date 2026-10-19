package com.example.culturemgt.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 新闻审批流转记录 */
@Data
@TableName("cul_news_approval")
public class NewsApproval {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long newsId;

    /** 审批节点名称 */
    private String nodeName;

    private String approver;

    /** 通过/驳回 */
    private String action;

    /** 审批意见（驳回必填20-500字） */
    private String opinion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;
}
