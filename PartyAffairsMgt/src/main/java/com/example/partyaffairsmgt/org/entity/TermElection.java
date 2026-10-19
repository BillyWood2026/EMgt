package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("term_election")
public class TermElection {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String orgName;
    private String termNo;
    private String meetingName;
    /** 党员大会 / 党员代表大会 */
    private String electionMode;
    private LocalDate electionDate;
    private String meetingLocation;
    /** 应到会有表决权人数 */
    private Integer shouldVoteCount;
    /** 实到人数 */
    private Integer actualCount;
    private Integer ballotsIssued;
    private Integer ballotsReceived;
    private Integer validVotes;
    private Integer invalidVotes;
    private Integer blankVotes;
    /** 批复文号 */
    private String approvalNo;
    private LocalDate recordDate;
    private LocalDate publicityStart;
    private LocalDate publicityEnd;
    /** 提前换届是否已经上级批准 */
    private Integer earlyApproval;
    private String earlyApprovalFile;
    /** PLAN/CANDIDATE/MEETING/FILED */
    private String stage;
    private Integer tallyDone;
    /** 方案步骤 JSON */
    private String planData;
    private LocalDate termStart;
    private LocalDate termEnd;
    private String electedSecretary;
    private String electedMembers;
    private Integer voterCount;
    /** 进行中/已备案/已审批 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
