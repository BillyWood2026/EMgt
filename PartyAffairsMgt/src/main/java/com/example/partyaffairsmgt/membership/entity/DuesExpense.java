package com.example.partyaffairsmgt.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dues_expense")
public class DuesExpense {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String expenseNo;
    private Long orgId;
    private String orgName;
    private String title;
    private String usageType;
    private Long planId;
    private String discussionAttachment;
    private String executeResult;
    /** 草稿/待审批/已批准/已登记/已核销 */
    private String auditStatus;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String purpose;
    private String handler;
    private String approver;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
