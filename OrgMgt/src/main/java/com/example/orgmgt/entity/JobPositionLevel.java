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
 * Job Position Level Entity
 * Implements five-level job position hierarchy for state-owned enterprise management
 * Strictly maps to five-level physical organization: Company -> Center -> Department -> Section -> Group
 */
@Data
@TableName("job_position_level")
public class JobPositionLevel {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("position_code")
    private String positionCode;
    
    @TableField("position_name")
    private String positionName;
    
    @TableField("org_level")
    private Integer orgLevel;
    
    @TableField("org_id")
    private Long orgId;
    
    @TableField("org_code")
    private String orgCode;
    
    @TableField("org_name")
    private String orgName;
    
    @TableField("position_grade")
    private String positionGrade;
    
    @TableField("position_status")
    private String positionStatus;
    
    @TableField("incumbent_id")
    private String incumbentId;
    
    @TableField("incumbent_name")
    private String incumbentName;
    
    @TableField("effective_date")
    private LocalDateTime effectiveDate;
    
    @TableField("expire_date")
    private LocalDateTime expireDate;
    
    @TableField("responsibility_desc")
    private String responsibilityDesc;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("is_core_management")
    private Boolean isCoreManagement;
    
    @TableField("is_temporary_post")
    private Boolean isTemporaryPost;
    
    @TableField("is_part_time_post")
    private Boolean isPartTimePost;
    
    @TableField("is_personalized")
    private Boolean isPersonalized;
    
    @TableField("personalized_name")
    private String personalizedName;
    
    @TableField("personalized_reason")
    private String personalizedReason;
    
    @TableField("is_in_staffing_control")
    private Boolean isInStaffingControl;
    
    @TableField("is_in_command_chain")
    private Boolean isInCommandChain;
    
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
    
    // Constants for position levels
    public static final int LEVEL_COMPANY = 1;      // General Manager
    public static final int LEVEL_CENTER = 2;       // Deputy General Manager, Director
    public static final int LEVEL_DEPARTMENT = 3;   // Manager, Deputy Manager
    public static final int LEVEL_SECTION = 4;      // Supervisor
    public static final int LEVEL_GROUP = 5;        // Team Leader
    
    // Position status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_VACANT = "VACANT";
    public static final String STATUS_ABOLISHED = "ABOLISHED";
    
    // Standard position names for each level
    public static final String[] STANDARD_POSITIONS = {
        "General Manager",           // Level 1 - Company
        "Deputy General Manager/Director",  // Level 2 - Center
        "Manager/Deputy Manager",    // Level 3 - Department
        "Supervisor",                // Level 4 - Section
        "Team Leader"                // Level 5 - Group
    };
}