package com.example.talent.entity;

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
 * 岗位异动/调动/晋升调薪单 Entity（talent_mgt.movement_order）
 * 同时服务 /api/movement 与 /api/transfer 两个前缀。
 * 状态机：DRAFT(草稿) -> SUBMITTED(已提交) -> APPROVED(已批准) / REJECTED(已驳回)
 */
@Data
@TableName("movement_order")
public class MovementOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 异动单号 */
    @TableField("movement_no")
    private String movementNo;

    /** 员工编号 */
    @TableField("employee_no")
    private String employeeNo;

    /** 员工姓名 */
    @TableField("employee_name")
    private String employeeName;

    /** 部门 */
    @TableField("department")
    private String department;

    /** 异动类型 PROMOTION(晋升)/TRANSFER(调动)/DEMOTION(降级)/LATERAL(平级调整) */
    @TableField("movement_type")
    private String movementType;

    /** 原岗位 */
    @TableField("from_position")
    private String fromPosition;

    /** 新岗位 */
    @TableField("to_position")
    private String toPosition;

    /** 原职级 */
    @TableField("from_level")
    private String fromLevel;

    /** 新职级 */
    @TableField("to_level")
    private String toLevel;

    /** 原薪资 */
    @TableField("from_salary")
    private BigDecimal fromSalary;

    /** 新薪资 */
    @TableField("to_salary")
    private BigDecimal toSalary;

    /** 调薪额 */
    @TableField("adjust_amount")
    private BigDecimal adjustAmount;

    /** 调薪幅度(%) */
    @TableField("adjust_rate")
    private BigDecimal adjustRate;

    /** 生效日期 */
    @TableField("effective_date")
    private String effectiveDate;

    /** 异动原因 */
    @TableField("reason")
    private String reason;

    /** 状态 DRAFT/SUBMITTED/APPROVED/REJECTED */
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
