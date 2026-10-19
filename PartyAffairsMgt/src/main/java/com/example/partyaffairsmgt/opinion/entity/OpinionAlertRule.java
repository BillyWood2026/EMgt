package com.example.partyaffairsmgt.opinion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 预警分级规则（需求 8.3 表30：阈值矩阵参数化）。 */
@Data
@TableName("opinion_alert_rule")
public class OpinionAlertRule {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleName;
    /** 级别：红/橙/黄/蓝 */
    private String level;
    private String sentimentCond;
    private String heatCond;
    private String relevanceCond;
    /** 处置时限（小时）：红24/橙48/黄72 */
    private Integer deadlineHours;
    private String notifyWays;
    /** 认领超时（分钟），红色默认30 */
    private Integer claimTimeoutMin;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
