package com.example.partyaffairsmgt.opinion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 舆情报告（需求 8.5：自动汇总、闭环率、环比、数据源缺失标注）。 */
@Data
@TableName("opinion_report")
public class OpinionReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String reportNo;
    private String title;
    /** 日报/周报/月报/专报 */
    private String reportType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String content;
    private String reporter;
    private LocalDateTime reportTime;
    /** 草稿/已提交/已发布 */
    private String status;
    private Integer totalCount;
    private Integer negativeCount;
    /** 热点TOP10 */
    private String top10;
    private Integer closedCount;
    private Integer dueCount;
    /** 处置闭环率%（已归档/应处置） */
    private BigDecimal closeRate;
    /** 环比变化% */
    private BigDecimal momChange;
    /** 数据源缺失说明（不生成虚假数据） */
    private String dataNote;
    private Long templateId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
