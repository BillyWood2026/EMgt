package com.example.strategymgt.dste.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("dste_review")
public class DsteReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reviewCode;
    private String reviewCycle;
    private String reviewScope;
    private String overallAchievement;
    private String highlights;
    private String problems;
    private String rootCause;
    private String improvementMeasures;
    private String responsibleSubjects;
    private LocalDate deadline;
    private String iterationSuggestion;
    private String reviewConclusion;
    private String attachments;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
