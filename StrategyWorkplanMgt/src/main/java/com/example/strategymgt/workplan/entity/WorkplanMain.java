package com.example.strategymgt.workplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("workplan_main")
public class WorkplanMain {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String planCode;
    private String planName;
    private String planType;
    private Integer planYear;
    private String planPeriod;
    private Long templateId;
    /** 计划来源：战略解码/部门职责/临时交办 */
    @TableField("source_type")
    private String sourceType;
    /** 战略对齐项（关联的战略目标/重点任务） */
    @TableField("align_item")
    private String alignItem;
    private BigDecimal totalWeight;
    private String planStatus;
    private String ownerDept;
    private String owner;
    private LocalDate startTime;
    private LocalDate endTime;
    private Long approvalId;
    private Integer lockStatus;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
