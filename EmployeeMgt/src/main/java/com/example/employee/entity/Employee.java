package com.example.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体。字段与前端 staff-mgt/List.vue 的表格列与新增/编辑表单完全对应。
 */
@Data
@TableName("employee")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务员工编号(前端字段 employeeId) */
    private String employeeId;

    /** 员工姓名(前端字段 employeeName) */
    private String employeeName;

    /** 性别(男/女) */
    private String gender;

    /** 身份证号 */
    private String idCard;

    /** 手机号 */
    private String phone;

    /** 部门名称(前端字段 deptName) */
    private String deptName;

    /** 岗位名称(前端字段 positionName) */
    private String positionName;

    /** 岗位序列 */
    private String positionSeq;

    /** 职级档位 */
    private String rankLevel;

    /** 人员类型 */
    private String employType;

    /** 涉密等级 */
    private String secretLevel;

    /** 民族 */
    private String nation;

    /** 籍贯 */
    private String nativePlace;

    /** 婚姻状态 */
    private String maritalStatus;

    /** 政治面貌 */
    private String politicalStatus;

    /** 紧急联系人 */
    private String emergencyContact;

    /** 紧急电话 */
    private String emergencyPhone;

    /** 入职日期(前端字段 hireDate, YYYY-MM-DD) */
    private LocalDate hireDate;

    /** 合同期限(1y/2y/3y/permanent) */
    private String contractTerm;

    /** 员工状态(ACTIVE/PROBATION/RESIGNED/RETIRED/STANDBY) */
    private String employeeStatus;

    /** 备注 */
    private String remark;

    @TableLogic
    private Integer deleted = 0;

    @Version
    private Integer version = 1;

    private LocalDateTime createTime;

    private String createBy;

    private LocalDateTime updateTime;

    private String updateBy;
}
