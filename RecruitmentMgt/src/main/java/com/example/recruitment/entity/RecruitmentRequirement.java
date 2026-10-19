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
 * 招聘需求实体，字段与前端 Requirement.vue 表格列 / 表单完全对应。
 */
@Data
@TableName("recruitment_requirement")
public class RecruitmentRequirement {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 需求编号，如 ZR-2026-001 */
    private String requirementNo;

    /** 岗位名称 */
    private String position;

    /** 所属部门 */
    private String department;

    /** 需求人数 */
    private Integer requirementNum;

    /** 状态：待审批 / 招聘中 / 已完成 / 已取消 */
    private String status;

    /** 薪资范围，如 15K-25K */
    private String salaryRange;

    /** 紧急程度：紧急 / 一般 / 常规 */
    private String urgency;

    /** 期望到岗日期 */
    private LocalDate expectedDate;

    /** 岗位职责 */
    private String responsibilities;

    /** 任职要求 */
    private String requirements;

    /** 招聘类型：社招/校招/内推/补录 */
    @TableField("recruit_type")
    private String recruitType;

    /** 现有编制 */
    @TableField("existing_headcount")
    private Integer existingHeadcount;

    /** 剩余编制 */
    @TableField("remaining_headcount")
    private Integer remainingHeadcount;

    /** 招聘截止时间 */
    @TableField("deadline")
    private LocalDate deadline;

    /** 学历要求 */
    @TableField("education_req")
    private String educationReq;

    /** 工作年限要求 */
    @TableField("work_years")
    private String workYears;

    /** 专业要求 */
    @TableField("major_req")
    private String majorReq;

    /** 技能标签（逗号分隔） */
    @TableField("skill_tags")
    private String skillTags;

    /** 工作地点 */
    @TableField("work_location")
    private String workLocation;

    /** 试用期时长 */
    @TableField("probation_period")
    private String probationPeriod;

    /** 申请说明 */
    @TableField("apply_reason")
    private String applyReason;

    @Version
    private Integer version;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private String createBy;

    private LocalDateTime updateTime;

    private String updateBy;
}
