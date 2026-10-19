package com.example.strategymgt.workplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("workplan_approval")
public class WorkplanApproval {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    /** 审批节点：部门负责人/分管领导/总经理 */
    @TableField("approval_node")
    private String approvalNode;
    private String approvalStatus;
    private String approver;
    private String approvalOpinion;
    /** 驳回整改要求 */
    @TableField("reject_require")
    private String rejectRequire;
    private LocalDateTime approvalTime;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
