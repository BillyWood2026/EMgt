package com.example.culturemgt.behavior.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人行为举证
 */
@Data
@TableName("cul_behavior_evidence")
public class BehaviorEvidence {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务编号 BH+yyyyMMdd+4位 */
    private String evidenceNo;

    /** 举证员工 */
    private String contributor;

    /** 部门 */
    private String dept;

    /** 价值观维度：客户导向/奋斗担当/艰苦奋斗/自我批判 */
    private String valueDimension;

    /** 正向/负向 */
    private String behaviorType;

    /** 发生时间(禁未来) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    /** 发生场景 */
    private String scene;

    /** 事迹描述(>=30字) */
    private String deedDesc;

    /** 佐证附件 JSON数组(>=1) */
    private String attachments;

    /** 关联项目/客户 */
    private String relatedProject;

    /** 自评分 0-10 */
    private Double selfScore;

    /** 草稿/待初审/待复核/已入账/已驳回 */
    private String status;

    /** 1上级初审 2文化专员复核 */
    private Integer stage;

    /** 审核链路 JSON数组 */
    private String approvalLog;

    /** 最近驳回原因 */
    private String rejectReason;

    /** 复核最终积分 */
    private Double finalScore;

    /** 积分流水ID */
    private Long pointTxnId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
