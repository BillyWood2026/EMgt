package com.example.partyaffairsmgt.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("assess_check")
public class AssessCheck {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long indicatorId;
    private BigDecimal dataValue;
    private BigDecimal checkScore;
    private String checkOpinion;
    private Integer vetoHit;
    private String checkBy;
    private LocalDateTime checkTime;

    @TableLogic
    private Integer deleted;
}
