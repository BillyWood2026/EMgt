package com.example.iam.entity.org;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Unified Organization Entity
 * Hierarchical organization structure
 */
@Data
@TableName("iam_org")
public class IamOrg {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** Organization code (unique) */
    private String orgCode;
    
    /** Organization name */
    private String orgName;
    
    /** Organization name in English */
    private String orgNameEn;
    
    /** Organization short name */
    private String orgShortName;
    
    /** Parent organization ID (null for root) */
    private Long parentId;
    
    /** Organization level (1=集团, 2=事业群, 3=事业部, 4=部门, 5=组) */
    private Integer orgLevel;
    
    /** Organization path (e.g., /1/2/3/) */
    private String orgPath;
    
    /** Hierarchy depth */
    private Integer hierarchyDepth;
    
    /** Sort order within parent */
    private Integer sortOrder;
    
    /** Organization type: GROUP, BUSINESS_GROUP, BUSINESS_UNIT, DEPARTMENT, TEAM */
    private String orgType;
    
    /** Cost center code */
    private String costCenter;
    
    /** Business line code */
    private String businessLineCode;
    
    /** Region code */
    private String regionCode;
    
    /** City code */
    private String cityCode;
    
    /** Address */
    private String address;
    
    /** Post code */
    private String postCode;
    
    /** Contact phone */
    private String contactPhone;
    
    /** Contact email */
    private String contactEmail;
    
    /** Manager employee ID */
    private String managerEmployeeId;
    
    /** HRBP employee ID */
    private String hrbpEmployeeId;
    
    /** Fixed headcount (编制) */
    private Integer fixedHeadcount;
    
    /** Current headcount */
    private Integer currentHeadcount;
    
    /** Status: ACTIVE, INACTIVE, LIQUIDATED */
    private String status;
    
    /** Effective date */
    private LocalDateTime effectiveDate;
    
    /** Expire date */
    private LocalDateTime expireDate;
    
    /** Data permission level inherited from org level */
    private Integer dataPermissionLevel;
    
    /** Allow external access flag */
    private Boolean allowExternalAccess;
    
    /** Audit fields */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    @Version
    private Long version;
    
    @TableLogic
    private Integer deleted;
}
