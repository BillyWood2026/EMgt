package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("relation_transfer")
public class RelationTransfer {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String memberName;
    private String transferType;
    private String fromOrg;
    private String toOrg;
    private String reason;
    private LocalDate applyDate;
    private LocalDate transferDate;
    private String letterNo;
    private String handler;
    private String status;
    private Long fromOrgId;
    private Long toOrgId;
    private String applyReason;
    private String currentNode;
    private LocalDate receiveDate;
    private String receiptStatus;
    private String flowStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
