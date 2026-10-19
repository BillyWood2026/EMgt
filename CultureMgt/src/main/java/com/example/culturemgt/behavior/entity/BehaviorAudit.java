package com.example.culturemgt.behavior.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 部门行为稽核（负向行为）
 */
@Data
@TableName("cul_behavior_audit")
public class BehaviorAudit {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务编号 JG+yyyyMMdd+4位 */
    private String auditNo;

    /** 关联员工 */
    private String targetUser;

    private String targetDept;

    /** 违规价值观维度 */
    private String violateDimension;

    /** 违规场景 */
    private String violateScene;

    /** 违规详情(>=10字) */
    private String violateDetail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    /** 对应 cul_redline.code */
    private String redlineCode;

    /** 扣分 0-10 */
    private Double deductScore;

    /** 整改要求 */
    private String rectifyRequirement;

    /** 整改期限 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate rectifyDeadline;

    /** 佐证附件 JSON数组(>=1) */
    private String attachments;

    /** 已登记/已告知/已确认/申诉中/整改实施中/待验收/已生效/申诉驳回已归档 */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;

    /** 申诉 JSON数组 */
    private String appealLog;

    /** 整改实施说明 */
    private String rectifyNote;

    /** 整改佐证 JSON数组 */
    private String rectifyEvidence;

    /** 验收意见 */
    private String acceptOpinion;

    /** 登记人 */
    private String registrar;

    /** 全链路 JSON数组 */
    private String approvalLog;

    /** 积分流水ID */
    private Long pointTxnId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
