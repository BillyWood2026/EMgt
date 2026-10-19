package com.example.strategymgt.dste.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("dste_resource_match")
public class DsteResourceMatch {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String resourceCode;
    private Long relatedTaskId;
    private Long relatedGoalId;
    private String resourceType;
    private BigDecimal requiredAmount;
    private BigDecimal matchedAmount;
    private BigDecimal gapAmount;
    private String resourceSource;
    private LocalDate availableTime;
    private String resourceOwner;
    private String usageRule;
    private String matchStatus;
    private String approvalStatus;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
