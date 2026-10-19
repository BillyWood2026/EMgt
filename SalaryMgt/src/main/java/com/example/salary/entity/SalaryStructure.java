package com.example.salary.entity;

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
 * 薪酬体系 Entity
 */
@Data
@TableName("salary_structure")
public class SalaryStructure {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("structure_name")
    private String structureName;

    @TableField("structure_code")
    private String structureCode;

    @TableField("description")
    private String description;

    @TableField("base_salary")
    private BigDecimal baseSalary;

    @TableField("bonus_ratio")
    private BigDecimal bonusRatio;

    @TableField("allowance_items")
    private String allowanceItems;

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
