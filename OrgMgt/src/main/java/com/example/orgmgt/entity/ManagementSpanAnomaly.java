package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Management Span Anomaly Record Entity
 * Records management quota anomalies including:
 * - Quota exceeded (职数超配)
 * - Low management amplitude (幅度低于最低标准)
 * - High management amplitude (幅度过载)
 * - Multiple chiefs violation (多正职)
 * 
 * Three-color warning system:
 * - GREEN: Compliant
 * - YELLOW: Warning (approaching threshold)
 * - RED: Critical (exceeded limits, blocked)
 */
@Data
@TableName("management_span_anomaly")
public class ManagementSpanAnomaly {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    // Organization ID
    @TableField("org_id")
    private Long orgId;
    
    // Organization code
    @TableField("org_code")
    private String orgCode;
    
    // Organization name
    @TableField("org_name")
    private String orgName;
    
    // Position ID
    @TableField("position_id")
    private Long positionId;
    
    // Position code
    @TableField("position_code")
    private String positionCode;
    
    // Position name
    @TableField("position_name")
    private String positionName;
    
    // Position level (L1-L5)
    @TableField("position_level")
    private String positionLevel;
    
    // Current quota (actual headcount)
    @TableField("current_quota")
    private Integer currentQuota;
    
    // Standard quota (configured standard)
    @TableField("standard_quota")
    private Integer standardQuota;
    
    // Maximum quota allowed
    @TableField("max_quota")
    private Integer maxQuota;
    
    // Current subordinate count
    @TableField("subordinate_count")
    private Integer subordinateCount;
    
    // Minimum amplitude standard
    @TableField("min_amplitude")
    private Integer minAmplitude;
    
    // Standard amplitude
    @TableField("standard_amplitude")
    private Integer standardAmplitude;
    
    // Maximum amplitude
    @TableField("max_amplitude")
    private Integer maxAmplitude;
    
    // Anomaly type: QUOTA_EXCEED, AMPLITUDE_LOW, AMPLITUDE_HIGH, MULTIPLE_CHIEFS
    @TableField("anomaly_type")
    private String anomalyType;
    
    // Warning level: GREEN, YELLOW, RED
    @TableField("warning_level")
    private String warningLevel;
    
    // Anomaly status: PENDING, PROCESSING, RESOLVED, EXEMPTED
    @TableField("status")
    private String status;
    
    // Occurrence time
    @TableField("occurrence_time")
    private LocalDateTime occurrenceTime;
    
    // Resolve time
    @TableField("resolve_time")
    private LocalDateTime resolveTime;
    
    // Rectification deadline
    @TableField("deadline")
    private LocalDateTime deadline;
    
    // Rectification description
    @TableField("rectification_desc")
    private String rectificationDesc;
    
    // Exemption approval number (if exempted)
    @TableField("exemption_approval_no")
    private String exemptionApprovalNo;
    
    // Exemption expiry date (if exempted)
    @TableField("exemption_expiry_date")
    private LocalDateTime exemptionExpiryDate;
    
    // Remarks
    @TableField("remark")
    private String remark;
    
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
    
    // Anomaly type constants
    public static final String ANOMALY_QUOTA_EXCEED = "QUOTA_EXCEED";
    public static final String ANOMALY_AMPLITUDE_LOW = "AMPLITUDE_LOW";
    public static final String ANOMALY_AMPLITUDE_HIGH = "AMPLITUDE_HIGH";
    public static final String ANOMALY_MULTIPLE_CHIEFS = "MULTIPLE_CHIEFS";
    
    // Warning level constants
    public static final String LEVEL_GREEN = "GREEN";
    public static final String LEVEL_YELLOW = "YELLOW";
    public static final String LEVEL_RED = "RED";
    
    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_RESOLVED = "RESOLVED";
    public static final String STATUS_EXEMPTED = "EXEMPTED";
}