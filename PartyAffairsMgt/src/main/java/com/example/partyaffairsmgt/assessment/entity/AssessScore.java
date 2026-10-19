package com.example.partyaffairsmgt.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("assess_score")
public class AssessScore {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long indicatorId;
    private String scoreMode;
    private BigDecimal autoScore;
    private String judgeScores;
    private BigDecimal finalScore;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
