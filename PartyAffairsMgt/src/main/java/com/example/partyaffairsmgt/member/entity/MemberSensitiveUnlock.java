package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("member_sensitive_unlock")
public class MemberSensitiveUnlock {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String fieldName;
    private String applicant;
    private String reason;
    private String approver;
    private String auditStatus;
    private LocalDateTime auditTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
