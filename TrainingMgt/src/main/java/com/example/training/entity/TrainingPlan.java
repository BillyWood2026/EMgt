package com.example.training.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 培训计划（training-mgt/Plan.vue）
 */
@Data
@TableName("training_plan")
public class TrainingPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 计划编号 */
    private String planCode;

    /** 计划名称 */
    private String planName;

    /** 培训类型 ONBOARDING/COMPLIANCE/SKILL/MANAGEMENT/EXECUTIVE */
    private String trainingType;

    /** 开始日期 */
    private LocalDate cycleStartDate;

    /** 结束日期 */
    private LocalDate cycleEndDate;

    /** 预算 */
    private BigDecimal totalBudget;

    /** 参与人数 */
    private Integer plannedParticipants;

    /** 考核方式 */
    private String assessmentMethod;

    /** 完成标准 */
    private String completionCriteria;

    /** 适用组织 */
    private String applicableOrg;

    /** 岗位序列 */
    private String jobSequence;

    /** 计划课时 */
    private Integer plannedHours;

    /** 已用预算 */
    private BigDecimal usedBudget;

    /** 审批进度 */
    private String approvalProgress;

    /** 绩效弱项指标 */
    private String perfWeakness;

    /** 胜任力短板 */
    private String competencyGap;

    /** 员工自评需求 */
    private String selfEvalNeeds;

    /** 需求优先级 */
    private String needPriority;

    /** 是否必修 */
    private Boolean isMandatory;

    /** 讲师费 */
    private BigDecimal budgetLecturer;

    /** 场地费 */
    private BigDecimal budgetVenue;

    /** 物料费 */
    private BigDecimal budgetMaterial;

    /** 差旅住宿费 */
    private BigDecimal budgetTravel;

    /** 预算预警阈值(%) */
    private Integer budgetWarnThreshold;

    /** 岗位准入必修 */
    private Boolean jobAccessRequired;

    /** 未完成锁定权限 */
    private Boolean lockOnIncomplete;

    /** 逾期预警周期(天) */
    private Integer overdueWarnDays;

    /** 状态 DRAFT/APPROVAL/PENDING_CLASS/PROGRESS/COMPLETED/ARCHIVED/已提交 */
    private String status;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @Version
    private Integer version = 1;

    @TableLogic
    private Integer deleted = 0;
}
