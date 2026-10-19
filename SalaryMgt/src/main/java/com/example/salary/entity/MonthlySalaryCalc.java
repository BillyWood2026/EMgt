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
 * 月度薪资核算 Entity（对应前端 Calculate.vue / History.vue）
 */
@Data
@TableName("monthly_salary_calc")
public class MonthlySalaryCalc {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 核算编号 */
    @TableField("calc_no")
    private String calcNo;

    /** 核算月份，格式 yyyy-MM */
    @TableField("month")
    private String month;

    /** 员工姓名 */
    @TableField("employee_name")
    private String employeeName;

    /** 部门 */
    @TableField("department")
    private String department;

    /** 岗位 */
    @TableField("position")
    private String position;

    /** 计薪天数 */
    @TableField("pay_days")
    private BigDecimal payDays;

    /** 基本工资 */
    @TableField("basic_salary")
    private BigDecimal basicSalary;

    /** 绩效工资 */
    @TableField("performance_salary")
    private BigDecimal performanceSalary;

    /** 奖金 */
    @TableField("bonus")
    private BigDecimal bonus;

    /** 津贴补贴 */
    @TableField("allowance")
    private BigDecimal allowance;

    /** 加班费 */
    @TableField("overtime_pay")
    private BigDecimal overtimePay;

    /** 应发合计 */
    @TableField("gross_salary")
    private BigDecimal grossSalary;

    /** 社保公积金 */
    @TableField("social_security")
    private BigDecimal socialSecurity;

    /** 个税 */
    @TableField("tax")
    private BigDecimal tax;

    /** 扣除 */
    @TableField("deduction")
    private BigDecimal deduction;

    /** 实发金额 */
    @TableField("net_salary")
    private BigDecimal netSalary;

    /** 状态：待核算 / 核算中 / 待审核 / 已审核 / 已发放 */
    @TableField("status")
    private String status;

    /** 核算时间 */
    @TableField("calc_time")
    private String calcTime;

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
