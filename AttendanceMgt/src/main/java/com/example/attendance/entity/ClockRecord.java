package com.example.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日常打卡记录 (Daily.vue -> GET /api/attendance/clock-in)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_clock_record")
public class ClockRecord extends BaseEntity {

    /** 工号 */
    @TableField("employee_id")
    private String employeeId;

    /** 员工姓名 */
    @TableField("employee_name")
    private String employeeName;

    /** 部门 */
    @TableField("department")
    private String department;

    /** 打卡日期 */
    @TableField("record_date")
    private LocalDate recordDate;

    /** 打卡时间 */
    @TableField("clock_in_date_time")
    private LocalDateTime clockInDateTime;

    /** 打卡类型 IN/OUT */
    @TableField("clock_in_type")
    private String clockInType;

    /** 风险标记 NORMAL/LATE/EARLY/MISSING */
    @TableField("risk_flag")
    private String riskFlag;

    /** 异常说明 */
    @TableField("risk_description")
    private String riskDescription;

    /** 上班打卡 HH:mm */
    @TableField("check_in")
    private String checkIn;

    /** 下班打卡 HH:mm */
    @TableField("check_out")
    private String checkOut;

    /** 工时 */
    @TableField("work_hours")
    private BigDecimal workHours;

    /** 备注 */
    @TableField("remark")
    private String remark;
}
