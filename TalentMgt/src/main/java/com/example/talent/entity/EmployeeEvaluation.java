package com.example.talent.entity;

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
 * 员工测评 / 员工评估实体 (employee_evaluation)
 * 对齐页面: talent-devep/Assess.vue、comp-assess/Evaluation.vue
 * 接口前缀: /api/evaluation/employee
 */
@Data
@TableName("employee_evaluation")
public class EmployeeEvaluation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 业务编号(AS/EV 开头)，前端以此作为列表行 id 与编辑标识 */
    @TableField("record_id")
    private String recordId;

    /** 员工姓名 */
    @TableField("employee_name")
    private String employeeName;

    /** 部门(组织名称) */
    @TableField("org_name")
    private String orgName;

    /** 岗位名称 */
    @TableField("position_name")
    private String positionName;

    /** 测评/评估周期，如 2026H1、2025Y */
    @TableField("batch_code")
    private String batchCode;

    /** 绩效/评估得分 */
    @TableField("performance_score")
    private BigDecimal performanceScore;

    /** 潜力总分 */
    @TableField("total_potential_score")
    private BigDecimal totalPotentialScore;

    /** 综合评级 A/B/C/D */
    @TableField("performance_tier")
    private String performanceTier;

    /** 九宫格分类 / 评估维度(逗号分隔) */
    @TableField("nine_box_category")
    private String nineBoxCategory;

    /** 测评结论 / 评估意见 */
    @TableField("manual_calibration_reason")
    private String manualCalibrationReason;

    /** 测评人 / 评估人 / 创建人 */
    @TableField("creator")
    private String creator;

    /** PENDING / IN_PROGRESS / COMPLETED */
    @TableField("status")
    private String status;

    /** 人工修正得分(复核) */
    @TableField("revised_score")
    private BigDecimal revisedScore;

    /** 风控风险等级(低/中/高风险) */
    @TableField("risk_level")
    private String riskLevel;

    /** 异常作答详情 */
    @TableField("abnormal_detail")
    private String abnormalDetail;

    /** 人才判定标签(逗号分隔) */
    @TableField("talent_label")
    private String talentLabel;

    /** 适配岗位建议 */
    @TableField("fit_position")
    private String fitPosition;

    /** 复核人 */
    @TableField("reviewer")
    private String reviewer;

    /** 复核时间(前端本地化字符串) */
    @TableField("review_time")
    private String reviewTime;

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
