package com.example.orgmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orgmgt.entity.ManagementSpanStandard;
import com.example.orgmgt.mapper.ManagementSpanStandardMapper;
import com.example.orgmgt.service.ManagementSpanStandardService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

/**
 * Management Span Standard Service Implementation
 * Implements Huawei-style management span control
 * 
 * Control Logic:
 * - NORMAL: current within [min, standard] range
 * - WARNING: current within [standard, max] range (approaching limit)
 * - EXCEEDED: current exceeds max (strict mode blocks new additions)
 */
@Service
public class ManagementSpanStandardServiceImpl extends ServiceImpl<ManagementSpanStandardMapper, ManagementSpanStandard> 
    implements ManagementSpanStandardService {
    
    @Override
    public ManagementSpanStandard getByPositionLevel(String positionLevel) {
        return baseMapper.selectByPositionLevel(positionLevel);
    }
    
    @Override
    public List<ManagementSpanStandard> getActiveStandards() {
        return baseMapper.selectActiveStandards();
    }
    
    @Override
    public void initializeDefaultStandards() {
        // Initialize default standards for all five position levels
        createDefaultStandard("L1", "General Manager", 2, 4, 8, 2, 4, 8, null, null, null, true, new BigDecimal("0.8"));
        createDefaultStandard("L2", "Deputy General Manager/Director", 2, 3, 6, 2, 3, 6, null, null, null, true, new BigDecimal("0.8"));
        createDefaultStandard("L3", "Department Manager", 5, 10, 20, null, null, null, 5, 10, 20, true, new BigDecimal("0.8"));
        createDefaultStandard("L4", "Section Chief", 3, 8, 15, null, null, null, 3, 8, 15, true, new BigDecimal("0.8"));
        createDefaultStandard("L5", "Team Leader", 2, 5, 10, null, null, null, 2, 5, 10, true, new BigDecimal("0.8"));
    }
    
    private void createDefaultStandard(String level, String name, Integer minSpan, Integer standardSpan, Integer maxSpan,
                                        Integer minOrg, Integer standardOrg, Integer maxOrg,
                                        Integer minPerson, Integer standardPerson, Integer maxPerson,
                                        Boolean strict, BigDecimal alertThreshold) {
        LambdaQueryWrapper<ManagementSpanStandard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ManagementSpanStandard::getPositionLevel, level);
        if (this.count(wrapper) == 0) {
            ManagementSpanStandard standard = new ManagementSpanStandard();
            standard.setPositionLevel(level);
            standard.setPositionName(name);
            standard.setMinSpan(minSpan);
            standard.setStandardSpan(standardSpan);
            standard.setMaxSpan(maxSpan);
            standard.setMinOrgCount(minOrg);
            standard.setStandardOrgCount(standardOrg);
            standard.setMaxOrgCount(maxOrg);
            standard.setMinPersonCount(minPerson);
            standard.setStandardPersonCount(standardPerson);
            standard.setMaxPersonCount(maxPerson);
            standard.setIsStrict(strict);
            standard.setAlertThreshold(alertThreshold);
            this.save(standard);
        }
    }
    
    @Override
    public String validateSpan(String positionLevel, Integer currentSpan) {
        ManagementSpanStandard standard = getByPositionLevel(positionLevel);
        if (standard == null || currentSpan == null) {
            return "NORMAL";
        }
        
        Integer minSpan = standard.getMinSpan();
        Integer standardSpan = standard.getStandardSpan();
        Integer maxSpan = standard.getMaxSpan();
        
        if (currentSpan < minSpan) {
            return "WARNING"; // Management redundancy warning
        } else if (currentSpan > maxSpan) {
            return "EXCEEDED"; // Management overload - should block
        } else if (currentSpan > standardSpan) {
            return "WARNING"; // Approaching limit
        }
        return "NORMAL";
    }
    
    @Override
    public String validateOrgCount(String positionLevel, Integer orgCount) {
        ManagementSpanStandard standard = getByPositionLevel(positionLevel);
        if (standard == null || orgCount == null) {
            return "NORMAL";
        }
        
        Integer minOrg = standard.getMinOrgCount();
        Integer maxOrg = standard.getMaxOrgCount();
        
        if (minOrg != null && orgCount < minOrg) {
            return "WARNING"; // Organization redundancy warning
        } else if (maxOrg != null && orgCount > maxOrg) {
            return "EXCEEDED"; // Organization overload - should block
        }
        return "NORMAL";
    }
    
    @Override
    public String getSpanStatus(String positionLevel, Integer currentSpan, Integer orgCount) {
        String spanStatus = validateSpan(positionLevel, currentSpan);
        if (!"NORMAL".equals(spanStatus)) {
            return spanStatus;
        }
        return validateOrgCount(positionLevel, orgCount);
    }
    
    @Override
    public boolean isPositionCreationAllowed(String positionLevel, Integer currentSpan) {
        ManagementSpanStandard standard = getByPositionLevel(positionLevel);
        if (standard == null) {
            return true; // No standard defined, allow
        }
        
        if (!standard.getIsStrict()) {
            return true; // Non-strict mode, allow
        }
        
        Integer maxSpan = standard.getMaxSpan();
        if (maxSpan != null && currentSpan != null && currentSpan >= maxSpan) {
            return false; // Strict mode blocks when at maximum
        }
        return true;
    }
}
