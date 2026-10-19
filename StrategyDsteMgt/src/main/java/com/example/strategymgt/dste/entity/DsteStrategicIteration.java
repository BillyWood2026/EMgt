package com.example.strategymgt.dste.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("dste_strategic_iteration")
public class DsteStrategicIteration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String iterationCode;
    private String iterationReason;
    private String iterationBasis;
    private String iterationContent;
    private String beforeCompare;
    private String afterCompare;
    private String approvalStatus;
    private LocalDate effectiveTime;
    private String versionRecord;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
