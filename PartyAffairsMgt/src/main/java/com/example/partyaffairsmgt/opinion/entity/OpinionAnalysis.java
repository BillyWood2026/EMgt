package com.example.partyaffairsmgt.opinion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 舆情分析（需求 8.2：情感分值、热度指数、关联度、聚类、批量标注、人工改判）。 */
@Data
@TableName("opinion_analysis")
public class OpinionAnalysis {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long collectionId;
    private String title;
    /** 情感倾向：正面/负面/中性 */
    private String sentiment;
    /** 情感分值 -1至1，<-0.2 判负面（阈值参数化） */
    private BigDecimal sentimentScore;
    /** 热度等级：高/中/低 */
    private String heatLevel;
    /** 热度指数 H=α转发+β评论+γ点赞+δ阅读（含时间衰减） */
    private BigDecimal heatValue;
    /** 关联度评分（关键词命中数量与位置加权） */
    private Integer relevanceScore;
    /** 关联度等级：高/中/低 */
    private String relevanceLevel;
    private String keywords;
    /** 聚类事件名 */
    private String eventCluster;
    /** 批量标注：属实/不属实/待核实 */
    private String markResult;
    private String analyst;
    private LocalDateTime analyzeTime;
    private String summary;
    /** 人工改判情感 */
    private String manualSentiment;
    /** 改判理由 */
    private String judgeReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
