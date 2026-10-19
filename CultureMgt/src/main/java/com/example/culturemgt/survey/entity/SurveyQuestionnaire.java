package com.example.culturemgt.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文化调研问卷。
 * 状态机：草稿 → 已发布 → 已结束；模板（template_flag=1）仅用于复制创建。
 * questions JSON：[{qid,type 单选|多选|量表|文本,stem,options[],dimension,required,maxScale}]
 */
@Data
@TableName("cul_survey_questionnaire")
public class SurveyQuestionnaire {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String periodType;
    private Integer anonymousFlag;
    private String targetOrg;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private String questions;
    private Integer targetCount;
    private String status;
    private Integer templateFlag;
    private String publisher;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
    private String creator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private Integer deleted;
}
