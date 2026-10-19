package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("permission_audit_log")
public class PermissionAuditLog {

    /**
     * Primary Key
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Log Time
     */
    private LocalDateTime logTime;

    /**
     * Operator
     */
    private String operator;

    /**
     * Role Code
     */
    private String roleCode;

    /**
     * Role Name
     */
    private String roleName;

    /**
     * Module Code
     */
    private String moduleCode;

    /**
     * Module Name
     */
    private String moduleName;

    /**
     * Function Code
     */
    private String functionCode;

    /**
     * Function Name
     */
    private String functionName;

    /**
     * Action (GRANT/REVOKE)
     */
    private String action;

    /**
     * IP Address
     */
    private String ipAddress;

    /**
     * Remarks
     */
    private String remarks;

    /**
     * Index Number
     */
    @com.baomidou.mybatisplus.annotation.TableField("index_no")
    private String indexNo;

}