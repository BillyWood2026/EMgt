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
 * Virtual Organization Member Entity
 * Records members in virtual organization (cross-department part-time members)
 * Personnel affiliation, position, and staffing remain in original entity organization
 */
@Data
@TableName("virtual_organization_member")
public class VirtualOrganizationMember {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("virtual_org_id")
    private Long virtualOrgId;
    
    @TableField("virtual_org_code")
    private String virtualOrgCode;
    
    @TableField("virtual_org_name")
    private String virtualOrgName;
    
    @TableField("employee_id")
    private String employeeId;
    
    @TableField("employee_name")
    private String employeeName;
    
    @TableField("original_org_id")
    private Long originalOrgId;
    
    @TableField("original_org_code")
    private String originalOrgCode;
    
    @TableField("original_org_name")
    private String originalOrgName;
    
    @TableField("original_position_code")
    private String originalPositionCode;
    
    @TableField("original_position_name")
    private String originalPositionName;
    
    @TableField("virtual_role")
    private String virtualRole;
    
    @TableField("join_date")
    private LocalDateTime joinDate;
    
    @TableField("leave_date")
    private LocalDateTime leaveDate;
    
    @TableField("member_status")
    private String memberStatus;
    
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
    
    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_LEFT = "LEFT";
}