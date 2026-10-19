package com.example.iam.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Unified User Entity
 * Global unique user identity across all modules
 */
@Data
@TableName("iam_user")
public class IamUser {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** Global unique employee ID (primary key from StaffMgt) */
    private String employeeId;
    
    /** Username for login */
    private String username;
    
    /** Password hash (BCrypt) */
    private String passwordHash;
    
    /** Real name */
    private String realName;
    
    /** Email */
    private String email;
    
    /** Mobile phone */
    private String mobile;
    
    /** ID card number (encrypted) */
    private String idCardNo;
    
    /** User status: ACTIVE, INACTIVE, LOCKED, DELETED */
    private String status;
    
    /** Password expired time */
    private LocalDateTime passwordExpiredTime;
    
    /** Last login time */
    private LocalDateTime lastLoginTime;
    
    /** Last login IP */
    private String lastLoginIp;
    
    /** Failed login attempts */
    private Integer failedAttempts;
    
    /** Lock expired time */
    private LocalDateTime lockExpiredTime;
    
    /** Multi-factor auth enabled */
    private Boolean mfaEnabled;
    
    /** User type: EMPLOYEE, EXTERNAL, SERVICE_ACCOUNT */
    private String userType;
    
    /** Default role ID */
    private Long defaultRoleId;
    
    /** Default organization ID */
    private Long defaultOrgId;
    
    /** Photo URL */
    private String photoUrl;
    
    /** Signature URL */
    private String signatureUrl;
    
    /** Employee status: ON_JOB, LEAVE, RESIGNED, RETIRED */
    private String employeeStatus;
    
    /** Entry date */
    private LocalDateTime entryDate;
    
    /** Probation end date */
    private LocalDateTime probationEndDate;
    
    /** Data permission level: 1-10 (higher = more sensitive) */
    private Integer dataPermissionLevel;
    
    /** Cost center code */
    private String costCenter;
    
    /** Work location */
    private String workLocation;
    
    /** Manager employee ID */
    private String managerEmployeeId;
    
    /** Super manager flag */
    private Boolean isSuperManager;
    
    /** System admin flag */
    private Boolean isSystemAdmin;
    
    /** Audit fields */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    /** Version for optimistic locking */
    @Version
    private Long version;
    
    /** Deleted flag */
    @TableLogic
    private Integer deleted;
}
