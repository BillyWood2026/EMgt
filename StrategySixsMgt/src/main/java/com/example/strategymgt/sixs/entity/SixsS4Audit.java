package com.example.strategymgt.sixs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sixs_s4_audit")
public class SixsS4Audit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String auditCode;
    private String auditPeriod;
    private String auditScope;
    private String auditType;
    private String auditFindings;
    private Integer problemCount;
    private Integer majorProblemCount;
    private String rectifyStatus;
    private BigDecimal rectifyRate;
    private LocalDate rectifyDeadline;
    private String auditor;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
