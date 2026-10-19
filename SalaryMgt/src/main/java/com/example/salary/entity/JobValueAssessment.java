package com.example.salary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位价值评估 Entity（对应前端 Position.vue）
 */
@Data
@TableName("job_value_assessment")
public class JobValueAssessment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 岗位编码 */
    @TableField("position_code")
    private String positionCode;

    /** 岗位名称 */
    @TableField("position_name")
    private String positionName;

    /** 所属部门 */
    @TableField("department")
    private String department;

    /** 岗位序列：管理序列/技术研发序列/职能支撑序列/业务营销序列/生产操作序列 */
    @TableField("job_sequence")
    private String jobSequence;

    /** 选用评估模型：huawei/mercer/hay/aon */
    @TableField("eval_model")
    private String evalModel;

    /** 评估版本，如 V1 */
    @TableField("eval_version")
    private String evalVersion;

    /** 知识技能分 */
    @TableField("knowledge_score")
    private Integer knowledgeScore;

    /** 责任要求分 */
    @TableField("responsibility_score")
    private Integer responsibilityScore;

    /** 努力程度分 */
    @TableField("effort_score")
    private Integer effortScore;

    /** 工作环境分 */
    @TableField("environment_score")
    private Integer environmentScore;

    /** 总分 */
    @TableField("total_score")
    private Integer totalScore;

    /** 人工校准岗级 */
    @TableField("calibrate_level")
    private String calibrateLevel;

    /** 价值等级：一级 ~ 五级 */
    @TableField("value_level")
    private String valueLevel;

    /** 打分依据与佐证说明 */
    @TableField("score_basis")
    private String scoreBasis;

    /** 评估时间 */
    @TableField("assess_time")
    private String assessTime;

    /** 状态：待评估 / 评估中 / 已完成 */
    @TableField("status")
    private String status;

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
