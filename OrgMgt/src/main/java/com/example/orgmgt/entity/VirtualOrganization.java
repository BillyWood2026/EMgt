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
 * Virtual Organization Entity
 * Implements Huawei-style virtual organization management for cross-department collaboration
 * Independent from five-level physical organization, does not affect entity org structure
 */
@Data
@TableName("virtual_organization")
public class VirtualOrganization {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("virtual_org_code")
    private String virtualOrgCode;
    
    @TableField("virtual_org_name")
    private String virtualOrgName;
    
    @TableField("org_type")
    private String orgType;
    
    @TableField("leader_id")
    private String leaderId;
    
    @TableField("leader_name")
    private String leaderName;
    
    @TableField("lead_org_id")
    private Long leadOrgId;
    
    @TableField("lead_org_code")
    private String leadOrgCode;
    
    @TableField("lead_org_name")
    private String leadOrgName;
    
    @TableField("participating_orgs")
    private String participatingOrgs;
    
    @TableField("effective_date")
    private LocalDateTime effectiveDate;
    
    @TableField("expire_date")
    private LocalDateTime expireDate;
    
    @TableField("org_status")
    private String orgStatus;
    
    @TableField("member_count")
    private Integer memberCount;
    
    @TableField("work_objective")
    private String workObjective;
    
    @TableField("remarks")
    private String remarks;
    
    @TableField("created_by")
    private String createdBy;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("updated_by")
    private String updatedBy;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;
    
    // Organization type constants
    public static final String TYPE_PROJECT = "PROJECT";      // Project-based
    public static final String TYPE_SPECIAL = "SPECIAL";      // Special task
    public static final String TYPE_TEMPORARY = "TEMPORARY";  // Temporary team
    
    // Status constants
    public static final String STATUS_PREPARING = "PREPARING";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_EXPIRING = "EXPIRING";
    public static final String STATUS_DISBAND = "DISBAND";
    
    public String getOrgTypeName() {
        switch (orgType) {
            case TYPE_PROJECT: return "Project Team";
            case TYPE_SPECIAL: return "Special Task Group";
            case TYPE_TEMPORARY: return "Temporary Team";
            default: return "Unknown";
        }
    }
}