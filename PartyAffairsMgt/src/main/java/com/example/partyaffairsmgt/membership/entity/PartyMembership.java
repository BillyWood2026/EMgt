package com.example.partyaffairsmgt.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("party_membership")
public class PartyMembership {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String memberName;
    private String cardNo;
    private String membershipStatus;
    /** 正常/停止党籍/转出/除名/开除党籍/死亡注销/自行脱党 */
    private String currentStatus;
    private LocalDate stopDate;
    private LocalDate restoreDate;
    private LocalDate joinDate;
    private LocalDate probationStart;
    private LocalDate probationEnd;
    private LocalDate fullMemberDate;
    private LocalDate statusChangeDate;
    private String reason;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
