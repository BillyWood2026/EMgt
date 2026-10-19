package com.example.strategymgt.blm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("blm_gap_analysis")
public class BlmGapAnalysis {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String gapCode;
    private Integer planYear;
    private String gapType;
    private String benchmarkDim;
    private BigDecimal currentValue;
    private BigDecimal benchmarkValue;
    private BigDecimal targetValue;
    private BigDecimal gapValue;
    private BigDecimal gapRate;
    private String gapCause;
    private String businessUnits;
    private String priority;
    private String rectifyDirection;
    private Long relatedPlanId;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
