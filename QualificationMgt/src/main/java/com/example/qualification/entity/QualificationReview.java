package com.example.qualification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 等级评定/评审 (对应前端 qual-rank/Rank.vue, 表 qualification_review)
 */
@Data
@TableName("qualification_review")
public class QualificationReview {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 评审编号 */
    @TableField("review_no")
    private String reviewNo;

    /** 员工ID */
    @TableField("employee_id")
    private Long employeeId;

    /** 员工工号 */
    @TableField("employee_no")
    private String employeeNo;

    /** 员工姓名 */
    @TableField("employee_name")
    private String employeeName;

    /** 申请资格/等级名称 */
    @TableField("qualification_name")
    private String qualificationName;

    /** 目标等级 */
    @TableField("target_level")
    private String targetLevel;

    /** 总分 */
    @TableField("total_score")
    private BigDecimal totalScore;

    /** 申请理由 */
    @TableField("apply_reason")
    private String applyReason;

    /** 评审状态: IN_REVIEW / PENDING_CALIBRATION / PENDING_APPROVAL / APPROVED / REJECTED */
    @TableField("review_status")
    private String reviewStatus;

    /** 评审人 */
    @TableField("reviewer")
    private String reviewer;

    /** 评审时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("review_time")
    private LocalDateTime reviewTime;

    /** 审批流程实例ID */
    @TableField("process_instance_id")
    private String processInstanceId;

    /** 当前流程节点 */
    @TableField("current_node")
    private String currentNode;

    /** 备注/审批意见 */
    @TableField("remark")
    private String remark;

    /** 当前等级 */
    @TableField("current_level")
    private String currentLevel;

    /** 司龄(年) */
    @TableField("work_years")
    private Integer workYears;

    /** 绩效结果 */
    @TableField("perf_result")
    private String perfResult;

    /** 举证材料 */
    @TableField("evidence")
    private String evidence;

    /** 自评得分 */
    @TableField("self_score")
    private BigDecimal selfScore;

    /** 部门评分 */
    @TableField("dept_score")
    private BigDecimal deptScore;

    /** HR复审得分 */
    @TableField("hr_score")
    private BigDecimal hrScore;

    /** 专家评审得分 */
    @TableField("expert_score")
    private BigDecimal expertScore;

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
