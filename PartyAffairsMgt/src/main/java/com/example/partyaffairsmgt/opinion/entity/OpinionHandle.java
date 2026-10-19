package com.example.partyaffairsmgt.opinion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 舆情处置工单（需求 8.4：七环节闭环 发现→研判→转办→处置→反馈→复核→归档）。 */
@Data
@TableName("opinion_handle")
public class OpinionHandle {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long alertId;
    private Long collectionId;
    private String title;
    /** 舆情等级：红色/橙色/黄色/蓝色 */
    private String alertLevel;
    /** 研判结论（属实性/影响面/风险等级） */
    private String researchConclusion;
    private String impact;
    /** 风险等级：高/中/低 */
    private String riskLevel;
    /** 承办单位 */
    private String unit;
    /** 责任人 */
    private String owner;
    /** 处置措施（回应、澄清、整改安排） */
    private String measure;
    private String handler;
    private LocalDateTime handleTime;
    /** 处置时限（按等级配置） */
    private LocalDateTime deadline;
    private String result;
    private String feedback;
    /** 反馈材料（佐证） */
    private String feedbackFile;
    /** 当前环节：发现/研判/转办/处置/反馈/复核/归档 */
    private String flowStatus;
    private String reviewer;
    private LocalDateTime reviewTime;
    /** 复核结果：通过/退回 */
    private String reviewResult;
    /** 归档状态：未归档/已归档 */
    private String archiveStatus;
    private LocalDateTime archiveTime;
    /** 重大舆情应急预案 */
    private Integer emergency;
    /** 转办认领时间（4小时无人认领升级） */
    private LocalDateTime claimTime;
    /** 待处置/处置中/已办结 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
