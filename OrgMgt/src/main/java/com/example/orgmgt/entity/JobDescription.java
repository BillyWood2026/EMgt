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
 * Job Description Entity
 * Represents the standardized job description template and content
 */
@Data
@TableName("job_description")
public class JobDescription {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("position_code")
    private String positionCode;
    
    @TableField("position_name")
    private String positionName;
    
    @TableField("org_unit")
    private String orgUnit;
    
    @TableField("position_level")
    private String positionLevel;
    
    @TableField("headcount")
    private Integer headcount;
    
    @TableField("job_summary")
    private String jobSummary;
    
    @TableField("main_responsibilities")
    private String mainResponsibilities;
    
    @TableField("work_permissions")
    private String workPermissions;
    
    @TableField("education_requirement")
    private String educationRequirement;
    
    @TableField("major_requirement")
    private String majorRequirement;
    
    @TableField("experience_requirement")
    private String experienceRequirement;
    
    @TableField("skill_requirement")
    private String skillRequirement;
    
    @TableField("quality_requirement")
    private String qualityRequirement;
    
    @TableField("work_relationship")
    private String workRelationship;
    
    @TableField("kpi_indicators")
    private String kpiIndicators;
    
    @TableField("salary_range")
    private String salaryRange;

    @TableField("duty_boundary")
    private String dutyBoundary;

    @TableField("reviewer")
    private String reviewer;

    @TableField("abandon_time")
    private LocalDateTime abandonTime;

    @TableField("change_note")
    private String changeNote;

    @TableField("remarks")
    private String remarks;

    @TableField("version_no")
    private String versionNo;
    
    @TableField("status")
    private String status;
    
    @TableField("write_status")
    private String writeStatus;
    
    @TableField("effective_time")
    private LocalDateTime effectiveTime;
    
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
    
    // Status constants
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";
    
    // Write status constants
    public static final String WRITE_STATUS_UNWRITTEN = "UNWRITTEN";
    public static final String WRITE_STATUS_DRAFT = "DRAFT";
    public static final String WRITE_STATUS_COMPLETED = "COMPLETED";
}