package com.example.recruitment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 面试安排实体，字段与前端 Interview.vue 表格列 / 表单完全对应。
 */
@Data
@TableName("interview")
public class Interview {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 面试编号，如 IV-2026-000001 */
    private String interviewNo;

    /** 候选人姓名 */
    private String candidateName;

    /** 应聘岗位 */
    private String position;

    /** 面试官 */
    private String interviewer;

    /** 面试日期 yyyy-MM-dd */
    private LocalDate interviewDate;

    /** 面试时间（HH:mm，前端时间选择器可能传入完整时间串，统一字符串存储） */
    private String interviewTime;

    /** 面试阶段：初试 / 复试 */
    private String stage;

    /** 面试结果：通过 / 未通过 / 空（待面试） */
    private String result;

    /** 面试地点 */
    private String location;

    /** 面试评价 */
    private String comment;

    /** 面试类型：线下/线上 */
    @TableField("interview_type")
    private String interviewType;

    /** 价值观得分（0-100） */
    @TableField("values_score")
    private Integer valuesScore;

    /** 专业能力得分（0-100） */
    @TableField("profession_score")
    private Integer professionScore;

    /** 综合素质得分（0-100） */
    @TableField("quality_score")
    private Integer qualityScore;

    /** 发展潜力得分（0-100） */
    @TableField("potential_score")
    private Integer potentialScore;

    /** 综合评分（四维加权） */
    @TableField("composite_score")
    private Integer compositeScore;

    /** STAR场景记录 */
    @TableField("star_record")
    private String starRecord;

    @Version
    private Integer version;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private String createBy;

    private LocalDateTime updateTime;

    private String updateBy;
}
