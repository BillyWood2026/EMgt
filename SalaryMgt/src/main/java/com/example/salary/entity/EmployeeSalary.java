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
 * 员工薪资 / 职级薪资 Entity（对应前端 Grade.vue）
 */
@Data
@TableName("employee_salary")
public class EmployeeSalary {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 工号 */
    @TableField("employee_no")
    private String employeeNo;

    /** 员工姓名 */
    @TableField("employee_name")
    private String employeeName;

    /** 部门 */
    @TableField("department")
    private String department;

    /** 岗位 */
    @TableField("position")
    private String position;

    /** 职级 */
    @TableField("job_level")
    private String jobLevel;

    /** 定薪类型：入职定薪/转正定薪/定岗定薪 */
    @TableField("settle_type")
    private String settleType;

    /** 薪酬带宽区间，如 8000-15000 */
    @TableField("band_range")
    private String bandRange;

    /** 基本工资 */
    @TableField("basic_salary")
    private BigDecimal basicSalary;

    /** 绩效工资 */
    @TableField("performance_salary")
    private BigDecimal performanceSalary;

    /** 津贴补贴 */
    @TableField("allowance")
    private BigDecimal allowance;

    /** 总薪资 */
    @TableField("total_salary")
    private BigDecimal totalSalary;

    /** 生效日期 */
    @TableField("effective_date")
    private String effectiveDate;

    /** 状态：生效 / 未生效 / 已停用 */
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
