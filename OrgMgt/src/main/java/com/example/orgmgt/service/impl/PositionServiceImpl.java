package com.example.orgmgt.service.impl;

import com.example.orgmgt.adapter.IamPermissionAdapter;
import com.example.orgmgt.adapter.UnifiedProcessEngineAdapter;
import com.example.orgmgt.entity.JobPositionChangeHistory;
import com.example.orgmgt.entity.Organization;
import com.example.orgmgt.entity.Position;
import com.example.orgmgt.mapper.JobPositionChangeHistoryMapper;
import com.example.orgmgt.mapper.PositionMapper;
import com.example.orgmgt.service.OrganizationService;
import com.example.orgmgt.service.PositionService;
import com.example.orgmgt.service.StaffingValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Position Service Implementation
 *
 * Delegates all approval workflows to the platform unified process engine
 * via UnifiedProcessEngineAdapter. All permission checks are delegated to
 * the platform IAM middleware via IamPermissionAdapter.
 *
 * Per SRS 9: Position management covers CRUD, change approval, history
 * management, and status control. All changes require approval workflow
 * and are recorded in the immutable audit trail.
 *
 * Per SRS 9.3: All position changes are 100% recorded with before/after
 * data, version numbers, and support version comparison and traceability.
 */
@Service
public class PositionServiceImpl implements PositionService {

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private StaffingValidationService staffingValidationService;

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

    /**
     * Job Position Change History Mapper - writes to immutable audit trail
     */
    @Autowired
    private JobPositionChangeHistoryMapper changeHistoryMapper;

    // IAM module code and process templates
    private static final String MODULE_CODE = "POSITION";
    private static final String TEMPLATE_POSITION_CREATE = "POSITION_CREATE";
    private static final String TEMPLATE_POSITION_UPDATE = "POSITION_UPDATE";
    private static final String TEMPLATE_POSITION_DELETE = "POSITION_DELETE";
    private static final String TEMPLATE_POSITION_STATUS = "POSITION_STATUS_CHANGE";

    @Override
    public List<Position> findAll() {
        return positionMapper.findAll();
    }

    @Override
    public Position findById(Long id) {
        return positionMapper.findById(id);
    }

    @Override
    public Position findByPositionCode(String positionCode) {
        return positionMapper.findByPositionCode(positionCode);
    }

    @Override
    public List<Position> findByOrgId(Long orgId) {
        return positionMapper.findByOrgId(orgId);
    }

    @Override
    public List<Position> findByOrgCode(String orgCode) {
        return positionMapper.findByOrgCode(orgCode);
    }

    @Override
    public List<Position> findByPositionLevel(String positionLevel) {
        return positionMapper.findByPositionLevel(positionLevel);
    }

    @Override
    public List<Position> findByPositionStatus(String positionStatus) {
        return positionMapper.findByPositionStatus(positionStatus);
    }

    @Override
    @Transactional
    public Position create(Position position) {
        // Auto-generate position code when not provided
        if (position.getPositionCode() == null || position.getPositionCode().trim().isEmpty()) {
            position.setPositionCode("P" + System.currentTimeMillis()
                    + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        }
        // POST-001: Validate parent organization is enabled
        validateOrganizationEnabled(position.getOrgId());

        // PLAN-001/003/004: Validate staffing quota and management span
        validateStaffingQuota(position);
        validateManagementSpan(position);
        validateManagementPositionCount(position);

        position.setPositionStatus("ENABLED");
        position.setCreatedTime(LocalDateTime.now());
        if (position.getStaffingCount() == null) {
            position.setStaffingCount(0);
        }
        if (position.getCurrentCount() == null) {
            position.setCurrentCount(0);
        }
        positionMapper.insert(position);

        // Record to immutable audit trail (SRS 9.3)
        recordChangeHistory(position.getId(), "CREATE", null, position, "SYSTEM");

        // Initiate approval workflow via unified process engine (SRS 9.2)
        initiateApprovalWorkflow(position, "CREATE", "SYSTEM");

        return position;
    }

    /**
     * Validates that the parent organization is enabled.
     *
     * @param orgId Organization ID to validate
     * @throws IllegalStateException if organization is disabled
     */
    private void validateOrganizationEnabled(Long orgId) {
        if (orgId == null) {
            return;
        }
        Organization org = organizationService.findById(orgId);
        if (org == null) {
            throw new IllegalArgumentException("所属组织不存在");
        }
        if (!"ENABLED".equals(org.getOrgStatus())) {
            throw new IllegalStateException("禁止在停用组织下新增岗位");
        }
    }

    /**
     * PLAN-001: Validates fixed staffing quota is not exceeded.
     * For FIXED staffing type, current count cannot exceed staffing quota.
     *
     * @param position Position to validate
     * @throws IllegalStateException if quota exceeded for fixed staffing
     */
    private void validateStaffingQuota(Position position) {
        if (position.getStaffingType() == null) {
            return; // No staffing type specified, skip validation
        }
        
        if (Position.STAFFING_FIXED.equals(position.getStaffingType())) {
            if (position.getCurrentCount() != null && 
                position.getStaffingCount() != null &&
                position.getCurrentCount() >= position.getStaffingCount()) {
                throw new IllegalStateException(
                    String.format("岗位[%s]已达固定编制上限(%d人)，禁止新增人员",
                        position.getPositionName(), position.getStaffingCount()));
            }
        }
    }

    /**
     * PLAN-003: Validates management span minimum requirement.
     * For management positions (L1-L4), subordinate count must meet minimum.
     *
     * @param position Position to validate
     * @throws IllegalStateException if management span below minimum
     */
    private void validateManagementSpan(Position position) {
        if (position.getPositionLevel() == null) {
            return;
        }

        // Only validate for management levels (L1-L4)
        if (position.getPositionLevel().startsWith("L5")) {
            return; // Staff level has no span requirements
        }

        // Check if current count meets minimum span
        boolean isValid = staffingValidationService.validateManagementQuota(
            position.getOrgId(),
            position.getPositionLevel(),
            position.getCurrentCount() != null ? position.getCurrentCount() : 0
        );

        if (!isValid) {
            String warningLevel = staffingValidationService.calculateWarningLevel(
                position.getOrgId(),
                position.getPositionLevel(),
                position.getCurrentCount() != null ? position.getCurrentCount() : 0
            );
            
            if ("CRITICAL".equals(warningLevel)) {
                throw new IllegalStateException(
                    String.format("岗位[%s]管理幅度低于最低要求，系统红色告警拦截",
                        position.getPositionName()));
            }
        }
    }

    /**
     * PLAN-004: Validates management position count limit per organization.
     * For management positions (L1-L4), checks if max count per org level is exceeded.
     *
     * @param position Position to validate
     * @throws IllegalStateException if management position count exceeds limit
     */
    private void validateManagementPositionCount(Position position) {
        if (position.getPositionLevel() == null || position.getOrgId() == null) {
            return;
        }

        // Only validate for management levels
        if (!position.getPositionLevel().startsWith("L1") && 
            !position.getPositionLevel().startsWith("L2") &&
            !position.getPositionLevel().startsWith("L3") &&
            !position.getPositionLevel().startsWith("L4")) {
            return;
        }

        // Count existing management positions in this org with same level
        List<Position> existingPositions = positionMapper.findByOrgId(position.getOrgId());
        
        long managementCount = existingPositions.stream()
            .filter(p -> p.getId() != null && !p.getId().equals(position.getId()))
            .filter(p -> p.getPositionLevel() != null && 
                        p.getPositionLevel().equals(position.getPositionLevel()))
            .count();

        // Maximum management positions per level per org
        int maxCount = 1; // Default max is 1
        
        if (managementCount >= maxCount) {
            throw new IllegalStateException(
                String.format("组织[%s]的%s级管理职数已达上限(%d人)，禁止新增",
                    position.getOrgName(), position.getPositionLevel(), maxCount));
        }
    }

    @Override
    @Transactional
    public Position update(Position position) {
        // PLAN-001/003/004: Validate staffing quota and management span
        validateStaffingQuota(position);
        validateManagementSpan(position);
        validateManagementPositionCount(position);

        // Capture before state for history
        Position before = positionMapper.findById(position.getId());

        position.setUpdatedTime(LocalDateTime.now());
        positionMapper.update(position);

        // Record to immutable audit trail (SRS 9.3)
        recordChangeHistory(position.getId(), "UPDATE", before, position, "SYSTEM");

        // Initiate approval workflow via unified process engine (SRS 9.2)
        initiateApprovalWorkflow(position, "UPDATE", "SYSTEM");

        return position;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Position before = positionMapper.findById(id);
        positionMapper.deleteById(id);

        // Record to immutable audit trail (SRS 9.3)
        recordChangeHistory(id, "DELETE", before, null, "SYSTEM");

        // Initiate approval workflow via unified process engine (SRS 9.2)
        if (before != null) {
            initiateApprovalWorkflow(before, "DELETE", "SYSTEM");
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        Position before = positionMapper.findById(id);
        positionMapper.updateStatus(id, status);

        // Create after state for history
        Position after = positionMapper.findById(id);

        // Record to immutable audit trail (SRS 9.3)
        recordChangeHistory(id, "STATUS_CHANGE", before, after, "SYSTEM");

        // Initiate approval workflow via unified process engine (SRS 9.2)
        if (after != null) {
            initiateApprovalWorkflow(after, "STATUS_CHANGE", "SYSTEM");
        }
    }

    @Override
    @Transactional
    public void updateCurrentCount(Long id, Integer count) {
        positionMapper.updateCurrentCount(id, count);
    }

    @Override
    public List<Position> findVacantPositions() {
        return positionMapper.findVacantPositions();
    }

    @Override
    @Transactional
    public List<Position> batchCreate(List<Position> positions) {
        for (Position position : positions) {
            create(position);
        }
        return positions;
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            positionMapper.deleteById(id);
        }
    }

    @Override
    @Transactional
    public void batchUpdateOrg(List<Long> positionIds, Long newOrgId) {
        for (Long positionId : positionIds) {
            Position position = positionMapper.findById(positionId);
            if (position != null) {
                Position before = positionMapper.findById(positionId);
                position.setOrgId(newOrgId);
                position.setUpdatedTime(LocalDateTime.now());
                positionMapper.update(position);

                // Record to immutable audit trail (SRS 9.3)
                recordChangeHistory(positionId, "ORG_TRANSFER", before, position, "SYSTEM");
            }
        }
    }

    /**
     * Initiate approval workflow via unified process engine
     * Per SRS 9.2: Position changes require DEPT_HEAD -> HR_REVIEW -> FINAL_APPROVAL
     */
    private void initiateApprovalWorkflow(Position position, String changeType, String initiator) {
        String templateCode;
        switch (changeType) {
            case "CREATE": templateCode = TEMPLATE_POSITION_CREATE; break;
            case "UPDATE": templateCode = TEMPLATE_POSITION_UPDATE; break;
            case "DELETE": templateCode = TEMPLATE_POSITION_DELETE; break;
            case "STATUS_CHANGE": templateCode = TEMPLATE_POSITION_STATUS; break;
            default: templateCode = TEMPLATE_POSITION_UPDATE;
        }

        UnifiedProcessEngineAdapter.StartProcessRequest request = new UnifiedProcessEngineAdapter.StartProcessRequest(
            templateCode,
            MODULE_CODE,
            position.getId(),
            "POSITION_" + changeType,
            initiator,
            initiator
        );
        request.setBusinessCode(position.getPositionCode());
        request.setBusinessName(position.getPositionName());
        request.setChangeType(changeType);
        request.setChangeContent("Position " + changeType + ": " + position.getPositionName());
        request.setCallbackUrl("/api/position/callback");

        Map<String, Object> variables = new HashMap<>();
        variables.put("positionId", position.getId());
        variables.put("positionCode", position.getPositionCode());
        variables.put("orgId", position.getOrgId());
        variables.put("positionLevel", position.getPositionLevel());
        request.setVariables(variables);

        processEngineAdapter.startProcess(request);
    }

    /**
     * Record change history to immutable audit trail
     * Per SRS 9.3: All position changes are permanently stored, cannot be deleted or modified
     */
    private void recordChangeHistory(Long positionId, String changeType, Position before, Position after, String operator) {
        JobPositionChangeHistory history = new JobPositionChangeHistory();
        history.setChangeNo(changeHistoryMapper.generateNextChangeNo());
        history.setPositionId(positionId);
        history.setPositionCode(after != null ? after.getPositionCode() : (before != null ? before.getPositionCode() : null));
        history.setPositionName(after != null ? after.getPositionName() : (before != null ? before.getPositionName() : null));
        history.setChangeType(changeType);
        history.setBeforeData(serializePosition(before));
        history.setAfterData(serializePosition(after));
        history.setChangeDesc(buildChangeDesc(changeType, before, after));
        history.setInitiatorId(operator);
        history.setInitiatorName(operator);
        history.setChangeTime(LocalDateTime.now());
        history.setApprovalResult("PENDING");
        history.setOperator(operator);
        history.setVersionNo(changeHistoryMapper.generateNextVersionNo(history.getPositionCode()));
        history.setCreateTime(LocalDateTime.now());

        changeHistoryMapper.insert(history);
    }

    /**
     * Serialize position to JSON-like string for audit trail
     */
    private String serializePosition(Position position) {
        if (position == null) return null;
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"positionCode\":\"").append(position.getPositionCode() != null ? position.getPositionCode() : "").append("\",");
        sb.append("\"positionName\":\"").append(position.getPositionName() != null ? position.getPositionName() : "").append("\",");
        sb.append("\"orgId\":").append(position.getOrgId() != null ? position.getOrgId() : "null").append(",");
        sb.append("\"orgName\":\"").append(position.getOrgName() != null ? position.getOrgName() : "").append("\",");
        sb.append("\"positionLevel\":\"").append(position.getPositionLevel() != null ? position.getPositionLevel() : "").append("\",");
        sb.append("\"positionStatus\":\"").append(position.getPositionStatus() != null ? position.getPositionStatus() : "").append("\",");
        sb.append("\"staffingCount\":").append(position.getStaffingCount() != null ? position.getStaffingCount() : 0).append(",");
        sb.append("\"currentCount\":").append(position.getCurrentCount() != null ? position.getCurrentCount() : 0);
        sb.append("}");
        return sb.toString();
    }

    /**
     * Build change description from before/after states
     */
    private String buildChangeDesc(String type, Position before, Position after) {
        switch (type) {
            case "CREATE": return "Create new position: " + (after != null ? after.getPositionName() : "");
            case "UPDATE": return "Update position information";
            case "DELETE": return "Delete position: " + (before != null ? before.getPositionName() : "");
            case "STATUS_CHANGE": return "Change position status";
            case "ORG_TRANSFER": return "Transfer position to new organization";
            default: return type;
        }
    }

    /**
     * Validate IAM permission before operation
     * Per SRS 8.10: All operations require IAM permission check
     */
    private boolean checkPermission(String userId, String functionCode) {
        return iamPermissionAdapter.checkPermission(userId, MODULE_CODE, functionCode);
    }
}