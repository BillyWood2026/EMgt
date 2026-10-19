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
 * Position Entity
 * Core position definition within organization structure
 * Supports position status, staffing count, and effective date management
 */
@Data
@TableName("position")
public class Position {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("position_code")
    private String positionCode;
    
    @TableField("position_name")
    private String positionName;
    
    @TableField("org_id")
    private Long orgId;
    
    @TableField("org_code")
    private String orgCode;
    
    @TableField("org_name")
    private String orgName;
    
    @TableField("position_level")
    private String positionLevel;
    
    @TableField("position_rank")
    private String positionRank;

    @TableField("position_short_name")
    private String positionShortName;

    @TableField("position_category")
    private String positionCategory;

    @TableField("salary_grade")
    private String salaryGrade;

    @TableField("owner")
    private String owner;

    @TableField("position_duty")
    private String positionDuty;
    
    @TableField("staffing_type")
    private String staffingType;
    
    @TableField("staffing_count")
    private Integer staffingCount;
    
    @TableField("current_count")
    private Integer currentCount;
    
    @TableField("position_status")
    private String positionStatus;
    
    @TableField("responsibilities")
    private String responsibilities;
    
    @TableField("qualifications")
    private String qualifications;
    
    @TableField("effective_date")
    private LocalDate effectiveDate;
    
    @TableField("expire_date")
    private LocalDate expireDate;
    
    @TableField("flexible_quota_expire_date")
    private LocalDateTime flexibleQuotaExpireDate;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("remark")
    private String remark;
    
    @TableField("created_by")
    private String createdBy;
    
    @TableField("created_time")
    private LocalDateTime createdTime;
    
    @TableField("updated_by")
    private String updatedBy;
    
    @TableField("updated_time")
    private LocalDateTime updatedTime;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;
    
    // Status constants
    public static final String STATUS_ENABLED = "ENABLED";
    public static final String STATUS_DISABLED = "DISABLED";
    
    // Staffing type constants
    public static final String STAFFING_FIXED = "FIXED";
    public static final String STAFFING_FLEXIBLE = "FLEXIBLE";
}