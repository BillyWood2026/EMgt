package com.example.iam.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * User Credential History
 * Tracks password history for password reuse prevention
 */
@Data
@TableName("iam_user_credential_history")
public class UserCredentialHistory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** User ID */
    private Long userId;
    
    /** Employee ID */
    private String employeeId;
    
    /** Credential type: PASSWORD, SSH_KEY, API_KEY, CERTIFICATE */
    private String credentialType;
    
    /** Credential hash */
    private String credentialHash;
    
    /** Credential salt */
    private String credentialSalt;
    
    /** Issue time */
    private LocalDateTime issueTime;
    
    /** Expire time */
    private LocalDateTime expireTime;
    
    /** Last used time */
    private LocalDateTime lastUsedTime;
    
    /** Status: ACTIVE, REVOKED, EXPIRED */
    private String status;
    
    /** Audit fields */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @Version
    private Long version;
    
    @TableLogic
    private Integer deleted;
}
