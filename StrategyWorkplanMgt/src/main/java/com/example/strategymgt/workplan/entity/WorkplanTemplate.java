package com.example.strategymgt.workplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("workplan_template")
public class WorkplanTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateCode;
    private String templateName;
    private String planType;
    /** 适用组织 */
    @TableField("apply_org")
    private String applyOrg;
    /** 适用岗位序列 */
    @TableField("apply_sequence")
    private String applySequence;
    /** 任务层级数 */
    @TableField("max_level")
    private Integer maxLevel;
    /** 总分权重% */
    @TableField("total_weight")
    private java.math.BigDecimal totalWeight;
    /** 逾期扣分标准 */
    @TableField("overdue_deduct")
    private String overdueDeduct;
    /** 节点考核开关 */
    @TableField("node_check_on")
    private Boolean nodeCheckOn;
    private String templateContent;
    private String defaultWbs;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
