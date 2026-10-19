package com.example.partyaffairsmgt.common.party;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 操作审计日志（导出、审批、合并撤销等敏感动作）。 */
@Data
@TableName("party_audit_log")
public class PartyAuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String operator;
    private String roleCode;
    private String action;
    private String targetType;
    private String targetId;
    private String detail;
    private LocalDateTime createTime;
}
