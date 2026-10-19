package com.example.strategymgt.workplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("workplan_score")
public class WorkplanScore {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Long taskId;
    private String scoreItem;
    private BigDecimal scoreWeight;
    private BigDecimal scoreValue;
    private String scorer;
    private LocalDateTime scoreTime;
    private String scoreOpinion;
    /** 扣分明细（逾期/质量扣分） */
    @TableField("deduct_detail")
    private String deductDetail;
    /** 调整原因（人工调整分数时必填） */
    @TableField("adjust_reason")
    private String adjustReason;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
