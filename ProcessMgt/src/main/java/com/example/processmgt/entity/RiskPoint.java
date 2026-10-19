package com.example.processmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程风险点 Entity
 */
@Data
@TableName("process_risk_point")
public class RiskPoint {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 风险编号（后端自动生成 RPyyyyMMdd####） */
    private String code;

    /** 风险点名称 */
    private String name;

    /** 风险等级: high/medium/low */
    private String severity;

    /** 关联流程 */
    private String relatedFlow;

    /** 关联节点 */
    private String relatedNode;

    /** 管控措施 */
    private String controlMeasure;

    /** 合规条款 */
    private String complianceClause;

    /** 禁止行为 */
    private String prohibitedAction;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
