package com.example.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 在线考试（learn-to-exam/e-examing/Exam.vue，前缀 /api/learn/exams）
 */
@Data
@TableName("online_exam")
public class OnlineExam {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 考试名称 */
    private String examName;

    /** 考试类型 onboarding/promotion/qualification/annual/special */
    private String type;

    /** 考试时长（分钟） */
    private Integer duration;

    /** 总分 */
    private Integer totalScore;

    /** 及格分 */
    private Integer passScore;

    /** 考试时间 YYYY-MM-DD HH:mm */
    private String examTime;

    /** 考试形式 closed_online/open_online/offline */
    private String mode;

    /** 是否限时 */
    private Boolean timeLimited;

    /** 考试说明 */
    private String description;

    /** 双随机抽题 */
    private Boolean randomPaper;

    /** 切屏上限 */
    private Integer maxSwitchCount;

    /** 秒答阈值 */
    private Integer minAnswerSeconds;

    /** AI雷同检测 */
    private Boolean aiSimilarityCheck;

    /** 规律作答识别 */
    private Boolean patternCheck;

    /** 强制全屏 */
    private Boolean forceFullScreen;

    /** 及格人数 */
    private Integer passedCount;

    /** 平均分 */
    private java.math.BigDecimal avgScore;

    /** 通过率 */
    private Integer passRate;

    /** 风控异常数 */
    private Integer riskCount;

    /** 状态 PENDING/ONGOING/FINISHED/ARCHIVED */
    private String status;

    /** 参考人数 */
    private Integer examineeCount;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @Version
    private Integer version = 1;

    @TableLogic
    private Integer deleted = 0;
}
