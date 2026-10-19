package com.example.orgmgt.service.impl;

import com.example.orgmgt.entity.PositionHierarchy;
import com.example.orgmgt.entity.PositionHierarchyHistory;
import com.example.orgmgt.mapper.PositionHierarchyHistoryMapper;
import com.example.orgmgt.mapper.PositionHierarchyMapper;
import com.example.orgmgt.service.PositionHierarchyService;
import com.example.orgmgt.service.OrgPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PositionHierarchyServiceImpl implements PositionHierarchyService {

    @Autowired
    private PositionHierarchyMapper hierarchyMapper;

    @Autowired
    private PositionHierarchyHistoryMapper historyMapper;

    @Autowired
    private OrgPermissionService orgPermissionService;

    /**
     * Principal position titles that must be unique per organization
     * Only one principal can hold each title per org unit
     */
    private static final String[] PRINCIPAL_TITLES = {
        "总经理", "部门经理", "主管", "领班"
    };

    /**
     * DUTY-004: High-level executive positions that require special permissions
     * Only SUPER_ADMIN can edit/delete these positions
     */
    private static final String[] EXECUTIVE_TITLES = {
        "总经理", "副总经理", "董事长", "副董事长", "总裁", "副总裁"
    };

    /**
     * Mapping of organization level to expected position grade
     * org_level 1 (Company) -> L1
     * org_level 2 (Center) -> L2
     * org_level 3 (Department) -> L3
     * org_level 4 (Section) -> L4
     * org_level 5 (Group) -> L5
     */
    private static final java.util.Map<Integer, String> ORG_LEVEL_TO_GRADE = new java.util.HashMap<>();
    static {
        ORG_LEVEL_TO_GRADE.put(1, "L1");
        ORG_LEVEL_TO_GRADE.put(2, "L2");
        ORG_LEVEL_TO_GRADE.put(3, "L3");
        ORG_LEVEL_TO_GRADE.put(4, "L4");
        ORG_LEVEL_TO_GRADE.put(5, "L5");
    }

    /**
     * DUTY-004: Validates that user has permission to edit executive positions.
     * Executive positions (总经理, 副总经理, etc.) can only be edited by SUPER_ADMIN.
     *
     * @param hierarchy Position hierarchy to validate
     * @param userId User attempting the operation
     * @throws IllegalStateException if user lacks permission
     */
    private void validateExecutivePositionPermission(PositionHierarchy hierarchy, String userId) {
        String positionName = hierarchy.getHierarchyName();
        
        // Check if this is an executive position
        if (isExecutivePosition(positionName)) {
            // Check if user has SUPER_ADMIN role
            boolean hasPermission = orgPermissionService.hasPermission(userId, "PERMISSION", "MANAGE");
            
            if (!hasPermission) {
                throw new IllegalStateException(
                    String.format("禁止操作高管职务。职务[%s]为核心高管职务，仅超级管理员可操作", positionName));
            }
        }
    }

    /**
     * Checks if a position title is an executive position
     */
    private boolean isExecutivePosition(String positionName) {
        if (positionName == null) {
            return false;
        }
        
        for (String executiveTitle : EXECUTIVE_TITLES) {
            if (positionName.contains(executiveTitle)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates that position grade matches organization level.
     * Company (L1) -> L1 positions only, etc.
     *
     * @param hierarchy Position hierarchy to validate
     * @throws IllegalArgumentException if grade doesn't match org level
     */
    private void validateGradeOrgLevelBinding(PositionHierarchy hierarchy) {
        if (hierarchy.getOrgLevel() == null || hierarchy.getHierarchyGrade() == null) {
            return;
        }

        String expectedGrade = ORG_LEVEL_TO_GRADE.get(hierarchy.getOrgLevel());
        if (expectedGrade == null) {
            return;
        }

        // Extract grade prefix (e.g., "L1" from "L1-M")
        String gradePrefix = hierarchy.getHierarchyGrade().substring(0, Math.min(2, hierarchy.getHierarchyGrade().length()));
        
        if (!expectedGrade.equals(gradePrefix)) {
            throw new IllegalArgumentException(
                String.format("职务层级与组织层级不匹配。%d级组织仅能配置%s级别职务，实际为%s",
                    hierarchy.getOrgLevel(), expectedGrade, gradePrefix));
        }
    }

    /**
     * Checks if a position title is a principal (正职) type.
     *
     * @param title Position title
     * @return true if principal type
     */
    private boolean isPrincipalTitle(String title) {
        if (title == null) return false;
        for (String principal : PRINCIPAL_TITLES) {
            if (title.contains(principal)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates that no duplicate principal position exists in the same organization.
     *
     * @param hierarchy Position hierarchy to validate
     * @throws IllegalStateException if duplicate principal found
     */
    private void validatePrincipalUniqueness(PositionHierarchy hierarchy) {
        if (!isPrincipalTitle(hierarchy.getHierarchyName())) {
            return; // Not a principal position, skip validation
        }

        // Find existing principal positions in the same organization
        List<PositionHierarchy> existingPositions = hierarchyMapper.findByOrgId(hierarchy.getOrgId());
        
        for (PositionHierarchy existing : existingPositions) {
            if (existing.getId().equals(hierarchy.getId())) {
                continue; // Skip self (update scenario)
            }
            if (isPrincipalTitle(existing.getHierarchyName()) && 
                existing.getHierarchyName().equals(hierarchy.getHierarchyName())) {
                throw new IllegalStateException(
                    String.format("同一组织禁止配置多个相同正职岗位。组织%s已存在%s职务",
                        hierarchy.getOrgName(), hierarchy.getHierarchyName()));
            }
        }
    }

    @Override
    public List<PositionHierarchy> findAll() {
        return hierarchyMapper.findAll();
    }

    @Override
    public PositionHierarchy findById(Long id) {
        return hierarchyMapper.findById(id);
    }

    @Override
    public PositionHierarchy findByHierarchyCode(String hierarchyCode) {
        return hierarchyMapper.findByHierarchyCode(hierarchyCode);
    }

    @Override
    public List<PositionHierarchy> findByOrgLevel(Integer orgLevel) {
        return hierarchyMapper.findByOrgLevel(orgLevel);
    }

    @Override
    public List<PositionHierarchy> findByOrgId(Long orgId) {
        return hierarchyMapper.findByOrgId(orgId);
    }

    @Override
    public List<PositionHierarchy> findByOrgCode(String orgCode) {
        return hierarchyMapper.findByOrgCode(orgCode);
    }

    @Override
    @Transactional
    public PositionHierarchy create(PositionHierarchy hierarchy) {
        // Auto-generate hierarchy code when not provided
        if (hierarchy.getHierarchyCode() == null || hierarchy.getHierarchyCode().trim().isEmpty()) {
            hierarchy.setHierarchyCode("H" + System.currentTimeMillis()
                    + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        }
        // org_level is NOT NULL in DB
        if (hierarchy.getOrgLevel() == null) {
            hierarchy.setOrgLevel(1);
        }
        // DUTY-004: Validate executive position permission
        // Note: In production, userId should come from security context
        validateExecutivePositionPermission(hierarchy, "SYSTEM");
        
        // DUTY-001: Validate position grade matches organization level
        validateGradeOrgLevelBinding(hierarchy);
        
        // DUTY-002: Validate principal position uniqueness
        validatePrincipalUniqueness(hierarchy);
        
        hierarchy.setHierarchyStatus("ENABLED");
        hierarchy.setCreatedTime(LocalDateTime.now());
        // 逻辑删除/乐观锁字段必须落库默认值，否则 findAll/findById 的 deleted=0 过滤会丢失该行
        if (hierarchy.getDeleted() == null) {
            hierarchy.setDeleted(0);
        }
        if (hierarchy.getVersion() == null) {
            hierarchy.setVersion(1);
        }
        hierarchyMapper.insert(hierarchy);
        
        createChangeRecord(hierarchy.getHierarchyCode(), "CREATE", "", 
            hierarchy.getHierarchyName() + "," + hierarchy.getHierarchyGrade());
        
        return hierarchy;
    }

    @Override
    @Transactional
    public PositionHierarchy update(PositionHierarchy hierarchy) {
        // DUTY-004: Validate executive position permission
        // Note: In production, userId should come from security context
        validateExecutivePositionPermission(hierarchy, "SYSTEM");
        
        // DUTY-001: Validate position grade matches organization level
        validateGradeOrgLevelBinding(hierarchy);
        
        // DUTY-002: Validate principal position uniqueness
        validatePrincipalUniqueness(hierarchy);
        
        PositionHierarchy old = hierarchyMapper.findById(hierarchy.getId());
        String beforeContent = old.getHierarchyName() + "," + old.getHierarchyGrade();
        String afterContent = hierarchy.getHierarchyName() + "," + hierarchy.getHierarchyGrade();

        hierarchy.setUpdatedTime(LocalDateTime.now());
        // 自定义 update 为全列 SET，未传字段沿用旧值，避免 version 等被置空
        if (hierarchy.getVersion() == null) {
            hierarchy.setVersion(old.getVersion());
        }
        hierarchyMapper.update(hierarchy);
        
        createChangeRecord(hierarchy.getHierarchyCode(), "UPDATE", beforeContent, afterContent);
        
        return hierarchy;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        PositionHierarchy hierarchy = hierarchyMapper.findById(id);
        if (hierarchy != null) {
            createChangeRecord(hierarchy.getHierarchyCode(), "DELETE", 
                hierarchy.getHierarchyName(), "");
        }
        hierarchyMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        hierarchyMapper.updateStatus(id, status);
    }

    @Override
    @Transactional
    public void appoint(Long hierarchyId, String incumbents) {
        PositionHierarchy hierarchy = hierarchyMapper.findById(hierarchyId);
        if (hierarchy != null) {
            String oldIncumbents = hierarchy.getIncumbents();
            hierarchy.setIncumbents(incumbents);
            hierarchy.setIsSeconded(0);
            hierarchy.setIsConcurrent(0);
            hierarchy.setUpdatedTime(LocalDateTime.now());
            hierarchyMapper.update(hierarchy);
            
            createChangeRecord(hierarchy.getHierarchyCode(), "APPOINT", oldIncumbents, incumbents);
        }
    }

    @Override
    @Transactional
    public void concurrentAppoint(Long hierarchyId, String incumbents) {
        PositionHierarchy hierarchy = hierarchyMapper.findById(hierarchyId);
        if (hierarchy != null) {
            String oldIncumbents = hierarchy.getIncumbents();
            hierarchy.setIncumbents(incumbents);
            hierarchy.setIsConcurrent(1);
            hierarchy.setIsSeconded(0);
            hierarchy.setUpdatedTime(LocalDateTime.now());
            hierarchyMapper.update(hierarchy);
            
            createChangeRecord(hierarchy.getHierarchyCode(), "CONCURRENT", oldIncumbents, incumbents);
        }
    }

    @Override
    @Transactional
    public void secondedAppoint(Long hierarchyId, String incumbents) {
        PositionHierarchy hierarchy = hierarchyMapper.findById(hierarchyId);
        if (hierarchy != null) {
            String oldIncumbents = hierarchy.getIncumbents();
            hierarchy.setIncumbents(incumbents);
            hierarchy.setIsSeconded(1);
            hierarchy.setIsConcurrent(0);
            hierarchy.setUpdatedTime(LocalDateTime.now());
            hierarchyMapper.update(hierarchy);
            
            createChangeRecord(hierarchy.getHierarchyCode(), "SECONDED", oldIncumbents, incumbents);
        }
    }

    @Override
    @Transactional
    public PositionHierarchyHistory createChangeRecord(String hierarchyCode, String changeType,
                                                       String beforeContent, String afterContent) {
        PositionHierarchyHistory history = new PositionHierarchyHistory();
        history.setChangeNo("PH-CHG-" + System.currentTimeMillis());
        history.setHierarchyCode(hierarchyCode);
        history.setChangeType(changeType);
        history.setBeforeContent(beforeContent);
        history.setAfterContent(afterContent);
        // Note: PositionHierarchyHistory does not have createdTime field
        historyMapper.insert(history);
        return history;
    }

    @Override
    public List<PositionHierarchyHistory> findHistoryByHierarchyCode(String hierarchyCode) {
        return historyMapper.findByHierarchyCode(hierarchyCode);
    }

    @Override
    public List<PositionHierarchyHistory> findHistoryByOrgLevel(Integer orgLevel) {
        return historyMapper.findByOrgLevel(orgLevel);
    }

    @Override
    public List<PositionHierarchyHistory> findByChangeType(String changeType) {
        return historyMapper.findByChangeType(changeType);
    }

    @Override
    @Transactional
    public List<PositionHierarchy> batchCreate(List<PositionHierarchy> hierarchies) {
        for (PositionHierarchy hierarchy : hierarchies) {
            create(hierarchy);
        }
        return hierarchies;
    }

    /**
     * DUTY-003: Configure custom position hierarchy for specific organization.
     * Allows organizations to define personalized position titles while maintaining
     * integration with quota and command chain management.
     *
     * Features:
     * - Custom position naming (e.g., "专项业务副经理")
     * - Integration with staffing quota control
     * - Integration with command chain management
     * - Approval workflow for activation
     * - Version tracking for audit trail
     *
     * @param orgId Organization ID
     * @param hierarchy Custom position hierarchy configuration
     */
    @Override
    @Transactional
    public void configureCustomHierarchy(Long orgId, PositionHierarchy hierarchy) {
        // DUTY-001: Validate position grade matches organization level
        validateGradeOrgLevelBinding(hierarchy);
        
        // DUTY-002: Validate principal position uniqueness
        validatePrincipalUniqueness(hierarchy);
        
        // Set organization context
        hierarchy.setOrgId(orgId);
        hierarchy.setCreatedTime(LocalDateTime.now());
        hierarchy.setHierarchyStatus("ENABLED");
        
        // Insert custom hierarchy
        hierarchyMapper.insert(hierarchy);
        
        // Create change record for audit trail
        createChangeRecord(hierarchy.getHierarchyCode(), "CUSTOM_CONFIG", "", 
            hierarchy.getHierarchyName() + "," + hierarchy.getHierarchyGrade());
        
        // TODO: Initiate approval workflow for custom hierarchy activation
        // In production, this should trigger multi-level approval
        // Workflow: DEPT_HEAD -> HR_REVIEW -> FINAL_APPROVAL
    }
}