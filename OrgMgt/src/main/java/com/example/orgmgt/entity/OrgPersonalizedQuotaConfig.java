package com.example.orgmgt.entity;

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
 * Organization Personalized Quota Configuration Entity
 * Supports customized management quota and span parameters per organization
 * for the five-level organizational structure
 * 
 * Priority: Organization-specific parameters > System default parameters
 */
@Data
@TableName("org_personalized_quota_config")
public class OrgPersonalizedQuotaConfig {
    
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
    
    // Organization level (COMPANY/CENTER/DEPARTMENT/SECTION/TEAM)
    @TableField("org_level")
    private String orgLevel;
    
    // Position level this config applies to (L1-L5)
    @TableField("position_level")
    private String positionLevel;
    
    // Custom maximum quota (can override system default)
    @TableField("custom_max_quota")
    private Integer customMaxQuota;
    
    // Custom minimum amplitude
    @TableField("custom_min_amplitude")
    private Integer customMinAmplitude;
    
    // Custom standard amplitude (optimal baseline)
    @TableField("custom_std_amplitude")
    private Integer customStdAmplitude;
    
    // Custom maximum amplitude
    @TableField("custom_max_amplitude")
    private Integer customMaxAmplitude;
    
    // Warning relaxation factor (e.g., 0.9 = 90% of limit triggers warning)
    @TableField("warning_factor")
    private BigDecimal warningFactor;
    
    // Effective date
    @TableField("effective_date")
    private LocalDate effectiveDate;
    
    // Expire date
    @TableField("expire_date")
    private LocalDate expireDate;
    
    // Configuration description
    @TableField("config_desc")
    private String configDesc;
    
    // Is custom parameters enabled
    @TableField("enabled")
    private Boolean enabled;
    
    // Approver
    @TableField("approver")
    private String approver;
    
    // Approval time
    @TableField("approval_time")
    private LocalDateTime approvalTime;
    
    // Approval result
    @TableField("approval_result")
    private String approvalResult;
    
    // Configurator
    @TableField("configured_by")
    private String configuredBy;
    
    // Configuration time
    @TableField("configure_time")
    private LocalDateTime configureTime;
    
    // Status: DRAFT, PENDING_APPROVAL, ACTIVE, INACTIVE, EXPIRED
    @TableField("status")
    private String status;
    
    // Version number
    @TableField("version_no")
    private String versionNo;
    
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
    
    // Status constants
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_REJECTED = "REJECTED";
}