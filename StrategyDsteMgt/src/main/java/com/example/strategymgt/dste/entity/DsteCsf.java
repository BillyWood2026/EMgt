package com.example.strategymgt.dste.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dste_csf")
public class DsteCsf {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String csfCode;
    private Long relatedGoalId;
    private String csfDefinition;
    private String successCriteria;
    private String keyFactors;
    private String priority;
    private String responsibleDept;
    private String responsiblePerson;
    private String collaborateDepts;
    private String achieveCycle;
    private String relatedKpiIds;
    private String relatedTaskIds;
    private String completionStatus;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
