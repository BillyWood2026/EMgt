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
 * 薪资项目 / 薪资政策 Entity（对应前端 Policy.vue）
 */
@Data
@TableName("salary_item")
public class SalaryItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 政策编号 */
    @TableField("policy_no")
    private String policyNo;

    /** 政策名称 */
    @TableField("name")
    private String name;

    /** 政策类型：basic / performance / bonus / allowance */
    @TableField("type")
    private String type;

    /** 生效日期（页面以字符串提交/展示，如 2026-01-01） */
    @TableField("effective_date")
    private String effectiveDate;

    /** 适用范围 */
    @TableField("scope")
    private String scope;

    /** 政策内容 */
    @TableField("content")
    private String content;

    /** 取整规则：四舍五入到分/四舍五入到元/向上取整/向下取整 */
    @TableField("rounding_rule")
    private String roundingRule;

    /** 异常兜底值 */
    @TableField("fallback_value")
    private java.math.BigDecimal fallbackValue;

    /** 核算优先级 */
    @TableField("priority")
    private Integer priority;

    /** 状态：生效 / 已失效 */
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
