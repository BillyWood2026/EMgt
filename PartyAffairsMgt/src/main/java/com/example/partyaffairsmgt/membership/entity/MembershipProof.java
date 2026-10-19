package com.example.partyaffairsmgt.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 党籍证明开具记录（5.1）。 */
@Data
@TableName("membership_proof")
public class MembershipProof {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String memberName;
    private String proofNo;
    private String purpose;
    private LocalDate issueDate;
    private String issuer;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
