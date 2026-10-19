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
 * 薪资核算 Entity
 */
@Data
@TableName("salary_calculation")
public class SalaryCalculation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("employee_id")
    private Long employeeId;

    @TableField("employee_name")
    private String employeeName;

    @TableField("year")
    private Integer year;

    @TableField("month")
    private Integer month;

    @TableField("base_salary")
    private BigDecimal baseSalary;

    @TableField("bonus")
    private BigDecimal bonus;

    @TableField("allowance")
    private BigDecimal allowance;

    @TableField("deduction")
    private BigDecimal deduction;

    @TableField("total_salary")
    private BigDecimal totalSalary;

    @TableField("status")
    private String status;

    @TableField("calculation_date")
    private LocalDate calculationDate;

    @TableField("approver")
    private String approver;

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
