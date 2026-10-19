package com.example.improvemgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("improve_proposal")
public class ImprovementProposal {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("proposal_no")
    private String proposalNo;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("department")
    private String department;

    @TableField("proposer")
    private String proposer;

    @TableField("problem")
    private String problem;

    @TableField("solution")
    private String solution;

    @TableField("estimated_savings")
    private BigDecimal estimatedSavings;

    @TableField("priority")
    private String priority;

    @TableField("status")
    private String status;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("approval_comment")
    private String approvalComment;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_IMPLEMENTING = "implementing";
    public static final String STATUS_COMPLETED = "completed";
}
