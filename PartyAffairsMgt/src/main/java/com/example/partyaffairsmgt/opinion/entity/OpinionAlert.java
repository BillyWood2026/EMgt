package com.example.partyaffairsmgt.opinion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 舆情预警记录（需求 8.3：四色分级、推送状态、认领升级、同事件合并）。 */
@Data
@TableName("opinion_alert")
public class OpinionAlert {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long collectionId;
    private Long analysisId;
    private String title;
    /** 预警级别：红色/橙色/黄色/蓝色 */
    private String alertLevel;
    private String alertReason;
    private LocalDateTime alertTime;
    private String receiver;
    /** 处置时限（按等级：红24/橙48/黄72小时） */
    private LocalDateTime handleDeadline;
    private String sentiment;
    private String heatLevel;
    private String relevanceLevel;
    /** 推送状态：已推送/静默延迟/推送失败/已升级 */
    private String notifyStatus;
    /** 推送失败信息 */
    private String notifyFail;
    /** 认领状态：未认领/已认领/已升级 */
    private String claimStatus;
    private LocalDateTime claimTime;
    /** 事件聚类（同事件重复预警合并） */
    private String eventCluster;
    /** 待处置/处置中/已解除 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
