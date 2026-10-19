package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("command_chain_change_history")
public class CommandChainChangeHistory {

    /**
     * Primary Key
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Change Number
     */
    private String changeNo;

    /**
     * Chain ID
     */
    private Long chainId;

    /**
     * Chain Code
     */
    private String chainCode;

    /**
     * Chain Name
     */
    private String chainName;

    /**
     * Change Type (CREATE/UPDATE/PAUSE/RESUME/RENEW/ABANDON)
     */
    private String changeType;

    /**
     * Before Data (JSON)
     */
    private String beforeData;

    /**
     * After Data (JSON)
     */
    private String afterData;

    /**
     * Change Rules Difference
     */
    private String changeRulesDiff;

    /**
     * Change Description
     */
    private String changeDesc;

    /**
     * Initiator ID
     */
    private String initiatorId;

    /**
     * Initiator Name
     */
    private String initiatorName;

    /**
     * Change Time
     */
    private LocalDateTime changeTime;

    /**
     * Approval Nodes
     */
    private String approvalNodes;

    /**
     * Approval Result (PENDING/APPROVED/REJECTED)
     */
    private String approvalResult;

    /**
     * Approval Time
     */
    private LocalDateTime approvalTime;

    /**
     * Operator
     */
    private String operator;

    /**
     * IP Address
     */
    private String ipAddress;

    /**
     * Version Number
     */
    private String versionNo;

    /**
     * Attachment Path
     */
    private String attachmentPath;

    /**
     * Remarks
     */
    private String remarks;

    /**
     * Create Time
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}