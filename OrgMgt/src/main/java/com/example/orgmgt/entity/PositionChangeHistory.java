package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("position_change_history")
public class PositionChangeHistory {

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
     * Position ID
     */
    private Long positionId;

    /**
     * Position Code
     */
    private String positionCode;

    /**
     * Position Name
     */
    private String positionName;

    /**
     * Change Type (CREATE/UPDATE/DELETE/STATUS_CHANGE/QUOTA_CHANGE)
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
     * Change Description
     */
    private String changeDesc;

    /**
     * Change Reason
     */
    private String changeReason;

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

    /**
     * Index Number
     */
    @TableField("index_no")
    private String indexNo;

}