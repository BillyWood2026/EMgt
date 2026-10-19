package com.example.talent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 胜任力标准 / 评估模型实体 (evaluation_standard)
 * 对齐页面: comp-assess/Model.vue
 * 接口前缀: /api/evaluation/standard
 */
@Data
@TableName("evaluation_standard")
public class EvaluationStandard {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 模型业务编号(MDL 开头)，前端以此作为列表行 id */
    @TableField("standard_code")
    private String standardCode;

    /** 模型/标准名称 */
    @TableField("standard_name")
    private String standardName;

    /** 适用岗位(岗位族) */
    @TableField("position_family")
    private String positionFamily;

    /** 评估维度 / 胜任力序列(逗号分隔) */
    @TableField("position_sequences")
    private String positionSequences;

    /** 评估周期: 年度/半年度/季度 */
    @TableField("period_type")
    private String periodType;

    /** 模型说明 */
    @TableField("description")
    private String description;

    /** 创建人 */
    @TableField("creator")
    private String creator;

    /** ACTIVE / INACTIVE */
    @TableField("status")
    private String status;

    /** 测评类型(如 360/CATA 等) */
    @TableField("assess_type")
    private String assessType;

    /** 测评时长(分钟) */
    @TableField("duration")
    private Integer duration;

    /** 题目数量 */
    @TableField("question_count")
    private Integer questionCount;

    /** 计分算法类型 */
    @TableField("scoring_algo")
    private String scoringAlgo;

    /** 防作弊等级 */
    @TableField("anti_cheat_level")
    private String antiCheatLevel;

    /** 维度权重配置 JSON */
    @TableField("dim_weight_json")
    private String dimWeightJson;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;

    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;

    @Version
    @TableField("version")
    private Integer version = 1;
}
