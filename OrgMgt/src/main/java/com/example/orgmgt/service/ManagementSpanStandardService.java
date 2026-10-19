package com.example.orgmgt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orgmgt.entity.ManagementSpanStandard;
import java.util.List;

/**
 * Management Span Standard Service Interface
 * Implements Huawei-style management span control
 */
public interface ManagementSpanStandardService extends IService<ManagementSpanStandard> {
    
    /**
     * Query management span standard by position level
     */
    ManagementSpanStandard getByPositionLevel(String positionLevel);
    
    /**
     * Query all active management span standards
     */
    List<ManagementSpanStandard> getActiveStandards();
    
    /**
     * Initialize default management span standards for all position levels
     */
    void initializeDefaultStandards();
    
    /**
     * Validate span against standard
     * Returns validation result: NORMAL, WARNING, EXCEEDED
     */
    String validateSpan(String positionLevel, Integer currentSpan);
    
    /**
     * Validate organization count against standard
     */
    String validateOrgCount(String positionLevel, Integer orgCount);
    
    /**
     * Get management span status
     */
    String getSpanStatus(String positionLevel, Integer currentSpan, Integer orgCount);
    
    /**
     * Check if position creation is allowed (strict mode)
     */
    boolean isPositionCreationAllowed(String positionLevel, Integer currentSpan);
}
