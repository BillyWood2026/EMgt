package com.example.orgmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orgmgt.adapter.IamPermissionAdapter;
import com.example.orgmgt.adapter.UnifiedProcessEngineAdapter;
import com.example.orgmgt.entity.OrgPersonalizedQuotaConfig;
import com.example.orgmgt.mapper.OrgPersonalizedQuotaConfigMapper;
import com.example.orgmgt.service.OrgPersonalizedQuotaConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Organization Personalized Quota Config Service Implementation
 *
 * Implements organization-specific quota and span parameter configuration.
 * Delegates all approval workflows to the platform unified process engine
 * via UnifiedProcessEngineAdapter. All permission checks are delegated to
 * the platform IAM middleware via IamPermissionAdapter.
 *
 * Per SRS 6.5: Personalized job position config allows organizations to
 * customize their own job positions within the standard five-level framework.
 * Priority: Personalized config > System default
 *
 * Per SRS 6.5.4: All personalized config operations require approval via
 * the unified process engine: ORG_APPLY -> HR_REVIEW -> LEADER_APPROVAL
 */
@Service
public class OrgPersonalizedQuotaConfigServiceImpl extends ServiceImpl<OrgPersonalizedQuotaConfigMapper, OrgPersonalizedQuotaConfig>
    implements OrgPersonalizedQuotaConfigService {

    /**
     * Unified Process Engine Adapter - delegates approval workflows to platform
     */
    @Autowired
    private UnifiedProcessEngineAdapter processEngineAdapter;

    /**
     * IAM Permission Adapter - delegates permission checks to platform IAM middleware
     */
    @Autowired
    private IamPermissionAdapter iamPermissionAdapter;

    // IAM module code and process template
    private static final String MODULE_CODE = "JOB_POSITION_LEVEL";
    private static final String TEMPLATE_PERSONALIZED_CONFIG = "PERSONALIZED_QUOTA_CONFIG";
    
    @Override
    public OrgPersonalizedQuotaConfig getByOrgId(Long orgId) {
        return baseMapper.selectByOrgId(orgId);
    }
    
    @Override
    public OrgPersonalizedQuotaConfig getByOrgAndLevel(Long orgId, String positionLevel) {
        return baseMapper.selectByOrgAndLevel(orgId, positionLevel);
    }
    
    @Override
    public List<OrgPersonalizedQuotaConfig> getActiveConfigs() {
        return baseMapper.selectActiveConfigs();
    }
    
    @Override
    @Transactional
    public boolean createConfig(OrgPersonalizedQuotaConfig config) {
        if (config.getVersionNo() == null) {
            config.setVersionNo("1.0");
        }
        if (config.getStatus() == null) {
            config.setStatus("DRAFT");
        }
        config.setConfigureTime(LocalDateTime.now());
        return this.save(config);
    }
    
    @Override
    @Transactional
    public boolean updateConfig(OrgPersonalizedQuotaConfig config) {
        OrgPersonalizedQuotaConfig existing = this.getById(config.getId());
        if (existing != null) {
            // Archive old version
            existing.setStatus("ARCHIVED");
            this.updateById(existing);
            // Create new version
            config.setId(null);
            config.setVersionNo(incrementVersion(existing.getVersionNo()));
            config.setConfigureTime(LocalDateTime.now());
        }
        return this.save(config);
    }
    
    /**
     * Submit personalized config for approval via unified process engine
     * Per SRS 6.5.4: ORG_APPLY -> HR_REVIEW -> LEADER_APPROVAL
     */
    @Override
    @Transactional
    public boolean submitForApproval(Long id) {
        OrgPersonalizedQuotaConfig config = this.getById(id);
        if (config == null) return false;
        config.setStatus("PENDING_APPROVAL");
        this.updateById(config);

        // Delegate to unified process engine to start approval workflow
        UnifiedProcessEngineAdapter.StartProcessRequest request = new UnifiedProcessEngineAdapter.StartProcessRequest(
            TEMPLATE_PERSONALIZED_CONFIG,
            MODULE_CODE,
            id,
            "PERSONALIZED_QUOTA_CONFIG",
            config.getCreatedBy() != null ? config.getCreatedBy() : "SYSTEM",
            config.getCreatedBy() != null ? config.getCreatedBy() : "SYSTEM"
        );
        request.setBusinessCode(config.getOrgCode());
        request.setBusinessName("Personalized Quota Config for Org " + config.getOrgId());
        request.setChangeType("PERSONALIZED_CONFIG");
        request.setChangeContent("Personalized quota config for org level " + config.getOrgLevel());
        request.setCallbackUrl("/api/org-personalized-quota/callback");

        Map<String, Object> variables = new HashMap<>();
        variables.put("orgId", config.getOrgId());
        variables.put("orgLevel", config.getOrgLevel());
        variables.put("positionLevel", config.getPositionLevel());
        variables.put("customMaxQuota", config.getCustomMaxQuota());
        request.setVariables(variables);

        processEngineAdapter.startProcess(request);
        return true;
    }

    /**
     * Approve personalized config (called by process engine callback)
     * Per SRS 6.5.4: This method is invoked by the process engine callback
     * after the approval workflow is completed successfully.
     */
    @Override
    @Transactional
    public boolean approveConfig(Long id, String approver) {
        OrgPersonalizedQuotaConfig config = this.getById(id);
        if (config == null) return false;
        config.setStatus("ACTIVE");
        config.setApprovalResult("APPROVED");
        config.setApprover(approver);
        config.setApprovalTime(LocalDateTime.now());
        return this.updateById(config);
    }

    /**
     * Reject personalized config (called by process engine callback)
     * Per SRS 6.5.4: This method is invoked by the process engine callback
     * when the approval workflow is rejected.
     */
    @Override
    @Transactional
    public boolean rejectConfig(Long id, String reason) {
        OrgPersonalizedQuotaConfig config = this.getById(id);
        if (config == null) return false;
        config.setStatus("REJECTED");
        config.setApprovalResult("REJECTED");
        config.setRemark(reason);
        return this.updateById(config);
    }
    
    @Override
    @Transactional
    public boolean toggleConfig(Long id, boolean enabled) {
        OrgPersonalizedQuotaConfig config = this.getById(id);
        if (config == null) return false;
        config.setEnabled(enabled);
        config.setStatus(enabled ? "ACTIVE" : "INACTIVE");
        return this.updateById(config);
    }
    
    @Override
    @Transactional
    public int expireConfigs() {
        LambdaQueryWrapper<OrgPersonalizedQuotaConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgPersonalizedQuotaConfig::getStatus, "ACTIVE")
               .lt(OrgPersonalizedQuotaConfig::getExpireDate, LocalDate.now());
        List<OrgPersonalizedQuotaConfig> expired = this.list(wrapper);
        for (OrgPersonalizedQuotaConfig config : expired) {
            config.setStatus("EXPIRED");
        }
        return this.updateBatchById(expired) ? expired.size() : 0;
    }
    
    @Override
    public OrgPersonalizedQuotaConfig getEffectiveConfig(Long orgId, String positionLevel) {
        LambdaQueryWrapper<OrgPersonalizedQuotaConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgPersonalizedQuotaConfig::getOrgId, orgId)
               .eq(OrgPersonalizedQuotaConfig::getPositionLevel, positionLevel)
               .eq(OrgPersonalizedQuotaConfig::getStatus, "ACTIVE")
               .eq(OrgPersonalizedQuotaConfig::getEnabled, true)
               // 生效/到期日期允许为空（空表示不限制），避免 NULL 比较导致恒查不到
               .apply("(effective_date IS NULL OR effective_date <= {0})", LocalDate.now())
               .apply("(expire_date IS NULL OR expire_date >= {0})", LocalDate.now())
               .last("LIMIT 1");
        return this.getOne(wrapper, false);
    }
    
    private String incrementVersion(String currentVersion) {
        if (currentVersion == null) return "1.0";
        String[] parts = currentVersion.split("\\.");
        try {
            int major = Integer.parseInt(parts[0].trim());
            int minor = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
            return major + "." + (minor + 1);
        } catch (NumberFormatException e) {
            // 非法版本号（历史脏数据）重置为新版本，避免编辑阻断
            return "1.1";
        }
    }
}
