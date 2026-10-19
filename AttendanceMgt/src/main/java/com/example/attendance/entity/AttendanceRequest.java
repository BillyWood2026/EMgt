package com.example.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考勤申请单（加班）(Overtime.vue -> /api/attendance/requests)
 * JSON 字段与前端完全一致：requestNo / employeeName / requestType /
 * startDate / endDate / durationHours / compensation / reason / approvalStatus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_request")
public class AttendanceRequest extends BaseEntity {

    /** 业务单号 OT-xxx（路径参数 overtimeNo） */
    @TableField("request_no")
    private String requestNo;

    /** 工号 */
    @TableField("employee_id")
    private String employeeId;

    /** 员工姓名 */
    @TableField("employee_name")
    private String employeeName;

    /** 部门 */
    @TableField("department")
    private String department;

    /** 申请类型 OVERTIME */
    @TableField("request_type")
    private String requestType = "OVERTIME";

    /** 加班类型 weekday/weekend/holiday */
    @TableField("overtime_type")
    private String overtimeType;

    /** 补偿方式 timeoff/pay */
    @TableField("compensation")
    private String compensation;

    /** 开始时间（前端字段 startDate） */
    @TableField("start_date_time")
    private LocalDateTime startDate;

    /** 结束时间（前端字段 endDate） */
    @TableField("end_date_time")
    private LocalDateTime endDate;

    /** 加班时长（小时） */
    @TableField("duration_hours")
    private BigDecimal durationHours;

    /** 加班原因 */
    @TableField("reason")
    private String reason;

    /** 审批状态 PENDING/APPROVED/REJECTED/CANCELLED */
    @TableField("approval_status")
    private String approvalStatus = "PENDING";

    /** 审批人 */
    @TableField("approver")
    private String approver;
}
