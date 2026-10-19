package com.example.perf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 绩效评估 Entity（对齐前端 Evaluation.vue：/api/perf/assessment）
 */
@Data
@TableName("perf_assessment")
public class PerfAssessment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 评估编号 */
    @TableField("eval_no")
    private String evalNo;

    /** 员工姓名 */
    @TableField("employee_name")
    private String employeeName;

    /** 部门 */
    @TableField("department")
    private String department;

    /** 岗位 */
    @TableField("position")
    private String position;

    /** 评估周期，如 2026Q2 */
    @TableField("period")
    private String period;

    /** 关联的考核模板/方案 */
    @TableField("template_name")
    private String templateName;

    /** 考核关系（上级/平级/下级评估人配置） */
    @TableField("eval_relation")
    private String evalRelation;

    /** 自评得分 */
    @TableField("self_score")
    private BigDecimal selfScore;

    /** 上级评分 */
    @TableField("superior_score")
    private BigDecimal superiorScore;

    /** 平级评分 */
    @TableField("peer_score")
    private BigDecimal peerScore;

    /** 评估得分（综合 = 自评×0.2 + 上级×0.6 + 平级×0.2，四舍五入） */
    @TableField("score")
    private BigDecimal score;

    /** 评级 A/B/C/D */
    @TableField("grade")
    private String grade;

    /** 状态 待评估/评估中/待审核/已完成 */
    @TableField("status")
    private String status;

    /** 低分强制说明（综合得分低于60分必填） */
    @TableField("low_score_reason")
    private String lowScoreReason;

    /** 评估意见（优势/不足/改进建议） */
    @TableField("eval_comment")
    private String evalComment;

    /** 评估时间（展示字符串，如 2026-05-10 14:30） */
    @TableField("evaluate_time")
    private String evaluateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted = 0;

    @TableField("version")
    @Version
    private Integer version = 1;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;
}
