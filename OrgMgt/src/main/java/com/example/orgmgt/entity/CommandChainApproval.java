package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("command_chain_approval")
public class CommandChainApproval {

    /**
     * Primary Key
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Approval Number
     */
    private String approvalNo;

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
     * Change Type
     */
    private String changeType;

    /**
     * Change Content
     */
    private String changeContent;

    /**
     * Request Time
     */
    private LocalDateTime requestTime;

    /**
     * Approval Status
     */
    private String approvalStatus;

    /**
     * Current Approval Node
     */
    private String currentNode;

    /**
     * Current Approver
     */
    private String currentApprover;

    /**
     * Approval History
     */
    private String approvalHistory;

    /**
     * Approval Time
     */
    private LocalDateTime approvalTime;

    /**
     * Reject Reason
     */
    private String rejectReason;

    /**
     * Created By
     */
    private String createdBy;

    /**
     * Create Time
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}