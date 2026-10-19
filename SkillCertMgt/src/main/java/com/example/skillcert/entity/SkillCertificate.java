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
 * 证书与年度复审（skill-certification/Certification.vue + SkillCertificate.vue）
 */
@Data
@TableName("skill_certificate")
public class SkillCertificate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String certNo;
    private String certName;
    private String certLevel;
    private String certType;
    private String level;
    private String employeeNo;
    private String employeeName;
    private String userName;
    private String orgName;
    private String skillName;
    private String skillTypeName;
    private Long skillTypeId;
    private Double score;
    private String examDate;
    private String validFrom;
    private String validTo;
    private String issueDate;
    private String validUntil;

    /** pending/passed/failed/valid（Certification.vue） */
    private String status;

    /** VALID/EXPIRING/EXPIRED/REVOKED（SkillCertificate.vue） */
    private String certStatus;

    /** 证书状态文本（展示） */
    @TableField(exist = false)
    private String certStatusText;

    private String result;
    private String issueOrg;
    private String bindPosition;
    private String recognizeType;
    private String externalCertNo;
    private String attachmentUrl;

    /** 剩余天数（展示，服务端计算） */
    @TableField(exist = false)
    private Integer expiringDays;

    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    @Version
    private Integer version = 1;

    @TableLogic
    private Integer deleted = 0;
}
