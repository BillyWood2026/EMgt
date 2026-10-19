package com.example.safetymgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("safety_inspection")
public class SafetyInspection {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 检查名称 */
    private String inspectionName;
    /** 类型(daily/special/quarterly/yearly) */
    private String inspectionType;
    /** 检查人 */
    private String inspector;
    /** 检查日期 */
    private LocalDate inspectDate;
    /** 检查地点 */
    private String location;
    /** 风险等级(红/橙/黄/蓝) */
    private String riskLevel;
    /** 危害描述 */
    private String hazardDesc;
    /** 结果(qualified/rectifying/unqualified) */
    private String result;
    /** 问题描述 */
    private String issueDesc;
    /** 整改措施 */
    private String rectifyMeasure;
    /** 整改期限 */
    private LocalDate rectifyDeadline;
    /** 整改责任人 */
    private String responsiblePerson;
    /** 状态 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
