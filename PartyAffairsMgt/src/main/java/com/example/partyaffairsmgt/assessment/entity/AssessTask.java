package com.example.partyaffairsmgt.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("assess_task")
public class AssessTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long schemeId;
    private Long orgId;
    private String orgName;
    private String status;
    private LocalDateTime selfSubmitTime;
    private LocalDateTime checkTime;
    private LocalDateTime scoreTime;
    private LocalDateTime approveTime;
    private String approver;
    private String approveOpinion;
    private LocalDate appealDeadline;
    private LocalDateTime publishTime;
    private BigDecimal totalScore;
    private BigDecimal adjustScore;
    private String grade;
    private Integer vetoTriggered;
    private Integer rankNo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
