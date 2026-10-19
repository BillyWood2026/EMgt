package com.example.partyaffairsmgt.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("assess_appeal")
public class AssessAppeal {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String reason;
    private String status;
    private String handleResult;
    private String handleBy;
    private LocalDateTime handleTime;
    private BigDecimal scoreBefore;
    private BigDecimal scoreAfter;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
