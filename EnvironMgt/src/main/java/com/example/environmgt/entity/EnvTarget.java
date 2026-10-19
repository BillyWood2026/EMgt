package com.example.environmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 环境目标（新增环境目标 → 可发起 BPM 审批）
 */
@Data
@TableName("env_target")
public class EnvTarget {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 目标编号（后端自动生成） */
    private String targetNo;
    /** 目标名称 */
    private String targetName;
    /** 厂区(SZ深圳/DG东莞/SZ2苏州) */
    private String factory;
    /** 业务域(SMT/ASM/RD) */
    private String bizDomain;
    /** 目标类别(节能减排/危废减量/合规提升) */
    private String category;
    /** 基线值 */
    private BigDecimal baselineValue;
    /** 目标值 */
    private BigDecimal targetValue;
    /** 单位 */
    private String unit;
    /** 完成期限 */
    private LocalDate deadline;
    /** 责任人 */
    private String owner;
    /** 状态(草稿/审批中/已批准/进行中/已完成) */
    private String status;
    /** 备注 */
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
