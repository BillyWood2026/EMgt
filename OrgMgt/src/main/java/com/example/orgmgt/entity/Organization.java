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
 * Organization Entity
 * Core entity for five-level organization structure: Company -> Center -> Department -> Section -> Group
 * Supports organization status management, accounting control, and staffing control
 */
@Data
@TableName("organization")
public class Organization {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("org_code")
    private String orgCode;
    
    @TableField("org_name")
    private String orgName;
    
    @TableField("org_level")
    private Integer orgLevel;
    
    @TableField("parent_org_id")
    private Long parentOrgId;
    
    @TableField("parent_org_code")
    private String parentOrgCode;
    
    @TableField("org_status")
    private String orgStatus;
    
    @TableField("establish_date")
    private LocalDate establishDate;
    
    @TableField("org_manager")
    private String orgManager;
    
    @TableField("contact_phone")
    private String contactPhone;
    
    @TableField("business_line")
    private String businessLine;
    
    @TableField("location")
    private String location;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("remark")
    private String remark;
    
    @TableField("is_core_org")
    private Integer isCoreOrg;
    
    @TableField("is_independent_accounting")
    private Integer isIndependentAccounting;
    
    @TableField("is_staffing_controlled")
    private Integer isStaffingControlled;
    
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
    
    // Level constants
    public static final int LEVEL_COMPANY = 1;
    public static final int LEVEL_CENTER = 2;
    public static final int LEVEL_DEPARTMENT = 3;
    public static final int LEVEL_SECTION = 4;
    public static final int LEVEL_GROUP = 5;
}