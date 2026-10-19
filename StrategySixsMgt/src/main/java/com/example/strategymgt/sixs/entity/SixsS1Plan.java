package com.example.strategymgt.sixs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sixs_s1_plan")
public class SixsS1Plan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String monitorCode;
    private String monitorPeriod;
    private String strategyFocus;
    private String strategyGoal;
    private BigDecimal planProgress;
    private String planStatus;
    private String deviationReason;
    private String rectifyMeasure;
    private String reviewOpinion;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
