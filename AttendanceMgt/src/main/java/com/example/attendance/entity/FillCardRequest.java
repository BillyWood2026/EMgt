package com.example.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 补卡申请 (Daily.vue -> POST /api/attendance/clock-in/fill-card-request)
 * 前端以 query params 提交（body 为 null）：
 * requestNo / employeeId / missingDate / shouldCheckInTime / shouldCheckOutTime / reason
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_fill_card_request")
public class FillCardRequest extends BaseEntity {

    /** 补卡单号 BK-xxx */
    @TableField("request_no")
    private String requestNo;

    /** 工号 */
    @TableField("employee_id")
    private String employeeId;

    /** 员工姓名 */
    @TableField("employee_name")
    private String employeeName;

    /** 缺卡日期 */
    @TableField("missing_date")
    private LocalDate missingDate;

    /** 应上班时间 HH:mm */
    @TableField("should_check_in_time")
    private String shouldCheckInTime;

    /** 应下班时间 HH:mm */
    @TableField("should_check_out_time")
    private String shouldCheckOutTime;

    /** 补卡原因 */
    @TableField("reason")
    private String reason;

    /** 状态 PENDING/APPROVED/REJECTED */
    @TableField("status")
    private String status = "PENDING";
}
