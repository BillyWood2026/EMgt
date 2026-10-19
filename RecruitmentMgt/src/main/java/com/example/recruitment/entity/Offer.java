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
 * 录用通知（Offer）实体，字段与前端 Offer.vue 表格列 / 表单完全对应。
 */
@Data
@TableName("offer")
public class Offer {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 录用通知编号，如 OF-2026-001 */
    private String offerNo;

    /** 候选人姓名 */
    private String candidateName;

    /** 应聘岗位 */
    private String position;

    /** 部门 */
    private String department;

    /** 薪资，如 15K（前端为文本输入） */
    private String salary;

    /** 入职日期 */
    private LocalDate startDate;

    /** 状态：待发送 / 已发送 / 已接受 / 已拒绝 / 已失效 */
    private String status;

    /** 备注 */
    private String remark;

    /** 职级，如 P6 / M2 */
    @TableField("job_grade")
    private String jobGrade;

    /** 试用期 */
    @TableField("probation_period")
    private String probationPeriod;

    /** Offer有效期 */
    @TableField("expiry_date")
    private LocalDate expiryDate;

    /** 工作地点 */
    @TableField("work_location")
    private String workLocation;

    /** 福利说明 */
    @TableField("benefits")
    private String benefits;

    /** 薪资结构 */
    @TableField("salary_structure")
    private String salaryStructure;

    /** 面试总分 */
    @TableField("interview_score")
    private Integer interviewScore;

    /** 审批意见 */
    @TableField("approval_comment")
    private String approvalComment;

    @Version
    private Integer version;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private String createBy;

    private LocalDateTime updateTime;

    private String updateBy;
}
