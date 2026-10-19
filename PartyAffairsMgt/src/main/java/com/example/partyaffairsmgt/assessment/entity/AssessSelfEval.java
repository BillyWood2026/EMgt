package com.example.partyaffairsmgt.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("assess_self_eval")
public class AssessSelfEval {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long indicatorId;
    private BigDecimal selfScore;
    private String selfDesc;
    private String evidence;
    private String submitBy;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
