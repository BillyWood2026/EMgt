package com.example.skillcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 技能申报与评审（skill-certification/SkillApply.vue + SkillReview.vue）
 */
@Data
@TableName("skill_application")
public class SkillApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;
    private String planName;
    private Long skillTypeId;
    private String skillTypeName;
    private String applyLevel;
    private String currentLevel;
    private Integer workYears;
    private Integer trainHours;
    private String performanceLevel;
    private String externalCertType;
    private String externalCertNo;
    private String projectResult;
    private String applyRemark;

    private Long employeeId;
    private String userName;
    private String staffNo;
    private String orgName;

    /** PENDING/APPROVED/REJECTED/EXAMINED */
    private String applyStatus;

    /** 申报状态文本（展示） */
    @TableField(exist = false)
    private String applyStatusText;

    private Double theoryScore;
    private Double practiceScore;
    private Double qualityScore;
    private Double finalScore;

    /** PASS/FAIL/SUPPLEMENT */
    private String result;

    /** PENDING/SCORED/COMPLETED/RETAKE */
    private String reviewStatus;

    /** 评审状态文本（展示） */
    @TableField(exist = false)
    private String reviewStatusText;

    private String reviewerName;
    private String reviewTime;
    private String conclusion;
    private String deductRemark;
    private String reviewerSign;
    private Integer hasExternalCert;

    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    @Version
    private Integer version = 1;

    @TableLogic
    private Integer deleted = 0;
}
