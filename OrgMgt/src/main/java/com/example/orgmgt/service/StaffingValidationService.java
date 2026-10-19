package com.example.orgmgt.service;

import com.example.orgmgt.entity.OrgPersonalizedQuotaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for validating staffing management span and calculating warning levels.
 * This service encapsulates the management span validation logic and warning calculations
 * based on position levels and organizational settings.
 * 
 * PLAN-006: Personalized config > System default
 * PLAN-005: Yellow warning when approaching threshold
 */
@Service
public class StaffingValidationService {

    @Autowired
    private OrgPersonalizedQuotaConfigService personalizedQuotaConfigService;

    // Management span parameters by position level
    // Format: positionLevel -> {min, standard, max}
    private static final Map<String, int[]> SPAN_PARAMETERS = new HashMap<>();
    private static final Map<String, String[]> WARN_LEVELS = new HashMap<>();

    static {
        // Initialize span parameters for each level
        // L1 - Executive: min=3, standard=5, max=8
        SPAN_PARAMETERS.put("L1", new int[]{3, 5, 8});
        // L2 - Senior Management: min=5, standard=8, max=12
        SPAN_PARAMETERS.put("L2", new int[]{5, 8, 12});
        // L3 - Middle Management: min=8, standard=10, max=15
        SPAN_PARAMETERS.put("L3", new int[]{8, 10, 15});
        // L4 - Junior Management: min=10, standard=12, max=18
        SPAN_PARAMETERS.put("L4", new int[]{10, 12, 18});
        // L5 - Staff: no span requirements
        SPAN_PARAMETERS.put("L5", new int[]{0, 0, 0});

        // Warning levels: normal range, warning range, critical range (percentage)
        WARN_LEVELS.put("VACANCY", new String[]{"0-20", "21-40", "41-100"});
        WARN_LEVELS.put("EXCEED", new String[]{"0-10", "11-25", "26-100"});
        WARN_LEVELS.put("SPAN", new String[]{"80-120", "60-79,121-140", "0-59,141-100"});
    }

    /**
     * Validates if the current count is within the allowed management quota for a given position level.
     *
     * @param orgId         The organization ID
     * @param positionLevel The position level (L1-L5)
     * @param currentCount  The current number of subordinates
     * @return true if the count is within quota, false otherwise
     */
    public boolean validateManagementQuota(Long orgId, String positionLevel, int currentCount) {
        int[] params = SPAN_PARAMETERS.getOrDefault(positionLevel, new int[]{0, 0, 0});
        int minSpan = params[0];
        int maxSpan = params[2];

        // For staff level (L5), no span requirements
        if (minSpan == 0 && maxSpan == 0) {
            return true;
        }

        return currentCount >= minSpan && currentCount <= maxSpan;
    }

    /**
     * Calculates the warning level based on the current subordinate count.
     * Uses three-tier validation: minimum, standard, maximum.
     *
     * @param orgId          The organization ID
     * @param positionLevel  The position level (L1-L5)
     * @param subordinateCount The current number of subordinates
     * @return warning level: "NORMAL", "WARNING", or "CRITICAL"
     */
    public String calculateWarningLevel(Long orgId, String positionLevel, int subordinateCount) {
        int[] params = SPAN_PARAMETERS.getOrDefault(positionLevel, new int[]{0, 0, 0});
        int minSpan = params[0];
        int standardSpan = params[1];
        int maxSpan = params[2];

        // For staff level (L5), always normal
        if (standardSpan == 0) {
            return "NORMAL";
        }

        // Calculate deviation percentage from standard
        double deviation = ((double) subordinateCount - standardSpan) / standardSpan * 100;

        // Three-color warning system:
        // Green (NORMAL): within ±20% of standard
        // Yellow (WARNING): between ±21% and ±40%
        // Red (CRITICAL): beyond ±40% or below minimum
        if (subordinateCount < minSpan) {
            return "CRITICAL";
        } else if (subordinateCount > maxSpan) {
            return "CRITICAL";
        } else if (Math.abs(deviation) <= 20) {
            return "NORMAL";
        } else if (Math.abs(deviation) <= 40) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }

    /**
     * PLAN-006: Gets the amplitude parameters (min, standard, max) for a given position level.
     * Priority: Personalized config > System default
     *
     * @param orgId         The organization ID
     * @param positionLevel The position level (L1-L5)
     * @return Map containing "min", "standard", and "max" parameters
     */
    public Map<String, Integer> getAmplitudeParameters(Long orgId, String positionLevel) {
        Map<String, Integer> result = new HashMap<>();
        
        // PLAN-006: Check for personalized config first
        OrgPersonalizedQuotaConfig personalizedConfig = 
            personalizedQuotaConfigService.getByOrgAndLevel(orgId, positionLevel);
        
        if (personalizedConfig != null && 
            "ACTIVE".equals(personalizedConfig.getStatus()) &&
            personalizedConfig.getEnabled()) {
            // Use personalized parameters
            result.put("min", personalizedConfig.getCustomMinAmplitude());
            result.put("standard", personalizedConfig.getCustomStdAmplitude());
            result.put("max", personalizedConfig.getCustomMaxAmplitude());
            result.put("personalized", 1);
            return result;
        }
        
        // Fall back to system defaults
        int[] params = SPAN_PARAMETERS.getOrDefault(positionLevel, new int[]{0, 0, 0});
        result.put("min", params[0]);
        result.put("standard", params[1]);
        result.put("max", params[2]);
        result.put("personalized", 0);
        
        return result;
    }

    /**
     * Validates staffing quota against standard.
     *
     * @param standardQuota The standard quota for the position
     * @param currentCount  The current filled count
     * @return warning level: "NORMAL", "WARNING", or "CRITICAL"
     */
    public String validateStaffingQuota(int standardQuota, int currentCount) {
        if (standardQuota <= 0) {
            return "NORMAL";
        }

        double ratio = (double) (standardQuota - currentCount) / standardQuota * 100;

        // Vacancy warning thresholds
        if (ratio <= 20) {
            return "NORMAL";
        } else if (ratio <= 40) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }

    /**
     * Validates if the staffing exceeds the standard quota.
     *
     * @param standardQuota The standard quota for the position
     * @param currentCount  The current filled count
     * @return warning level: "NORMAL", "WARNING", or "CRITICAL"
     */
    public String validateExceedQuota(int standardQuota, int currentCount) {
        if (standardQuota <= 0) {
            return "NORMAL";
        }

        double exceedRatio = (double) (currentCount - standardQuota) / standardQuota * 100;

        if (exceedRatio <= 0) {
            return "NORMAL";
        } else if (exceedRatio <= 10) {
            return "NORMAL";
        } else if (exceedRatio <= 25) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }

    /**
     * Calculates overall staffing health score for an organization.
     *
     * @param orgId The organization ID
     * @return health score between 0-100
     */
    public int calculateStaffingHealthScore(Long orgId) {
        // This would typically query the database for actual staffing data
        // For demonstration, return a default score
        return 85;
    }

    /**
     * Gets warning level configuration for a specific anomaly type.
     *
     * @param anomalyType The type of anomaly (VACANCY, EXCEED, SPAN)
     * @return array containing [normal range, warning range, critical range]
     */
    public String[] getWarningLevelConfig(String anomalyType) {
        return WARN_LEVELS.getOrDefault(anomalyType, new String[]{"", "", ""});
    }
}