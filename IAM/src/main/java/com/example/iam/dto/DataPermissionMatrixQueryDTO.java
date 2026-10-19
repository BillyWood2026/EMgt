package com.example.iam.dto;

import lombok.Data;
import java.util.List;

/**
 * Data Permission Matrix Query DTO
 */
@Data
public class DataPermissionMatrixQueryDTO {
    
    /** Module code */
    private String moduleCode;
    
    /** Sub-module code */
    private String subModuleCode;
    
    /** Role ID */
    private Long roleId;
    
    /** Role code */
    private String roleCode;
    
    /** Organization ID */
    private Long orgId;
    
    /** Entity name (table/object) */
    private String entityName;
    
    /** User ID (to get effective matrix) */
    private Long userId;
    
    /** Employee ID (alternative) */
    private String employeeId;
}
