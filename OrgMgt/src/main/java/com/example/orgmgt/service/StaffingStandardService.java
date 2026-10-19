package com.example.orgmgt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orgmgt.entity.StaffingStandard;
import java.util.List;

/**
 * Staffing Standard Service Interface
 */
public interface StaffingStandardService extends IService<StaffingStandard> {
    
    /**
     * Query staffing standards by organization ID
     */
    List<StaffingStandard> getByOrgId(Long orgId);
    
    /**
     * Query active staffing standards
     */
    List<StaffingStandard> getActiveStandards();
    
    /**
     * Query staffing standards by position level
     */
    List<StaffingStandard> getByPositionLevel(String positionLevel);
    
    /**
     * Query staffing standards by staffing type
     */
    List<StaffingStandard> getByStaffingType(String staffingType);
    
    /**
     * Create new staffing standard with version management
     */
    boolean createStandard(StaffingStandard standard);
    
    /**
     * Update staffing standard with version archive
     */
    boolean updateStandard(StaffingStandard standard);
    
    /**
     * Deactivate expired flexible staffing standards
     */
    int deactivateExpiredStandards();
    
    /**
     * Get standard quota for position
     */
    Integer getStandardQuota(Long orgId, String positionLevel);
}
