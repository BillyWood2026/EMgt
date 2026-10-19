package com.example.partyaffairsmgt.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 党费减免申请（5.3）。 */
@Data
@TableName("dues_reduce")
public class DuesReduce {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String memberName;
    private Long orgId;
    private String orgName;
    private Integer feeYear;
    private Integer feeMonth;
    /** 少交/免交 */
    private String reduceType;
    private BigDecimal amount;
    private String reason;
    /** 支部研究记录（必填，审批链完整性 E-DU-05） */
    private String branchRecord;
    private String approver;
    private LocalDate approveDate;
    /** 待审批/已批准/已驳回 */
    private String auditStatus;
    private String auditOpinion;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
