package com.example.partyaffairsmgt.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 活动统计与考核（需求 9.5：完成率、参与率、未达标、扣分建议、数据待补齐标注）。 */
@Data
@TableName("activity_assessment")
public class ActivityAssessment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String orgName;
    private Integer assessYear;
    /** 年度/季度 */
    private String periodType;
    private Integer periodQuarter;
    private Integer planCount;
    private Integer actualCount;
    private BigDecimal attendanceRate;
    /** 三会一课完成率% */
    private BigDecimal qualifyRate;
    /** 党员参与率% */
    private BigDecimal participationRate;
    private Integer memberTotal;
    private Integer activityTimes;
    private Integer attendTimes;
    private BigDecimal score;
    private String grade;
    private LocalDate assessDate;
    private String comment;
    /** 未达标标记 */
    private Integer unqualifiedFlag;
    /** 考核扣分建议（联动第10章） */
    private String deductSuggestion;
    /** 数据待补齐说明 */
    private String dataNote;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
