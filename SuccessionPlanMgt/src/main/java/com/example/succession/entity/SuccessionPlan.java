package com.example.succession.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Succession Plan Entity (继任管理)
 */
@Data
@TableName("succession_plan")
public class SuccessionPlan {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("position_id")
    private Long positionId;

    @TableField("position_name")
    private String positionName;

    @TableField("successor_id")
    private Long successorId;

    @TableField("successor_name")
    private String successorName;

    @TableField("readiness")
    private String readiness;

    @TableField("development_plan")
    private String developmentPlan;

    @TableField("review_date")
    private LocalDate reviewDate;

    @TableField("status")
    private String status;

    /** 匹配分 */
    @TableField("match_score")
    private BigDecimal matchScore;

    /** 断层风险指数 */
    @TableField("risk_index")
    private BigDecimal riskIndex;

    /** 绩效得分 */
    @TableField("perf_score")
    private BigDecimal perfScore;

    /** 潜力得分 */
    @TableField("potential_score")
    private BigDecimal potentialScore;

    /** 资质得分 */
    @TableField("qual_score")
    private BigDecimal qualScore;

    /** 经验得分 */
    @TableField("exp_score")
    private BigDecimal expScore;

    /** 离职率(%) */
    @TableField("attrition_rate")
    private BigDecimal attritionRate;

    /** 断层率(%) */
    @TableField("gap_rate")
    private BigDecimal gapRate;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;
}
