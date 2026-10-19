package com.example.perf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 继任候选人 Entity（/api/succession，perf_mgt.succession_record）
 */
@Data
@TableName("succession_record")
public class SuccessionRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联计划编号 */
    @TableField("plan_no")
    private String planNo;

    @TableField("employee_no")
    private String employeeNo;

    @TableField("employee_name")
    private String employeeName;

    @TableField("position_name")
    private String positionName;

    @TableField("department")
    private String department;

    /** 九宫格定位 */
    @TableField("nine_box")
    private String nineBox;

    /** 潜力层级 */
    @TableField("potential_level")
    private String potentialLevel;

    /** 就绪度 */
    @TableField("readiness")
    private String readiness;

    /** 匹配分（绩效.3+潜力.3+资质.25+经验.15） */
    @TableField("match_score")
    private BigDecimal matchScore;

    /** 风险指数（离职.6+断层.4） */
    @TableField("risk_index")
    private BigDecimal riskIndex;

    /** 状态 草稿/提交审批/生效/作废 */
    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;

    @TableField("deleted")
    @TableLogic
    private Integer deleted = 0;

    @TableField("version")
    @Version
    private Integer version = 1;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;
}
