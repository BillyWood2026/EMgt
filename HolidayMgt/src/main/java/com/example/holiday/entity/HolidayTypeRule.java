package com.example.holiday.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 假期类型政策 (Policy.vue -> /api/holiday/type-rule/type)
 * JSON 字段与前端完全一致：leaveTypeCode / leaveTypeName / leaveCategory /
 * effectiveStartDate / effectiveEndDate / status / maxAnnualDays / salaryPaidFlag / description
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holiday_type_rule")
public class HolidayTypeRule extends BaseEntity {

    /** 假期类型编码 */
    @TableField("leave_type_code")
    private String leaveTypeCode;

    /** 政策名称 */
    @TableField("leave_type_name")
    private String leaveTypeName;

    /** 适用对象 all/regular/probation/manager（前端 leaveCategory） */
    @TableField("leave_category")
    private String leaveCategory;

    /** 适用组织层级（列表回显 applyTo 读取该字段） */
    @TableField("applicable_org_level")
    private String applicableOrgLevel;

    /** 年度最大天数 */
    @TableField("max_annual_days")
    private Integer maxAnnualDays;

    /** 最大连续天数 */
    @TableField("max_consecutive_days")
    private Integer maxConsecutiveDays = 0;

    /** 是否带薪 */
    @TableField("salary_paid_flag")
    private Boolean salaryPaidFlag;

    /** 生效日期 */
    @TableField("effective_start_date")
    private LocalDate effectiveStartDate;

    /** 失效日期 */
    @TableField("effective_end_date")
    private LocalDate effectiveEndDate;

    /** 状态 ACTIVE/INACTIVE/DRAFT */
    @TableField("status")
    private String status = "ACTIVE";

    /** 政策说明 */
    @TableField("description")
    private String description;
}
