package com.example.orgmgt.entity;

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
 * Management Span Standard Entity
 * Defines management span control standards for hierarchical positions
 * Implements Huawei-style minimum/standard/maximum span control
 * 
 * Control dimensions:
 * - Minimum span: Below this triggers management redundancy warning
 * - Standard span: Optimal configuration baseline
 * - Maximum span: Exceeding this blocks new personnel/positions
 */
@Data
@TableName("management_span_standard")
public class ManagementSpanStandard {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    // Position level (L1-L5 corresponding to five-level organization)
    @TableField("position_level")
    private String positionLevel;
    
    // Position name
    @TableField("position_name")
    private String positionName;
    
    // Minimum management span (direct subordinates)
    @TableField("min_span")
    private Integer minSpan;
    
    // Standard management span (optimal baseline)
    @TableField("standard_span")
    private Integer standardSpan;
    
    // Maximum management span (hard limit)
    @TableField("max_span")
    private Integer maxSpan;
    
    // Minimum subordinate organization count (for top-level positions)
    @TableField("min_org_count")
    private Integer minOrgCount;
    
    // Standard subordinate organization count
    @TableField("standard_org_count")
    private Integer standardOrgCount;
    
    // Maximum subordinate organization count
    @TableField("max_org_count")
    private Integer maxOrgCount;
    
    // Minimum subordinate personnel count
    @TableField("min_person_count")
    private Integer minPersonCount;
    
    // Standard subordinate personnel count
    @TableField("standard_person_count")
    private Integer standardPersonCount;
    
    // Maximum subordinate personnel count
    @TableField("max_person_count")
    private Integer maxPersonCount;
    
    // Strict mode: intercept if violated
    @TableField("is_strict")
    private Boolean isStrict;
    
    // Alert threshold ratio (e.g., 0.8 = alert when 80% of max reached)
    @TableField("alert_threshold")
    private BigDecimal alertThreshold;
    
    // Remark
    @TableField("remarks")
    private String remarks;
    
    // Creator
    @TableField("created_by")
    private String createdBy;
    
    // Create time
    @TableField("create_time")
    private LocalDateTime createTime;
    
    // Updater
    @TableField("updated_by")
    private String updatedBy;
    
    // Update time
    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;
}