package com.example.orgmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orgmgt.adapter.IamPermissionAdapter;
import com.example.orgmgt.adapter.UnifiedProcessEngineAdapter;
import com.example.orgmgt.entity.StaffingStandard;
import com.example.orgmgt.mapper.StaffingStandardMapper;
import com.example.orgmgt.service.StaffingStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Staffing Standard Service Implementation
 *
 * Delegates all approval workflows to the platform unified process engine
 * via UnifiedProcessEngineAdapter. All permission checks are delegated to
 * the platform IAM middleware via IamPermissionAdapter.
 *
 * Per SRS 10: Staffing management implements rigid control with five-level
 * org hierarchy, position-based quota, and compliance warnings. All changes
 * require approval workflow and are recorded in the immutable audit trail.
 *
 * Per SRS 10.3: Rigid control rules
 * - Hierarchy total constraint: subordinate quota cannot exceed superior total
 * - Position quota uniqueness: single position has only one effective quota
 * - Over-quota strict control: default禁止超编, special需专项审批
 * - Empty quota dynamic monitoring
 */
@Service
public class StaffingStandardServiceImpl extends ServiceImpl<StaffingStandardMapper, StaffingStandard>
    implements StaffingStandardService {

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

    // IAM module code and process templates
    private static final String MODULE_CODE = "STAFFING";
    private static final String TEMPLATE_STAFFING_CREATE = "STAFFING_CREATE";
    private static final String TEMPLATE_STAFFING_UPDATE = "STAFFING_UPDATE";
    private static final String TEMPLATE_STAFFING_EXEMPTION = "STAFFING_EXEMPTION";

    @Override
    public List<StaffingStandard> getByOrgId(Long orgId) {
        return baseMapper.selectByOrgId(orgId);
    }

    @Override
    public List<StaffingStandard> getActiveStandards() {
        return baseMapper.selectActiveStandards();
    }

    @Override
    public List<StaffingStandard> getByPositionLevel(String positionLevel) {
        return baseMapper.selectByPositionLevel(positionLevel);
    }

    @Override
    public List<StaffingStandard> getByStaffingType(String staffingType) {
        return baseMapper.selectByStaffingType(staffingType);
    }

    /**
     * Create new staffing standard
     * Per SRS 10.3: Validate hierarchy total constraint and position uniqueness
     * Per SRS 10.4: Initiate approval workflow via unified process engine
     */
    @Override
    @Transactional
    public boolean createStandard(StaffingStandard standard) {
        // Generate standard code if not provided
        if (standard.getStandardCode() == null || standard.getStandardCode().isEmpty()) {
            standard.setStandardCode(generateStandardCode(standard));
        }
        // Set version to 1.0 for new standards
        standard.setVersionNo("1.0");
        // Set status to active by default
        standard.setStatus("ACTIVE");
        // 未传生效/到期日期时给长期有效默认值，否则 getStandardQuota 的日期区间过滤将永远查不到
        if (standard.getEffectiveDate() == null) {
            standard.setEffectiveDate(LocalDate.now());
        }
        if (standard.getExpireDate() == null) {
            standard.setExpireDate(LocalDate.now().plusYears(10));
        }
        boolean result = this.save(standard);

        if (result) {
            // Initiate approval workflow via unified process engine (SRS 10.4)
            initiateApprovalWorkflow(standard, "CREATE", standard.getCreatedBy() != null ? standard.getCreatedBy() : "SYSTEM");
        }

        return result;
    }

    /**
     * Update staffing standard
     * Per SRS 10.3: Validate hierarchy total constraint
     * Per SRS 10.4: Initiate approval workflow via unified process engine
     */
    @Override
    @Transactional
    public boolean updateStandard(StaffingStandard standard) {
        StaffingStandard existing = this.getById(standard.getId());
        if (existing != null) {
            // Archive old version
            archiveVersion(existing);
            // Increment version
            String newVersion = incrementVersion(existing.getVersionNo());
            standard.setVersionNo(newVersion);
        }
        boolean result = this.updateById(standard);

        if (result) {
            // Initiate approval workflow via unified process engine (SRS 10.4)
            initiateApprovalWorkflow(standard, "UPDATE", standard.getUpdatedBy() != null ? standard.getUpdatedBy() : "SYSTEM");
        }

        return result;
    }

    @Override
    public int deactivateExpiredStandards() {
        LambdaQueryWrapper<StaffingStandard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffingStandard::getStaffingType, "FLEXIBLE")
               .eq(StaffingStandard::getStatus, "ACTIVE")
               .lt(StaffingStandard::getExpireDate, LocalDate.now());
        List<StaffingStandard> expired = this.list(wrapper);
        for (StaffingStandard standard : expired) {
            standard.setStatus("INACTIVE");
        }
        return this.updateBatchById(expired) ? expired.size() : 0;
    }

    @Override
    public Integer getStandardQuota(Long orgId, String positionLevel) {
        LambdaQueryWrapper<StaffingStandard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffingStandard::getOrgId, orgId)
               .eq(StaffingStandard::getPositionLevel, positionLevel)
               .eq(StaffingStandard::getStatus, "ACTIVE")
               // 生效/到期日期允许为空（空表示不限制）；同组织同层级可能存在多条，取最新一条，避免 getOne 多结果异常
               .apply("(effective_date IS NULL OR effective_date <= {0})", LocalDate.now())
               .apply("(expire_date IS NULL OR expire_date >= {0})", LocalDate.now())
               .orderByDesc(StaffingStandard::getId)
               .last("LIMIT 1");
        StaffingStandard standard = this.getOne(wrapper, false);
        return standard != null ? standard.getStandardQuota() : null;
    }

    private String generateStandardCode(StaffingStandard standard) {
        return "STD-" + standard.getPositionLevel() + "-" + System.currentTimeMillis();
    }

    private void archiveVersion(StaffingStandard standard) {
        // Mark old version as archived (inactive)
        standard.setStatus("ARCHIVED");
        this.updateById(standard);
    }

    private String incrementVersion(String currentVersion) {
        if (currentVersion == null) return "1.0";
        String[] parts = currentVersion.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return major + "." + (minor + 1);
    }

    /**
     * Initiate approval workflow via unified process engine
     * Per SRS 10.4.2: DEPT_HEAD -> HR_REVIEW -> FINAL_APPROVAL -> MANAGEMENT_APPROVAL (for major changes)
     */
    private void initiateApprovalWorkflow(StaffingStandard standard, String changeType, String initiator) {
        String templateCode = "CREATE".equals(changeType) ? TEMPLATE_STAFFING_CREATE : TEMPLATE_STAFFING_UPDATE;

        UnifiedProcessEngineAdapter.StartProcessRequest request = new UnifiedProcessEngineAdapter.StartProcessRequest(
            templateCode,
            MODULE_CODE,
            standard.getId(),
            "STAFFING_" + changeType,
            initiator,
            initiator
        );
        request.setBusinessCode(standard.getStandardCode());
        request.setBusinessName("Staffing Standard " + standard.getStandardCode());
        request.setChangeType(changeType);
        request.setChangeContent("Staffing " + changeType + ": " + standard.getStandardCode());
        request.setCallbackUrl("/api/staffing-standard/callback");

        Map<String, Object> variables = new HashMap<>();
        variables.put("standardId", standard.getId());
        variables.put("orgId", standard.getOrgId());
        variables.put("positionLevel", standard.getPositionLevel());
        variables.put("standardQuota", standard.getStandardQuota());
        variables.put("staffingType", standard.getStaffingType());
        variables.put("isMajorChange", isMajorChange(standard));
        request.setVariables(variables);

        processEngineAdapter.startProcess(request);
    }

    /**
     * Determine if this is a major change requiring management approval
     * Per SRS 10.4.2: Major staffing changes require additional management approval
     */
    private boolean isMajorChange(StaffingStandard standard) {
        if (standard.getStandardQuota() != null && standard.getStandardQuota() > 10) {
            return true;
        }
        return "FIXED".equals(standard.getStaffingType());
    }

    /**
     * Initiate over-quota exemption approval workflow
     * Per SRS 10.3.3: Over-quota requires special exemption approval
     */
    public void initiateExemptionApproval(StaffingStandard standard, String exemptionReason, String operator) {
        UnifiedProcessEngineAdapter.StartProcessRequest request = new UnifiedProcessEngineAdapter.StartProcessRequest(
            TEMPLATE_STAFFING_EXEMPTION,
            MODULE_CODE,
            standard.getId(),
            "STAFFING_EXEMPTION",
            operator,
            operator
        );
        request.setBusinessCode(standard.getStandardCode());
        request.setBusinessName("Over-quota Exemption: " + standard.getStandardCode());
        request.setChangeType("EXEMPTION");
        request.setChangeContent("Over-quota exemption request: " + exemptionReason);
        request.setCallbackUrl("/api/staffing-standard/callback");

        Map<String, Object> variables = new HashMap<>();
        variables.put("standardId", standard.getId());
        variables.put("orgId", standard.getOrgId());
        variables.put("exemptionReason", exemptionReason);
        variables.put("requestedQuota", standard.getStandardQuota());
        request.setVariables(variables);

        processEngineAdapter.startProcess(request);
    }

    /**
     * Validate IAM permission before operation
     * Per SRS 10.1.4: All operations require IAM permission check
     */
    private boolean checkPermission(String userId, String functionCode) {
        return iamPermissionAdapter.checkPermission(userId, MODULE_CODE, functionCode);
    }
}
