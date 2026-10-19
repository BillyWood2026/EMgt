package com.example.culturemgt.behavior.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 价值观评议配置
 */
@Data
@TableName("cul_behavior_review_config")
public class BehaviorReviewConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 绩效周期 2026Q3/2026H1 */
    private String periodTag;

    /** 4维权重 JSON {"客户导向":25,...} */
    @JsonProperty("weightJson")
    private String weightJson;

    /** 参评对象(部门串/全员) */
    private String targetScope;

    /** 是否允许互评 */
    private Integer allowPeer;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 草稿/已发布/已结束 */
    private String status;

    private String publisher;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
