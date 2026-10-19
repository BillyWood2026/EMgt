package com.example.culturemgt.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 氛围短板整改任务。
 * 状态机：待方案提交 →（责任人提交方案）→ 方案待审核 →（文化专员通过）→ 整改实施中
 *        →（提交验收材料）→ 待验收 →（验收）→ 验收闭环 / 验收驳回重整改（回整改实施中）
 *        方案审核驳回回 待方案提交。
 */
@Data
@TableName("cul_survey_rectify")
public class SurveyRectify {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionnaireId;
    private String sourceTag;
    private String title;
    private String dept;
    private String owner;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;
    private String status;
    private String plan;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planSubmitTime;
    private String reviewOpinion;
    private String implementNote;
    private String evidence;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closeTime;
    private String creator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private Integer deleted;
}
