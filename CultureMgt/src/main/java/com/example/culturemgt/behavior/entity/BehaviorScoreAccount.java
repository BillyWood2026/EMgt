package com.example.culturemgt.behavior.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文化积分主档（一人一行）
 */
@Data
@TableName("cul_behavior_score_account")
public class BehaviorScoreAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userName;

    private String dept;

    /** 当前总分(基线60) */
    private Double totalScore;

    /** 客户导向累计 */
    private Double dimCustomer;

    /** 奋斗担当累计 */
    private Double dimStrive;

    /** 艰苦奋斗累计 */
    private Double dimHardship;

    /** 自我批判累计 */
    private Double dimReflect;

    /** 正向笔数 */
    private Integer posCount;

    /** 负向笔数 */
    private Integer negCount;

    private Integer rankMonth;

    private Integer rankQuarter;

    private Integer rankYear;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCalcTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
