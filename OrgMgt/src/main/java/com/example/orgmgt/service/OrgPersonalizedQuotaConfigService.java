package com.example.orgmgt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orgmgt.entity.OrgPersonalizedQuotaConfig;
import java.util.List;

/**
 * Organization Personalized Quota Config Service Interface
 */
public interface OrgPersonalizedQuotaConfigService extends IService<OrgPersonalizedQuotaConfig> {
    
    /**
     * Query config by organization ID
     */
    OrgPersonalizedQuotaConfig getByOrgId(Long orgId);
    
    /**
     * Query config by organization and position level
     */
    OrgPersonalizedQuotaConfig getByOrgAndLevel(Long orgId, String positionLevel);
    
    /**
     * Query active configs
     */
    List<OrgPersonalizedQuotaConfig> getActiveConfigs();
    
    /**
     * Create new personalized config
     */
    boolean createConfig(OrgPersonalizedQuotaConfig config);
    
    /**
     * Update personalized config
     */
    boolean updateConfig(OrgPersonalizedQuotaConfig config);
    
    /**
     * Submit for approval
     */
    boolean submitForApproval(Long id);
    
    /**
     * Approve config
     */
    boolean approveConfig(Long id, String approver);
    
    /**
     * Reject config
     */
    boolean rejectConfig(Long id, String reason);
    
    /**
     * Enable/disable config
     */
    boolean toggleConfig(Long id, boolean enabled);
    
    /**
     * Expire outdated configs
     */
    int expireConfigs();
    
    /**
     * Get effective config for position level in organization
     * Returns personalized config if exists, otherwise null
     */
    OrgPersonalizedQuotaConfig getEffectiveConfig(Long orgId, String positionLevel);
}
