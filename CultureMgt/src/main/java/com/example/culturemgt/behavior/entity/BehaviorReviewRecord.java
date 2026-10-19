package com.example.culturemgt.behavior.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 价值观评议记录
 */
@Data
@TableName("cul_behavior_review_record")
public class BehaviorReviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long configId;

    private String periodTag;

    /** 评价人 */
    private String evaluator;

    /** 被评人 */
    private String evaluatee;

    /** 上级/互评 */
    private String relation;

    private Double scoreCustomer;

    private Double scoreStrive;

    private Double scoreHardship;

    private Double scoreReflect;

    /** 算法71加权合计 */
    private Double totalScore;

    /** S/A/B/C/D */
    private String levelTag;

    /** 评语(>=10字) */
    private String comment;

    /** 是否已推送绩效中台 */
    private Integer pushed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pushTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
