package com.example.partyaffairsmgt.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 报名与签到（需求 9.2：四种签到方式、去重、补签审批、定位半径）。 */
@Data
@TableName("activity_enrollment")
public class ActivityEnrollment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private String activityTitle;
    private Long orgId;
    private String orgName;
    private Long memberId;
    private String memberName;
    private LocalDateTime enrollTime;
    private LocalDateTime signInTime;
    private LocalDateTime signOutTime;
    /** 已参加/请假/缺席 */
    private String attendStatus;
    /** 现场扫码/定位/代签/离线补签 */
    private String signMethod;
    /** 未签到/已签到/待核实 */
    private String signStatus;
    /** 补签原因（必填） */
    private String makeUpReason;
    /** 补签审批：待审/通过/驳回 */
    private String makeUpAudit;
    /** 定位距活动地点米数（半径默认500） */
    private Integer distanceM;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
