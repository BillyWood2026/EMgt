package com.example.strategymgt.sixs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sixs_s5_performance")
public class SixsS5Performance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String evalCode;
    private String evalPeriod;
    private String evaluatedSubject;
    private String perfDim;
    private BigDecimal targetValue;
    private BigDecimal actualValue;
    private BigDecimal completionRate;
    private BigDecimal weight;
    private BigDecimal dimScore;
    private String perfLevel;
    private String evalOpinion;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
