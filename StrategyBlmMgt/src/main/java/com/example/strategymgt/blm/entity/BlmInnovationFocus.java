package com.example.strategymgt.blm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("blm_innovation_focus")
public class BlmInnovationFocus {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String innovationCode;
    private Integer planYear;
    private String innovationType;
    private String innovationTrack;
    private String coreContent;
    private BigDecimal expectedBenefit;
    private String landingRisk;
    private String resourceNeed;
    private String priority;
    private Long relatedGoalId;
    private String followDept;
    private String owner;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
