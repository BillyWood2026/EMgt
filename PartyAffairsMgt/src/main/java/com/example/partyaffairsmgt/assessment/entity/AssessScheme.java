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
@TableName("assess_scheme")
public class AssessScheme {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String schemeName;
    private String periodType;
    private Integer assessYear;
    private String targetScope;
    private String targetOrgIds;
    private String targetOrgNames;
    private String indicatorVersion;
    private String weightSnapshot;
    private BigDecimal passLine;
    private BigDecimal goodLine;
    private BigDecimal excellentLine;
    private BigDecimal excellentRatio;
    private BigDecimal adjustCap;
    private Integer appealDays;
    private LocalDate selfDeadline;
    private LocalDate checkStart;
    private LocalDate onsiteDate;
    private LocalDate feedbackDate;
    private String status;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
