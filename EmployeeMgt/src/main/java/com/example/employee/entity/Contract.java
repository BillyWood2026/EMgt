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
 * 合同实体。字段与前端 staff-mgt/Contract.vue 的表格列与新增/编辑表单完全对应。
 */
@Data
@TableName("contract")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contract {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 合同编号(前端字段 contractNumber, 如 HT-2026-001) */
    private String contractNumber;

    /** 员工ID(前端下拉值可能为业务编号或员工主键, 统一按字符串存储) */
    private String employeeId;

    /** 员工姓名 */
    private String employeeName;

    /** 合同类型(LABOR/SERVICE/CONFIDENTIALITY/NON_COMPETE) */
    private String contractType;

    /** 合同开始日期(前端字段 contractStartDate) */
    private LocalDate contractStartDate;

    /** 合同结束日期(前端字段 contractEndDate) */
    private LocalDate contractEndDate;

    /** 合同期限类型(FIXED/UNFIXED) */
    private String contractDurationType;

    /** 签约主体 */
    private String signingEntity;

    /** 试用期时长(月) */
    private Integer probationMonths;

    /** 经办人 */
    private String handler;

    /** 合同附件 */
    private String attachment;

    /** 续签次数 */
    private Integer renewCount;

    /** 终止原因 */
    private String terminationReason;

    /** 终止日期 */
    private LocalDate terminationDate;

    /** 补偿金说明 */
    private String compensationNote;

    /** 合同状态(VALID/EXPIRED/TERMINATED/CANCELLED/VOID) */
    private String contractStatus;

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
