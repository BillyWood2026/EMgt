package com.example.partyaffairsmgt.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 审计日志（需求 12.5：只增不改不删）。 */
@Data
@TableName("sys_audit_log")
public class SysAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String operator;
    private String roleCode;
    /** 登录/操作/敏感 */
    private String opType;
    private String action;
    private String targetType;
    private String targetId;
    private String ip;
    private String beforeValue;
    private String afterValue;
    private String detail;
    private LocalDateTime createTime;
}
