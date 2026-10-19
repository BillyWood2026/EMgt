package com.example.holiday.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 假期余额 (Balance.vue -> /api/holiday/balance)
 * JSON 字段与前端完全一致：employeeId / employeeName / leaveTypeCode /
 * availableBalance / usedBalance / balanceYear / balanceStatus / remark
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holiday_balance")
public class HolidayBalance extends BaseEntity {

    /** 工号 */
    @TableField("employee_id")
    private String employeeId;

    /** 员工姓名 */
    @TableField("employee_name")
    private String employeeName;

    /** 部门 */
    @TableField("department")
    private String department;

    /** 假期类型 annual/timeoff/sick/marriage/maternity/compassionate */
    @TableField("leave_type_code")
    private String leaveTypeCode;

    /** 总额度(天) */
    @TableField("available_balance")
    private BigDecimal availableBalance;

    /** 已用(天) */
    @TableField("used_balance")
    private BigDecimal usedBalance;

    /** 生效年度 */
    @TableField("balance_year")
    private Integer balanceYear;

    /** 状态 ACTIVE/EXPIRED/CARRYOVER */
    @TableField("balance_status")
    private String balanceStatus = "ACTIVE";

    /** 备注 */
    @TableField("remark")
    private String remark;
}
