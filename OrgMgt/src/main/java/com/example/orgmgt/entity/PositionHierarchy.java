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
 * Position Hierarchy Entity
 * Defines hierarchical position titles within the organization structure
 * Supports concurrent positions, secondment, and personalized position naming
 */
@Data
@TableName("position_hierarchy")
public class PositionHierarchy {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("hierarchy_code")
    private String hierarchyCode;
    
    @TableField("hierarchy_name")
    private String hierarchyName;
    
    @TableField("org_level")
    private Integer orgLevel;
    
    @TableField("org_id")
    private Long orgId;
    
    @TableField("org_code")
    private String orgCode;
    
    @TableField("org_name")
    private String orgName;
    
    @TableField("hierarchy_grade")
    private String hierarchyGrade;
    
    @TableField("hierarchy_status")
    private String hierarchyStatus;
    
    @TableField("incumbents")
    private String incumbents;
    
    @TableField("effective_date")
    private LocalDate effectiveDate;
    
    @TableField("expire_date")
    private LocalDate expireDate;
    
    @TableField("duty_description")
    private String dutyDescription;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("is_core_management")
    private Integer isCoreManagement;
    
    @TableField("is_concurrent")
    private Integer isConcurrent;
    
    @TableField("is_seconded")
    private Integer isSeconded;
    
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
}