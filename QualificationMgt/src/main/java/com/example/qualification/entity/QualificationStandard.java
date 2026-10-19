package com.example.qualification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 等级标准 (对应前端 qual-rank/Qualification.vue, 表 qualification_standard)
 */
@Data
@TableName("qualification_standard")
public class QualificationStandard {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 标准编号 */
    @TableField("standard_no")
    private String standardNo;

    /** 标准名称 */
    @TableField("name")
    private String name;

    /** 等级: 初级/中级/高级/专家 */
    @TableField("level")
    private String level;

    /** 类别: 技术类/管理类等 */
    @TableField("category")
    private String category;

    /** 资格要求 */
    @TableField("requirements")
    private String requirements;

    /** 描述 */
    @TableField("description")
    private String description;

    /** 档位 */
    @TableField("grade")
    private String grade;

    /** 岗位序列 */
    @TableField("sequence")
    private String sequence;

    /** 子序列 */
    @TableField("sub_sequence")
    private String subSequence;

    /** 生效日期 */
    @TableField("effective_date")
    private String effectiveDate;

    /** 失效日期 */
    @TableField("expiry_date")
    private String expiryDate;

    /** 基础条件权重 */
    @TableField("base_weight")
    private Integer baseWeight;

    /** 知识技能权重 */
    @TableField("knowledge_weight")
    private Integer knowledgeWeight;

    /** 行为履职权重 */
    @TableField("behavior_weight")
    private Integer behaviorWeight;

    /** 绩效贡献权重 */
    @TableField("performance_weight")
    private Integer performanceWeight;

    /** 综合素质权重 */
    @TableField("quality_weight")
    private Integer qualityWeight;

    /** 达标分数线 */
    @TableField("pass_score")
    private Integer passScore;

    /** 普通项容错% */
    @TableField("normal_tolerance")
    private Integer normalTolerance;

    /** 关键项严卡 */
    @TableField("key_item_strict")
    private Boolean keyItemStrict;

    /** 年度名额 */
    @TableField("quota_total")
    private Integer quotaTotal;

    /** 已用名额 */
    @TableField("quota_used")
    private Integer quotaUsed;

    /** 有效期(年) */
    @TableField("valid_years")
    private Integer validYears;

    /** 准入条件 */
    @TableField("admission_condition")
    private String admissionCondition;

    /** 举证要求 */
    @TableField("evidence_requirement")
    private String evidenceRequirement;

    /** 状态: 启用/停用 */
    @TableField("status")
    private String status;

    @TableField("create_by")
    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_by")
    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @Version
    @TableField("version")
    private Integer version = 1;

    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;
}
