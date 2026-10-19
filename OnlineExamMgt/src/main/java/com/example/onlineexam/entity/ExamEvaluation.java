package com.example.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试成绩评估（learn-to-exam/e-examing/Result.vue，前缀 /api/learnexam/evaluation）
 */
@Data
@TableName("exam_evaluation")
public class ExamEvaluation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 员工姓名 */
    private String employeeName;

    /** 部门 */
    private String department;

    /** 考试名称（前端 courseName） */
    private String courseName;

    /** 考试时间 YYYY-MM-DD HH:mm */
    private String examTime;

    /** 总分 */
    private Integer totalScore;

    /** 及格分 */
    private Integer passScore;

    /** 实际成绩 */
    private BigDecimal overallScore;

    /** 是否已发证 0/1 */
    private Integer hasCertificate;

    /** 证书状态 issued/pending/none */
    private String certificateStatus;

    /** 通过率（百分比） */
    private Integer passRate;

    /** 评估时间 */
    private String evaluationTime;

    /** 改进建议 */
    private String improvementSuggestions;

    /** 备注 */
    private String remark;

    /** 切屏次数 */
    private Integer switchCount;

    /** 设备ID */
    private String deviceId;

    /** IP地址 */
    private String ip;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @Version
    private Integer version = 1;

    @TableLogic
    private Integer deleted = 0;
}
