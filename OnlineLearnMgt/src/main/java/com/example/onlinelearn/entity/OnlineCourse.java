package com.example.onlinelearn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 在线课程（learn-to-exam/e-learning/Course.vue，前缀 /api/learn/courses）
 */
@Data
@TableName("online_course")
public class OnlineCourse {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程名称 */
    private String courseName;

    /** 课程分类 professional/management/compliance/safety/general */
    private String category;

    /** 学时 */
    private Integer duration;

    /** 学分 */
    private BigDecimal credit;

    /** 难度 beginner/intermediate/advanced */
    private String level;

    /** 必修/选修 mandatory/elective */
    private String required;

    /** 讲师 */
    private String teacher;

    /** 上线日期 */
    private LocalDate onlineDate;

    /** 课程简介 */
    private String description;

    /** 学习形式 */
    private String learnMode;

    /** 结业方式 */
    private String finishMode;

    /** 考试状态 */
    private String examStatus;

    /** 禁止拖拽进度 */
    private Boolean noDrag;

    /** 强制100%观看 */
    private Boolean forceFullWatch;

    /** 允许倍速 */
    private Boolean allowSpeed;

    /** 倍速最大限制 */
    private BigDecimal maxSpeed;

    /** 单次最短学习时长(分钟) */
    private Integer minSessionMinutes;

    /** 课件必阅 */
    private Boolean mustReadCourseware;

    /** 作业必做 */
    private Boolean mustHomework;

    /** 考试必考 */
    private Boolean mustExam;

    /** 满意度必评 */
    private Boolean mustEvaluate;

    /** 考试及格线 */
    private Integer examPassScore;

    /** 最大补考次数 */
    private Integer maxRetake;

    /** 观看完成系数 */
    private BigDecimal watchCoef;

    /** 作业系数 */
    private BigDecimal homeworkCoef;

    /** 考试得分系数 */
    private BigDecimal examCoef;

    /** 逾期扣分 */
    private BigDecimal overdueDeduct;

    /** 状态 DRAFT/PUBLISHED/OFFLINE */
    private String status;

    /** 学习人数 */
    private Integer enrollCount;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @Version
    private Integer version = 1;

    @TableLogic
    private Integer deleted = 0;
}
