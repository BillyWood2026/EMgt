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
 * Organization Permission Entity
 * Manages role-based access control for organization management module
 */
@Data
@TableName("org_permission")
public class OrgPermission {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("role_code")
    private String roleCode;
    
    @TableField("role_name")
    private String roleName;
    
    @TableField("module_code")
    private String moduleCode;
    
    @TableField("module_name")
    private String moduleName;
    
    @TableField("function_code")
    private String functionCode;
    
    @TableField("function_name")
    private String functionName;
    
    @TableField("has_permission")
    private Boolean hasPermission;
    
    @TableField("permission_type")
    private String permissionType;

    @TableField("scope")
    private String scope;

    @TableField("status")
    private String status;

    @TableField("data_scope")
    private String dataScope;
    
    @TableField("org_level")
    private String orgLevel;
    
    @TableField("description")
    private String description;
    
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
    
    // Data scope constants
    public static final String SCOPE_ALL = "ALL";
    public static final String SCOPE_OWN_CENTER = "OWN_CENTER";
    public static final String SCOPE_OWN_DEPT = "OWN_DEPT";
    public static final String SCOPE_OWN_SECTION = "OWN_SECTION";
    public static final String SCOPE_PERSONAL = "PERSONAL";
}