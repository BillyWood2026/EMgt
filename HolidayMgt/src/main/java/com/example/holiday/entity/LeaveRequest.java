package com.example.holiday.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 请假申请 (Leave.vue -> /api/holiday/request)
 * JSON 字段与前端完全一致：requestNo / employeeName / departmentId / leaveTypeCode /
 * startDate / endDate / totalDays / leaveReason / requestStatus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holiday_leave_request")
public class LeaveRequest extends BaseEntity {

    /** 请假单号 LR-xxx */
    @TableField("request_no")
    private String requestNo;

    /** 工号 */
    @TableField("employee_id")
    private String employeeId;

    /** 员工姓名 */
    @TableField("employee_name")
    private String employeeName;

    /** 部门（前端 departmentId 绑定部门名） */
    @TableField("department_id")
    private String departmentId;

    /** 请假类型 personal/sick/annual/marriage/maternity/compassionate */
    @TableField("leave_type_code")
    private String leaveTypeCode;

    /** 开始日期 */
    @TableField("start_date")
    private LocalDate startDate;

    /** 结束日期 */
    @TableField("end_date")
    private LocalDate endDate;

    /** 时长(天) */
    @TableField("total_days")
    private BigDecimal totalDays;

    /** 请假原因 */
    @TableField("leave_reason")
    private String leaveReason;

    /** 审批状态 DRAFT/PENDING/APPROVED/REJECTED/CANCELLED */
    @TableField("request_status")
    private String requestStatus = "PENDING";

    /** 审批人 */
    @TableField("approver")
    private String approver;
}
