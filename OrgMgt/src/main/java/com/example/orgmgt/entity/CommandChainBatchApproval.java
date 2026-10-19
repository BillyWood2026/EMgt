package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("command_chain_batch_approval")
public class CommandChainBatchApproval {

    /**
     * Primary Key
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Batch Number
     */
    private String batchNo;

    /**
     * Chain IDs (JSON array)
     */
    private String chainIds;

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
     * Approval Result (JSON)
     */
    private String approvalResult;

    /**
     * Approver
     */
    private String approver;

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