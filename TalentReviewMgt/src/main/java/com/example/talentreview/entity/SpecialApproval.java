package com.example.talentreview.entity;

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
 * 特殊审批 / 人才评审实体 (special_approval)
 * 对齐页面: talent-devep/Review.vue
 * 接口前缀: /api/evaluation/special-approval
 */
@Data
@TableName("special_approval")
public class SpecialApproval {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 审批业务编号(RV 开头)，前端以此作为列表行 id */
    @TableField("approval_id")
    private String approvalId;

    /** 申请人 / 评审对象姓名 */
    @TableField("employee_name")
    private String employeeName;

    /** 部门 / 组织 */
    @TableField("org_code")
    private String orgCode;

    /** 审批/评审类型: promotion/transfer/talent/succession */
    @TableField("approval_type")
    private String approvalType;

    /** 审批事由 / 评审主题 */
    @TableField("reason")
    private String reason;

    /** HR 建议 / 评审专家 */
    @TableField("hr_recommendation")
    private String hrRecommendation;

    /** 生效日期 / 评审日期 */
    @TableField("effective_date")
    private LocalDate effectiveDate;

    /** PENDING / IN_PROGRESS / APPROVED / REJECTED */
    @TableField("approval_status")
    private String approvalStatus;

    /** 审批人 */
    @TableField("approver")
    private String approver;

    /** 审批时间 */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /** 审批意见 */
    @TableField("opinion")
    private String opinion;

    /** 原能力等级 */
    @TableField("orig_level")
    private String origLevel;

    /** 拟破格等级 */
    @TableField("target_level")
    private String targetLevel;

    /** 突破限制项(逗号分隔) */
    @TableField("break_items")
    private String breakItems;

    /** 人才特殊标签 */
    @TableField("special_tag")
    private String specialTag;

    /** 佐证材料 */
    @TableField("evidence")
    private String evidence;

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
