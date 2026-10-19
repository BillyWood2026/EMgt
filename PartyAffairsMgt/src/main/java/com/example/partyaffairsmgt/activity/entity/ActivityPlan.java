package com.example.partyaffairsmgt.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 活动计划（需求 9.1：频次合规、时间冲突、审批；9.2：发布/报名截止/签到方式/应参加人数）。 */
@Data
@TableName("activity_plan")
public class ActivityPlan {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String orgName;
    private String title;
    /** 支部党员大会/支部委员会/党小组会/党课/主题党日/其他 */
    private String activityType;
    private LocalDate planDate;
    private String location;
    private String content;
    private String participantScope;
    private BigDecimal budget;
    private String planner;
    /** 草稿/已审批/已取消 */
    private String status;
    /** 主持人 */
    private String host;
    /** 是否纳入三会一课台账 */
    private Integer inLedger;
    private Integer periodYear;
    private Integer periodQuarter;
    private Integer periodMonth;
    /** 同组织同日三会冲突标记 */
    private Integer conflictFlag;
    /** 待审批/通过/退回 */
    private String approveStatus;
    private String approver;
    private LocalDateTime approveTime;
    private String approveOpinion;
    /** 未发布/已发布/已取消 */
    private String publishStatus;
    private LocalDateTime publishTime;
    private LocalDateTime enrollDeadline;
    /** 签到方式（逗号分隔） */
    private String signMethod;
    /** 应参加人数 */
    private Integer expectCount;
    /** 授课人（党课） */
    private String lecturer;
    /** 授课人职务：机关党委书记/班子成员/党支部书记 */
    private String lecturerRole;
    /** 定时发布时间 */
    private LocalDateTime scheduledPublish;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
