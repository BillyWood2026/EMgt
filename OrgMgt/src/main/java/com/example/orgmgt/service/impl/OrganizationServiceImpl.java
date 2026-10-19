package com.example.orgmgt.service.impl;

import com.example.orgmgt.entity.Organization;
import com.example.orgmgt.entity.Position;
import com.example.orgmgt.mapper.OrganizationMapper;
import com.example.orgmgt.mapper.PositionMapper;
import com.example.orgmgt.service.OrganizationService;
import com.example.orgmgt.service.OrgChangeLinkedService;
import com.example.orgmgt.service.OrgChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    @Autowired
    private OrganizationMapper organizationMapper;
    
    @Autowired
    private PositionMapper positionMapper;
    
    @Autowired
    private OrgChangeService orgChangeService;
    
    @Autowired
    private OrgChangeLinkedService orgChangeLinkedService;

    @Override
    public List<Organization> findAll() {
        return organizationMapper.findAll();
    }

    @Override
    public Organization findById(Long id) {
        return organizationMapper.findById(id);
    }

    @Override
    public Organization findByOrgCode(String orgCode) {
        return organizationMapper.findByOrgCode(orgCode);
    }

    @Override
    public List<Organization> findByOrgLevel(Integer orgLevel) {
        return organizationMapper.findByOrgLevel(orgLevel);
    }

    @Override
    public List<Organization> findByParentOrgId(Long parentOrgId) {
        return organizationMapper.findByParentOrgId(parentOrgId);
    }

    @Override
    public List<Organization> findByOrgStatus(String orgStatus) {
        return organizationMapper.findByOrgStatus(orgStatus);
    }

    /**
     * Organization level constants (five-level hierarchy)
     */
    public static final int LEVEL_COMPANY = 1;      // Level 1: Company
    public static final int LEVEL_CENTER = 2;       // Level 2: Center
    public static final int LEVEL_DEPARTMENT = 3;   // Level 3: Department
    public static final int LEVEL_SECTION = 4;      // Level 4: Section
    public static final int LEVEL_GROUP = 5;        // Level 5: Group

    /**
     * Validates hierarchical level constraint.
     * Parent org level + 1 must equal child org level.
     * Direct hierarchy creation only, no skipping levels.
     *
     * @param parentOrg Parent organization
     * @param newOrgLevel Level of new organization to create
     * @throws IllegalArgumentException if level constraint is violated
     */
    private void validateLevelConstraint(Organization parentOrg, Integer newOrgLevel) {
        if (parentOrg == null) {
            // Root level organization - must be level 1 (company)
            if (newOrgLevel != LEVEL_COMPANY) {
                throw new IllegalArgumentException("根组织仅能创建一级公司组织");
            }
            return;
        }

        int parentLevel = parentOrg.getOrgLevel();
        int expectedChildLevel = parentLevel + 1;

        // Check if trying to skip levels
        if (newOrgLevel != expectedChildLevel) {
            throw new IllegalArgumentException(
                String.format("Cannot create organization across levels. Parent is level %d organization, only level %d child organization can be created",
                    parentLevel, expectedChildLevel));
        }

        // Check if parent is at max depth (level 5 cannot have children)
        if (parentLevel >= LEVEL_GROUP) {
            throw new IllegalArgumentException("Level 5 group organization cannot create child organizations");
        }
    }

    @Override
    @Transactional
    public Organization create(Organization organization) {
        // Validate level constraint before creation
        if (organization.getParentOrgId() != null && organization.getParentOrgId() > 0) {
            Organization parent = organizationMapper.findById(organization.getParentOrgId());
            if (parent != null) {
                validateLevelConstraint(parent, organization.getOrgLevel());
            }
        } else {
            // No parent specified - must be creating a company (level 1)
            organization.setParentOrgId(0L);
            organization.setParentOrgCode(null);
            validateLevelConstraint(null, organization.getOrgLevel());
        }

        if (organization.getOrgCode() == null || organization.getOrgCode().isEmpty()) {
            organization.setOrgCode(generateOrgCode(organization.getParentOrgId(), organization.getOrgLevel()));
        }
        organization.setOrgStatus("ENABLED");
        organization.setCreatedTime(LocalDateTime.now());
        organizationMapper.insert(organization);
        return organization;
    }

    @Override
    @Transactional
    public Organization update(Organization organization) {
        Organization existing = organizationMapper.findById(organization.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Organization not found");
        }
        
        // COMM-004: Handle org name change
        if (existing.getOrgName() != null && !existing.getOrgName().equals(organization.getOrgName())) {
            orgChangeLinkedService.handleOrgNameChange(
                organization.getId(), existing.getOrgName(), organization.getOrgName(), "System");
        }
        
        // COMM-004: Handle org code change
        if (existing.getOrgCode() != null && !existing.getOrgCode().equals(organization.getOrgCode())) {
            orgChangeLinkedService.handleOrgCodeChange(
                organization.getId(), existing.getOrgCode(), organization.getOrgCode(), "System");
        }
        
        // COMM-004: Handle org level change
        if (existing.getOrgLevel() != null && !existing.getOrgLevel().equals(organization.getOrgLevel())) {
            orgChangeLinkedService.handleOrgLevelChange(
                organization.getId(), existing.getOrgLevel(), organization.getOrgLevel(), "System");
        }
        
        organization.setUpdatedTime(LocalDateTime.now());
        organizationMapper.update(organization);
        return organization;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        // COMM-004: Handle org deletion and cascade to all modules
        orgChangeLinkedService.handleOrgDeletion(id, "System");
        organizationMapper.deleteById(id);
    }

    /**
     * ORG-004: Updates organization status with validation.
     * When disabling an organization, checks for child organizations and positions.
     *
     * @param id Organization ID
     * @param status New status (ENABLED/DISABLED)
     * @throws IllegalStateException if organization has children or positions when disabling
     */
    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        Organization existing = organizationMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Organization not found");
        }
        
        String oldStatus = existing.getOrgStatus();
        
        // Check if trying to disable the organization
        if ("DISABLED".equals(status)) {
            validateCanDisable(id);
        }
        
        organizationMapper.updateStatus(id, status);
        
        // COMM-004: Handle org status change and cascade to all modules
        if (oldStatus != null && !oldStatus.equals(status)) {
            orgChangeLinkedService.handleOrgStatusChange(id, oldStatus, status, "System");
        }
    }

    /**
     * Validates that an organization can be disabled.
     * Checks for child organizations and positions.
     *
     * @param orgId Organization ID to validate
     * @throws IllegalStateException if organization has children or positions
     */
    private void validateCanDisable(Long orgId) {
        Organization org = organizationMapper.findById(orgId);
        if (org == null) {
            throw new IllegalArgumentException("Organization not found");
        }

        // Check for child organizations
        List<Organization> children = organizationMapper.findByParentOrgId(orgId);
        if (children != null && !children.isEmpty()) {
            throw new IllegalStateException(
                String.format("Organization [%s] has %d child organizations. Please handle child organizations before disabling",
                    org.getOrgName(), children.size()));
        }

        // Check for positions under this organization
        List<Position> positions = positionMapper.findByOrgId(orgId);
        if (positions != null && !positions.isEmpty()) {
            throw new IllegalStateException(
                String.format("Organization [%s] has %d positions. Please handle positions before disabling",
                    org.getOrgName(), positions.size()));
        }
    }

    @Override
    public List<Organization> findByOrgNameLike(String orgName) {
        return organizationMapper.findByOrgNameLike("%" + orgName + "%");
    }

    @Override
    public List<Organization> findByBusinessLine(String businessLine) {
        return organizationMapper.findByBusinessLine(businessLine);
    }

    @Override
    public String generateOrgCode(Long parentOrgId, Integer orgLevel) {
        if (parentOrgId == null || parentOrgId == 0) {
            return "C001";
        }
        Organization parent = organizationMapper.findById(parentOrgId);
        if (parent == null) {
            return "C001";
        }
        String parentCode = parent.getOrgCode();
        List<Organization> children = organizationMapper.findByParentOrgId(parentOrgId);
        int count = children.size() + 1;
        String suffix = String.format("%03d", count);
        return parentCode + "-" + suffix;
    }

    /**
     * ORG-003: Migrates organization to a new parent.
     * This method creates an approval request rather than directly updating.
     * The actual migration is executed only after approval is granted.
     *
     * @param orgId Organization ID to migrate
     * @param newParentOrgId New parent organization ID
     * @param changeReason Reason for the migration
     * @param attachmentPath Path to supporting documents
     * @param initiator User initiating the change
     * @throws IllegalArgumentException if organization or new parent not found
     */
    @Override
    @Transactional
    public Map<String, Object> migrateOrg(Long orgId, Long newParentOrgId, String changeReason, 
                                          String attachmentPath, String initiator) {
        Map<String, Object> result = new HashMap<>();
        
        Organization org = organizationMapper.findById(orgId);
        if (org == null) {
            throw new IllegalArgumentException("Organization not found");
        }
        
        Organization newParent = organizationMapper.findById(newParentOrgId);
        if (newParent == null) {
            throw new IllegalArgumentException("Target parent organization not found");
        }
        
        // Validate level constraint - can only migrate within same level constraint
        // (cannot move a department under a group, etc.)
        if (newParent.getOrgLevel() != org.getOrgLevel() - 1) {
            throw new IllegalArgumentException(
                String.format("Migration target violates level constraint. Level %d organization can only migrate under level %d organization",
                    org.getOrgLevel(), org.getOrgLevel() - 1));
        }
        
        // Prepare before/after data for change history
        String beforeData = String.format("parentOrgId:%d,parentOrgCode:%s", 
            org.getParentOrgId(), org.getParentOrgCode());
        String afterData = String.format("parentOrgId:%d,parentOrgCode:%s", 
            newParentOrgId, newParent.getOrgCode());
        
        // Create approval request via OrgChangeService
        // This creates a pending approval that must be processed before actual migration
        result = orgChangeService.createApproval(
            orgId,
            "MIGRATE",
            String.format("Organization migration: from %s to %s, reason: %s", 
                org.getParentOrgCode(), newParent.getOrgCode(), changeReason),
            beforeData,
            afterData,
            initiator
        );
        
        // Store org ID in result for later use during approval
        result.put("orgId", orgId);
        result.put("orgCode", org.getOrgCode());
        result.put("orgName", org.getOrgName());
        result.put("newParentId", newParentOrgId);
        result.put("newParentCode", newParent.getOrgCode());
        
        return result;
    }
    
    /**
     * Internal method to execute migration after approval is granted.
     * Called by OrgChangeService when approval is completed.
     *
     * @param orgId Organization ID
     * @param newParentOrgId New parent organization ID
     */
    @Transactional
    public void executeMigration(Long orgId, Long newParentOrgId) {
        Organization org = organizationMapper.findById(orgId);
        Organization newParent = organizationMapper.findById(newParentOrgId);
        
        if (org != null && newParent != null) {
            org.setParentOrgId(newParentOrgId);
            org.setParentOrgCode(newParent.getOrgCode());
            org.setUpdatedTime(LocalDateTime.now());
            organizationMapper.update(org);
        }
    }

    @Override
    public List<Map<String, Object>> getOrgTree() {
        List<Organization> allOrgs = organizationMapper.findAll();
        Map<Long, Map<String, Object>> nodeMap = new HashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();

        for (Organization org : allOrgs) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", org.getId());
            node.put("orgCode", org.getOrgCode());
            node.put("orgName", org.getOrgName());
            node.put("orgLevel", org.getOrgLevel());
            node.put("orgStatus", org.getOrgStatus());
            node.put("children", new ArrayList<>());
            nodeMap.put(org.getId(), node);

            if (org.getParentOrgId() == null || org.getParentOrgId() == 0) {
                roots.add(node);
            }
        }

        for (Organization org : allOrgs) {
            if (org.getParentOrgId() != null && org.getParentOrgId() != 0) {
                Map<String, Object> parentNode = nodeMap.get(org.getParentOrgId());
                if (parentNode != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parentNode.get("children");
                    children.add(nodeMap.get(org.getId()));
                }
            }
        }

        return roots;
    }

    @Override
    @Transactional
    public List<Organization> batchCreate(List<Organization> organizations) {
        for (Organization org : organizations) {
            create(org);
        }
        return organizations;
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            organizationMapper.deleteById(id);
        }
    }
}