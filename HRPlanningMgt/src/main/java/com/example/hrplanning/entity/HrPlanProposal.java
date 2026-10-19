package com.example.hrplanning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人力规划提案 hr_plan_proposal
 */
@Data
@TableName("hr_plan_proposal")
public class HrPlanProposal {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("proposal_no")
    private String proposalNo;

    @TableField("plan_no")
    private String planNo;

    @TableField("org_code")
    private String orgCode;

    @TableField("period_start")
    private LocalDate periodStart;

    @TableField("period_end")
    private LocalDate periodEnd;

    @TableField("total_demand")
    private Integer totalDemand;

    @TableField("internal_supply")
    private Integer internalSupply;

    @TableField("external_recruitment_gap")
    private Integer externalRecruitmentGap;

    @TableField("redundancy_optimization")
    private Integer redundancyOptimization;

    @TableField("supply_demand_gap")
    private String supplyDemandGap;

    @TableField("total_hr_cost_budget")
    private BigDecimal totalHrCostBudget;

    @TableField("status")
    private String status;

    /** 规划情景：基准/增长/收缩 */
    @TableField("planning_scenario")
    private String planningScenario;

    /** 内部调剂人数 */
    @TableField("internal_transfer_count")
    private Integer internalTransferCount;

    /** 落地举措分解（JSON/文本） */
    @TableField("action_plan")
    private String actionPlan;

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
