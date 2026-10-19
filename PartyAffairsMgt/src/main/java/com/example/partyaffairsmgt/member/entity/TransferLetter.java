package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("transfer_letter")
public class TransferLetter {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String letterNo;
    private Long transferId;
    private Long memberId;
    private String memberName;
    private Long fromOrgId;
    private String fromOrgName;
    private Long toOrgId;
    private String toOrgName;
    private LocalDate issueDate;
    private Integer validDays;
    private LocalDate expireDate;
    private LocalDate receiveDate;
    private String receiver;
    private String receiptNo;
    private LocalDate receiptDate;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
