package com.example.culturemgt.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

/** 调研问卷答卷（匿名 respondent=匿名用户；dimension_scores 提交时按量表题自动计算）。 */
@Data
@TableName("cul_survey_response")
public class SurveyResponse {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionnaireId;
    private String title;
    private String periodTag;
    private String respondent;
    private String respondentDept;
    private Integer anonymousFlag;
    private String answers;
    private String dimensionScores;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private Integer deleted;
}
