package com.example.training.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 离线培训课程（training-mgt/Course.vue）
 */
@Data
@TableName("training_course")
public class TrainingCourse {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程编号 */
    private String courseNo;

    /** 课程名称 */
    private String name;

    /** 课程类型 mandatory/elective/certification */
    private String type;

    /** 课时 */
    private Integer duration;

    /** 难度等级 beginner/intermediate/advanced */
    private String level;

    /** 课程简介 */
    private String description;

    /** 课程大纲 */
    private String outline;

    /** 适用序列 */
    private String applicableSequence;

    /** 标准学分 */
    private java.math.BigDecimal standardCredit;

    /** 考核方式 */
    private String assessmentMethod;

    /** 及格线 */
    private Integer passScore;

    /** 状态 待发布/已发布/已下架 */
    private String status;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @Version
    private Integer version = 1;

    @TableLogic
    private Integer deleted = 0;
}
