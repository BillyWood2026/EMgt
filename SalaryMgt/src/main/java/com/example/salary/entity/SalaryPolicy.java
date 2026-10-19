package com.example.salary.entity;

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
 * 薪酬政策 Entity
 */
@Data
@TableName("salary_policy")
public class SalaryPolicy {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("policy_name")
    private String policyName;

    @TableField("policy_type")
    private String policyType;

    @TableField("effective_date")
    private LocalDate effectiveDate;

    @TableField("expiry_date")
    private LocalDate expiryDate;

    @TableField("min_salary")
    private BigDecimal minSalary;

    @TableField("max_salary")
    private BigDecimal maxSalary;

    @TableField("adjustment_cycle")
    private String adjustmentCycle;

    @TableField("description")
    private String description;

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
