package com.example.strategymgt.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("budget_audit")
public class BudgetAudit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String auditCode;
    private String auditPeriod;
    private String auditScope;
    private String auditFindings;
    private Integer problemCount;
    private String rectifyStatus;
    private String auditor;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
