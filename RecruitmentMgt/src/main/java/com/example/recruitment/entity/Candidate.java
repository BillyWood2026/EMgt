package com.example.recruitment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 候选人（人才池 rec02）实体，字段与前端 Candidate.vue 表格列 / 表单完全对应。
 * 表 rec02_candidate 的列：name / phone / position / source / education /
 * experience / remark / current_stage。
 */
@Data
@TableName("rec02_candidate")
public class Candidate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 应聘岗位 */
    private String position;

    /** 来源：zhilian / 51job / boss / internal / headhunter */
    private String source;

    /** 学历：本科 / 硕士 / 大专 / 高中 */
    private String education;

    /** 工作经验，如 5年以上 */
    private String experience;

    /** 备注 */
    private String remark;

    /** 当前阶段：简历筛选 / 初试 / 复试 / 待录用 / 已入职 / 已淘汰 */
    private String currentStage;

    /** 性别：男/女 */
    @TableField("gender")
    private String gender;

    /** 年龄 */
    @TableField("age")
    private Integer age;

    /** 邮箱 */
    @TableField("email")
    private String email;

    /** 专业 */
    @TableField("major")
    private String major;

    /** 技能标签（逗号分隔） */
    @TableField("skill_tags")
    private String skillTags;

    /** 期望薪资 */
    @TableField("expected_salary")
    private String expectedSalary;

    /** 期望工作地点 */
    @TableField("expected_location")
    private String expectedLocation;

    /** 内推人（工号/姓名） */
    @TableField("referrer")
    private String referrer;

    @Version
    private Integer version;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private String createBy;

    private LocalDateTime updateTime;

    private String updateBy;
}
