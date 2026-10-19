package com.example.culturemgt.common.cul;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** CCMS 全链路操作审计日志（永久留存，禁止物理删除）。 */
@Data
@TableName("cul_audit_log")
public class CulAuditLog {

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
