package com.example.orgmgt.service.impl;

import com.example.orgmgt.entity.OrgUnit;
import com.example.orgmgt.service.OrgUnitService;
import com.example.orgmgt.mapper.OrgUnitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Organization Unit Service Implementation
 * Enforces five-level hierarchy constraints
 */
@Service
public class OrgUnitServiceImpl implements OrgUnitService {
    
    @Autowired
    private OrgUnitMapper orgUnitMapper;
    
    @Override
    public List<Map<String, Object>> getOrgTree() {
        List<OrgUnit> allOrgs = orgUnitMapper.selectAll();
        return buildTree(allOrgs, 0L);
    }
    
    private List<Map<String, Object>> buildTree(List<OrgUnit> allOrgs, Long parentOrgId) {
        List<Map<String, Object>> tree = new ArrayList<>();

        for (OrgUnit org : allOrgs) {
            if ((org.getParentOrgId() == null && parentOrgId == 0) ||
                (org.getParentOrgId() != null && org.getParentOrgId().equals(parentOrgId))) {

                Map<String, Object> node = new HashMap<>();
                node.put("id", org.getId());
                node.put("orgCode", org.getOrgCode());
                node.put("label", org.getOrgName());
                node.put("orgName", org.getOrgName());
                node.put("orgLevel", org.getOrgLevel());
                node.put("orgLevelName", String.valueOf(org.getOrgLevel()));
                node.put("orgStatus", org.getOrgStatus());
                node.put("managerName", org.getOrgManager());
                node.put("sortOrder", org.getSortOrder() != null ? org.getSortOrder() : 0);
                node.put("children", buildTree(allOrgs, org.getId()));
                
                tree.add(node);
            }
        }
        
        tree.sort(Comparator.comparingInt(n -> (Integer) ((Map<String, Object>) n).get("sortOrder")));
        return tree;
    }
    
    @Override
    public List<OrgUnit> getOrgList(String keyword, String orgLevel, String status, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
        return orgUnitMapper.selectList(keyword, orgLevel, status, safePage, safeSize, (safePage - 1) * safeSize);
    }
    
    @Override
    public OrgUnit getOrgById(Long id) {
        return orgUnitMapper.selectById(id);
    }
    
    @Override
    public OrgUnit getOrgByCode(String orgCode) {
        return orgUnitMapper.selectByCode(orgCode);
    }
    
    @Override
    @Transactional
    public Map<String, Object> createOrg(OrgUnit orgUnit, String initiator) {
        Map<String, Object> result = validateCreate(orgUnit);
        if (!Boolean.TRUE.equals(result.get("valid"))) {
            return result;
        }
        
        // Generate org code
        if (orgUnit.getOrgCode() == null || orgUnit.getOrgCode().isEmpty()) {
            orgUnit.setOrgCode(generateOrgCode(orgUnit.getParentOrgId()));
        }
        
        orgUnit.setOrgStatus("ENABLED");
        orgUnit.setCreatedTime(LocalDateTime.now());
        orgUnit.setCreatedBy(initiator);
        orgUnit.setSortOrder(getNextSortOrder(orgUnit.getParentOrgId()));
        orgUnit.setDeleted(0);
        orgUnit.setVersion(1);
        
        orgUnitMapper.insert(orgUnit);
        
        // Initiate approval workflow
        initiateApproval(orgUnit.getId(), "CREATE", initiator);
        
        result.clear();
        result.put("success", true);
        result.put("message", "Organization created successfully, pending approval");
        result.put("orgId", orgUnit.getId());
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> updateOrg(Long id, OrgUnit orgUnit, String operator) {
        Map<String, Object> result = new HashMap<>();
        
        OrgUnit existing = orgUnitMapper.selectById(id);
        if (existing == null) {
            result.put("success", false);
            result.put("message", "Organization not found");
            return result;
        }
        
        // Validate name uniqueness
        if (orgUnit.getOrgName() != null && !orgUnit.getOrgName().equals(existing.getOrgName())) {
            if (orgUnitMapper.checkNameExists(orgUnit.getOrgName(), id) > 0) {
                result.put("success", false);
                result.put("message", "Organization name already exists");
                return result;
            }
        }
        
        orgUnit.setId(id);
        orgUnit.setUpdatedTime(LocalDateTime.now());
        orgUnit.setUpdatedBy(operator);
        orgUnit.setDeleted(existing.getDeleted());
        orgUnit.setVersion(existing.getVersion());
        // 自定义 update 为全列 SET：请求未携带的字段沿用旧值，避免 orgStatus 等被置空导致业务校验误判
        if (orgUnit.getOrgCode() == null) orgUnit.setOrgCode(existing.getOrgCode());
        if (orgUnit.getOrgName() == null) orgUnit.setOrgName(existing.getOrgName());
        if (orgUnit.getOrgLevel() == null) orgUnit.setOrgLevel(existing.getOrgLevel());
        if (orgUnit.getParentOrgId() == null) orgUnit.setParentOrgId(existing.getParentOrgId());
        if (orgUnit.getParentOrgCode() == null) orgUnit.setParentOrgCode(existing.getParentOrgCode());
        if (orgUnit.getOrgStatus() == null) orgUnit.setOrgStatus(existing.getOrgStatus());
        if (orgUnit.getEstablishDate() == null) orgUnit.setEstablishDate(existing.getEstablishDate());
        if (orgUnit.getOrgManager() == null) orgUnit.setOrgManager(existing.getOrgManager());
        if (orgUnit.getContactPhone() == null) orgUnit.setContactPhone(existing.getContactPhone());
        if (orgUnit.getBusinessLine() == null) orgUnit.setBusinessLine(existing.getBusinessLine());
        if (orgUnit.getLocation() == null) orgUnit.setLocation(existing.getLocation());
        if (orgUnit.getSortOrder() == null) orgUnit.setSortOrder(existing.getSortOrder());
        if (orgUnit.getRemark() == null) orgUnit.setRemark(existing.getRemark());
        if (orgUnit.getIsCoreOrg() == null) orgUnit.setIsCoreOrg(existing.getIsCoreOrg());
        if (orgUnit.getIsIndependentAccounting() == null) orgUnit.setIsIndependentAccounting(existing.getIsIndependentAccounting());
        if (orgUnit.getIsStaffingControlled() == null) orgUnit.setIsStaffingControlled(existing.getIsStaffingControlled());

        orgUnitMapper.update(orgUnit);
        
        // Record change history
        recordChangeHistory(id, "UPDATE", existing, orgUnit, operator);
        
        result.put("success", true);
        result.put("message", "Organization updated successfully");
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> deleteOrg(Long id, String operator) {
        Map<String, Object> result = new HashMap<>();
        
        OrgUnit orgUnit = orgUnitMapper.selectById(id);
        if (orgUnit == null) {
            result.put("success", false);
            result.put("message", "Organization not found");
            return result;
        }
        
        if (hasChildren(id)) {
            result.put("success", false);
            result.put("message", "Cannot delete organization with children");
            return result;
        }
        
        if ("ENABLED".equals(orgUnit.getOrgStatus())) {
            result.put("success", false);
            result.put("message", "Please disable organization first before deletion");
            return result;
        }
        
        // Record change history before deletion
        recordChangeHistory(id, "DELETE", orgUnit, null, operator);
        
        orgUnitMapper.delete(id);
        
        result.put("success", true);
        result.put("message", "Organization deleted successfully");
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> toggleStatus(Long id, String newStatus, String operator) {
        Map<String, Object> result = new HashMap<>();
        
        OrgUnit orgUnit = orgUnitMapper.selectById(id);
        if (orgUnit == null) {
            result.put("success", false);
            result.put("message", "Organization not found");
            return result;
        }
        
        if (newStatus.equals(orgUnit.getOrgStatus())) {
            result.put("success", false);
            result.put("message", "Organization is already " + newStatus.toLowerCase());
            return result;
        }
        
        // Check if disabling parent with active children
        if ("DISABLED".equals(newStatus) && hasActiveChildren(id)) {
            result.put("success", false);
            result.put("message", "Cannot disable organization with active children");
            return result;
        }
        
        String oldStatus = orgUnit.getOrgStatus();
        orgUnit.setOrgStatus(newStatus);
        orgUnit.setUpdatedTime(LocalDateTime.now());
        orgUnit.setUpdatedBy(operator);
        
        orgUnitMapper.update(orgUnit);
        
        // Record change history
        Map<String, Object> before = new HashMap<>();
        before.put("orgStatus", oldStatus);
        Map<String, Object> after = new HashMap<>();
        after.put("orgStatus", newStatus);
        recordChangeSimple(id, "STATUS_CHANGE", before, after, operator);
        
        result.put("success", true);
        result.put("message", "Organization status updated successfully");
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> moveOrg(Long id, Long newParentId, String operator) {
        Map<String, Object> result = new HashMap<>();
        
        OrgUnit orgUnit = orgUnitMapper.selectById(id);
        if (orgUnit == null) {
            result.put("success", false);
            result.put("message", "Organization not found");
            return result;
        }
        
        OrgUnit newParent = orgUnitMapper.selectById(newParentId);
        if (newParent == null) {
            result.put("success", false);
            result.put("message", "Target parent organization not found");
            return result;
        }
        
        // Validate level constraint: can only move to parent one level higher
        if (newParent.getOrgLevel() != orgUnit.getOrgLevel() - 1) {
            result.put("success", false);
            result.put("message", "Invalid hierarchy level: can only move to immediate parent level");
            return result;
        }
        
        // Cannot move to own descendant
        if (isDescendant(newParentId, id)) {
            result.put("success", false);
            result.put("message", "Cannot move organization to its own descendant");
            return result;
        }
        
        String oldParentCode = orgUnit.getParentOrgCode();
        orgUnit.setParentOrgId(newParentId);
        orgUnit.setParentOrgCode(newParent.getOrgCode());
        orgUnit.setUpdatedTime(LocalDateTime.now());
        orgUnit.setUpdatedBy(operator);
        orgUnit.setSortOrder(getNextSortOrder(newParentId));
        
        // Regenerate org code based on new parent
        String newCode = generateOrgCode(newParentId);
        // Update all children codes recursively
        updateDescendantCodes(id, newCode);
        
        orgUnit.setOrgCode(newCode);
        orgUnitMapper.update(orgUnit);
        
        // Record change history
        Map<String, Object> before = new HashMap<>();
        before.put("parentOrgCode", oldParentCode);
        Map<String, Object> after = new HashMap<>();
        after.put("parentOrgCode", newParent.getOrgCode());
        recordChangeSimple(id, "MOVE", before, after, operator);
        
        result.put("success", true);
        result.put("message", "Organization moved successfully");
        
        return result;
    }
    
    @Override
    public Map<String, Object> validateCreate(OrgUnit orgUnit) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        
        // Check company level (level 1) - only one allowed
        if (OrgUnit.LEVEL_COMPANY == orgUnit.getOrgLevel()) {
            if (orgUnitMapper.countByLevel(OrgUnit.LEVEL_COMPANY) > 0) {
                result.put("valid", false);
                result.put("message", "Only one company node is allowed");
                return result;
            }
            if (orgUnit.getParentOrgId() != null && orgUnit.getParentOrgId() != 0) {
                result.put("valid", false);
                result.put("message", "Company cannot have parent organization");
                return result;
            }
        }
        
        // Check parent for non-company levels
        if (OrgUnit.LEVEL_COMPANY != orgUnit.getOrgLevel()) {
            if (orgUnit.getParentOrgId() == null) {
                result.put("valid", false);
                result.put("message", "Non-company organizations must have a parent");
                return result;
            }
            
            OrgUnit parent = orgUnitMapper.selectById(orgUnit.getParentOrgId());
            if (parent == null) {
                result.put("valid", false);
                result.put("message", "Parent organization not found");
                return result;
            }
            
            // Validate parent level: parent must be exactly one level higher
            if (parent.getOrgLevel() != orgUnit.getOrgLevel() - 1) {
                result.put("valid", false);
                result.put("message", "Invalid parent level: must be one level higher");
                return result;
            }
        }
        
        // Check name uniqueness
        if (orgUnit.getOrgName() != null && orgUnitMapper.checkNameExists(orgUnit.getOrgName(), null) > 0) {
            result.put("valid", false);
            result.put("message", "Organization name already exists");
            return result;
        }
        
        // Check code uniqueness if provided
        if (orgUnit.getOrgCode() != null && !orgUnit.getOrgCode().isEmpty()) {
            if (orgUnitMapper.checkCodeExists(orgUnit.getOrgCode()) > 0) {
                result.put("valid", false);
                result.put("message", "Organization code already exists");
                return result;
            }
        }
        
        return result;
    }
    
    @Override
    public String generateOrgCode(Long parentOrgId) {
        if (parentOrgId == null || parentOrgId == 0) {
            // Company level - generate root code
            return "C001";
        }
        
        OrgUnit parent = orgUnitMapper.selectById(parentOrgId);
        if (parent == null) {
            throw new RuntimeException("Parent organization not found");
        }
        
        String parentOrgCode = parent.getOrgCode();
        int level = parent.getOrgLevel() + 1;
        
        // Get next sequence number for this parent
        List<OrgUnit> siblings = orgUnitMapper.selectByParentId(parentOrgId);
        int nextSeq = siblings.size() + 1;
        
        // Format: parentOrgCode + levelCode + sequence
        String levelCode = getLevelCode(level);
        return parentOrgCode + "-" + levelCode + String.format("%02d", nextSeq);
    }
    
    private String getLevelCode(int level) {
        switch (level) {
            case OrgUnit.LEVEL_COMPANY: return "C";
            case OrgUnit.LEVEL_CENTER: return "CT";
            case OrgUnit.LEVEL_DEPARTMENT: return "D";
            case OrgUnit.LEVEL_SECTION: return "S";
            case OrgUnit.LEVEL_GROUP: return "G";
            default: return "U";
        }
    }
    
    @Override
    public boolean hasChildren(Long orgId) {
        List<OrgUnit> children = orgUnitMapper.selectByParentId(orgId);
        return children != null && !children.isEmpty();
    }
    
    private boolean hasActiveChildren(Long orgId) {
        List<OrgUnit> children = orgUnitMapper.selectByParentId(orgId);
        if (children == null) return false;
        return children.stream().anyMatch(c -> "ENABLED".equals(c.getOrgStatus()));
    }
    
    private boolean isDescendant(Long ancestorId, Long descendantId) {
        List<OrgUnit> children = orgUnitMapper.selectByParentId(ancestorId);
        if (children == null) return false;
        
        for (OrgUnit child : children) {
            if (child.getId().equals(descendantId)) {
                return true;
            }
            if (isDescendant(child.getId(), descendantId)) {
                return true;
            }
        }
        return false;
    }
    
    private int getNextSortOrder(Long parentOrgId) {
        List<OrgUnit> siblings = orgUnitMapper.selectByParentId(parentOrgId);
        if (siblings == null || siblings.isEmpty()) {
            return 1;
        }
        return siblings.stream()
            .mapToInt(OrgUnit::getSortOrder)
            .max()
            .orElse(0) + 1;
    }
    
    private void updateDescendantCodes(Long parentOrgId, String parentOrgCode) {
        List<OrgUnit> children = orgUnitMapper.selectByParentId(parentOrgId);
        if (children == null || children.isEmpty()) return;
        
        for (int i = 0; i < children.size(); i++) {
            OrgUnit child = children.get(i);
            String levelCode = getLevelCode(child.getOrgLevel());
            String newCode = parentOrgCode + "-" + levelCode + String.format("%02d", i + 1);
            
            child.setOrgCode(newCode);
            orgUnitMapper.update(child);
            
            // Recursively update grandchildren
            updateDescendantCodes(child.getId(), newCode);
        }
    }
    
    @Override
    public Map<String, Object> getOrgStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", orgUnitMapper.countAll());
        stats.put("enabled", orgUnitMapper.countByStatus("ENABLED"));
        stats.put("disabled", orgUnitMapper.countByStatus("DISABLED"));
        stats.put("company", orgUnitMapper.countByLevel(OrgUnit.LEVEL_COMPANY));
        stats.put("center", orgUnitMapper.countByLevel(OrgUnit.LEVEL_CENTER));
        stats.put("department", orgUnitMapper.countByLevel(OrgUnit.LEVEL_DEPARTMENT));
        stats.put("section", orgUnitMapper.countByLevel(OrgUnit.LEVEL_SECTION));
        stats.put("group", orgUnitMapper.countByLevel(OrgUnit.LEVEL_GROUP));
        return stats;
    }
    
    @Override
    @Transactional
    public Map<String, Object> batchImport(List<Map<String, Object>> dataList) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Map<String, Object> data : dataList) {
            try {
                OrgUnit orgUnit = new OrgUnit();
                orgUnit.setOrgName((String) data.get("orgName"));
                orgUnit.setOrgLevel((Integer) data.get("orgLevel"));
                orgUnit.setParentOrgId(data.get("parentOrgId") != null ? ((Number) data.get("parentOrgId")).longValue() : null);
                orgUnit.setOrgManager((String) data.get("managerName"));
                orgUnit.setBusinessLine((String) data.get("businessLine"));
                orgUnit.setLocation((String) data.get("location"));
                
                Map<String, Object> createResult = createOrg(orgUnit, "IMPORT");
                if (Boolean.TRUE.equals(createResult.get("success"))) {
                    successCount++;
                } else {
                    failCount++;
                    errors.add((String) createResult.get("message"));
                }
            } catch (Exception e) {
                failCount++;
                errors.add("Import failed: " + e.getMessage());
            }
        }
        
        result.put("success", true);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        
        return result;
    }
    
    @Override
    public List<OrgUnit> batchExport(List<Long> orgIds) {
        List<OrgUnit> result = new ArrayList<>();
        for (Long id : orgIds) {
            OrgUnit org = orgUnitMapper.selectById(id);
            if (org != null) {
                result.add(org);
            }
        }
        return result;
    }
    
    private void initiateApproval(Long orgId, String type, String initiator) {
        // Implementation for approval workflow initiation
    }
    
    private void recordChangeHistory(Long orgId, String type, OrgUnit before, OrgUnit after, String operator) {
        // Implementation for change history recording
    }
    
    private void recordChangeSimple(Long orgId, String type, Map<String, Object> before, Map<String, Object> after, String operator) {
        // Implementation for simplified change history recording
    }
}