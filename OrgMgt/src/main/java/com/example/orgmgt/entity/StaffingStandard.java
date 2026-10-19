package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Staffing Standard Entity
 * Defines fixed and flexible staffing standards for positions
 * Supports version management for standard updates
 */
@Data
@TableName("staffing_standard")
public class StaffingStandard {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    // Standard code (unique, auto-generated)
    @TableField("standard_code")
    private String standardCode;
    
    // Organization ID
    @TableField("org_id")
    private Long orgId;
    
    // Organization code
    @TableField("org_code")
    private String orgCode;
    
    // Organization name
    @TableField("org_name")
    private String orgName;
    
    // Position level (L1-L5)
    @TableField("position_level")
    private String positionLevel;
    
    // Position category (MANAGEMENT/TECHNICAL/FUNCTIONAL/OPERATIONAL)
    @TableField("position_category")
    private String positionCategory;
    
    // Staffing type: FIXED/FLEXIBLE
    @TableField("staffing_type")
    private String staffingType;
    
    // Standard headcount quota
    @TableField("standard_quota")
    private Integer standardQuota;

    @TableField("position_code")
    private String positionCode;

    @TableField("position_name")
    private String positionName;

    @TableField("occupied_count")
    private Integer occupiedCount;

    @TableField("allow_overstaff")
    private Boolean allowOverstaff;

    @TableField("last_adjust_batch")
    private String lastAdjustBatch;

    // Effective date
    @TableField("effective_date")
    private LocalDate effectiveDate;
    
    // Expire date
    @TableField("expire_date")
    private LocalDate expireDate;
    
    // Standard basis/authority document
    @TableField("standard_basis")
    private String standardBasis;
    
    // Version number
    @TableField("version_no")
    private String versionNo;
    
    // Status: ACTIVE/INACTIVE
    @TableField("status")
    private String status;
    
    // Remark
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
    
    // Staffing type constants
    public static final String TYPE_FIXED = "FIXED";
    public static final String TYPE_FLEXIBLE = "FLEXIBLE";
    
    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
}