package com.example.strategymgt.blm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("blm_business_design")
public class BlmBusinessDesign {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String designCode;
    private String strategyCycle;
    private String targetCustomers;
    private String valueProposition;
    private String profitModel;
    private String coreActivities;
    private String strategicControl;
    private String differentiation;
    private String businessBoundary;
    private String relatedInnovationIds;
    private Long relatedGoalId;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
