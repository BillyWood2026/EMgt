package com.example.partyaffairsmgt.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 党籍变动记录（5.1）。 */
@Data
@TableName("membership_change")
public class MembershipChange {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String memberName;
    private Long orgId;
    private String orgName;
    /** 停止党籍/恢复党籍/自行脱党/除名/开除党籍/死亡注销/转入/转出 */
    private String changeType;
    private String reason;
    private String basisNo;
    private LocalDate effectiveDate;
    private LocalDate branchMeetingDate;
    private String approver;
    private LocalDate approveDate;
    /** 待审批/已批准/已驳回 */
    private String auditStatus;
    private String auditOpinion;
    /** MANUAL / DUES */
    private String source;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
