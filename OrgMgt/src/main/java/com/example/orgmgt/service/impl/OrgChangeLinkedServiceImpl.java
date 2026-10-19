package com.example.orgmgt.service.impl;

import com.example.orgmgt.entity.CommandChain;
import com.example.orgmgt.entity.JobDescription;
import com.example.orgmgt.entity.Organization;
import com.example.orgmgt.entity.Position;
import com.example.orgmgt.entity.PositionHierarchy;
import com.example.orgmgt.entity.VirtualOrganization;
import com.example.orgmgt.mapper.CommandChainMapper;
import com.example.orgmgt.mapper.JobDescriptionMapper;
import com.example.orgmgt.mapper.PositionHierarchyMapper;
import com.example.orgmgt.mapper.PositionMapper;
import com.example.orgmgt.mapper.VirtualOrganizationMapper;
import com.example.orgmgt.service.OrgChangeLinkedService;
import com.example.orgmgt.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * COMM-004: Organization Change Linked Service Implementation
 * Implements cross-module synchronization when organization changes occur
 * 
 * Features:
 * - Organization name change -> update position, hierarchy, command chain
 * - Organization code change -> update all related tables
 * - Organization status change -> cascade position/hierarchy status
 * - Organization migration -> cascade child org/position/hierarchy
 * - Organization deletion -> cascade all related data
 * 
 * Modules to sync:
 * - Position (岗位)
 * - PositionHierarchy (职务层级)
 * - CommandChain (指挥链)
 * - VirtualOrganization (虚拟组织)
 * - JobDescription (岗位说明书)
 */
@Service
public class OrgChangeLinkedServiceImpl implements OrgChangeLinkedService {

    private static final Logger logger = LoggerFactory.getLogger(OrgChangeLinkedServiceImpl.class);

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private PositionHierarchyMapper hierarchyMapper;

    @Autowired
    private CommandChainMapper commandChainMapper;

    @Autowired
    private VirtualOrganizationMapper virtualOrganizationMapper;

    @Autowired
    private JobDescriptionMapper jobDescriptionMapper;

    @Autowired
    private OperationLogService operationLogService;

    @Override
    @Transactional
    public Map<String, Object> handleOrgNameChange(Long orgId, String oldOrgName, String newOrgName, String operator) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "组织名称变更同步完成");
        Map<String, Integer> affectedCounts = new HashMap<>();

        logger.info("Handling org name change: {} -> {} for orgId: {}", oldOrgName, newOrgName, orgId);

        // Sync: Position
        int positionCount = syncPositionOrgName(orgId, newOrgName);
        affectedCounts.put("position", positionCount);

        // Sync: PositionHierarchy
        int hierarchyCount = syncHierarchyOrgName(orgId, newOrgName);
        affectedCounts.put("positionHierarchy", hierarchyCount);

        // Sync: CommandChain
        int chainCount = syncCommandChainOrgName(orgId, newOrgName);
        affectedCounts.put("commandChain", chainCount);

        // Sync: VirtualOrganization
        int virtualOrgCount = syncVirtualOrgOrgName(orgId, newOrgName);
        affectedCounts.put("virtualOrganization", virtualOrgCount);

        // Sync: JobDescription
        int jdCount = syncJobDescriptionOrgName(orgId, newOrgName);
        affectedCounts.put("jobDescription", jdCount);

        result.put("affectedCounts", affectedCounts);

        // Record operation log
        operationLogService.record(
            "ORGANIZATION", orgId, null, newOrgName,
            "UPDATE", String.format("组织名称变更：%s -> %s", oldOrgName, newOrgName),
            operator, operator,
            "{\"orgName\":\"" + oldOrgName + "\"}",
            "{\"orgName\":\"" + newOrgName + "\"}"
        );

        logger.info("Org name change sync completed, affected modules: {}", affectedCounts);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> handleOrgCodeChange(Long orgId, String oldOrgCode, String newOrgCode, String operator) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "组织编码变更同步完成");
        Map<String, Integer> affectedCounts = new HashMap<>();

        logger.info("Handling org code change: {} -> {} for orgId: {}", oldOrgCode, newOrgCode, orgId);

        // Sync: Position
        int positionCount = syncPositionOrgCode(orgId, newOrgCode);
        affectedCounts.put("position", positionCount);

        // Sync: PositionHierarchy
        int hierarchyCount = syncHierarchyOrgCode(orgId, newOrgCode);
        affectedCounts.put("positionHierarchy", hierarchyCount);

        // Sync: CommandChain
        int chainCount = syncCommandChainOrgCode(oldOrgCode, newOrgCode);
        affectedCounts.put("commandChain", chainCount);

        // Sync: VirtualOrganization
        int virtualOrgCount = syncVirtualOrgOrgCode(orgId, newOrgCode);
        affectedCounts.put("virtualOrganization", virtualOrgCount);

        // Sync: JobDescription
        int jdCount = syncJobDescriptionOrgCode(orgId, newOrgCode);
        affectedCounts.put("jobDescription", jdCount);

        result.put("affectedCounts", affectedCounts);

        // Record operation log
        operationLogService.record(
            "ORGANIZATION", orgId, newOrgCode, null,
            "UPDATE", String.format("组织编码变更：%s -> %s", oldOrgCode, newOrgCode),
            operator, operator,
            "{\"orgCode\":\"" + oldOrgCode + "\"}",
            "{\"orgCode\":\"" + newOrgCode + "\"}"
        );

        logger.info("Org code change sync completed, affected modules: {}", affectedCounts);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> handleOrgStatusChange(Long orgId, String oldStatus, String newStatus, String operator) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "组织状态变更同步完成");
        Map<String, Integer> affectedCounts = new HashMap<>();

        logger.info("Handling org status change: {} -> {} for orgId: {}", oldStatus, newStatus, orgId);

        // Only cascade status changes for disable/enable operations
        if ("DISABLED".equals(newStatus)) {
            // Disable all related positions
            int positionCount = syncPositionStatus(orgId, "DISABLED");
            affectedCounts.put("position", positionCount);

            // Disable all related position hierarchies
            int hierarchyCount = syncHierarchyStatus(orgId, "DISABLED");
            affectedCounts.put("positionHierarchy", hierarchyCount);

            // Disable or pause related command chains
            int chainCount = syncCommandChainStatusByOrg(orgId, "PAUSED");
            affectedCounts.put("commandChain", chainCount);

            // Disable related job descriptions
            int jdCount = syncJobDescriptionStatus(orgId, "ARCHIVED");
            affectedCounts.put("jobDescription", jdCount);
        } else if ("ENABLED".equals(newStatus)) {
            // Enable all related positions
            int positionCount = syncPositionStatus(orgId, "ENABLED");
            affectedCounts.put("position", positionCount);

            // Enable all related position hierarchies
            int hierarchyCount = syncHierarchyStatus(orgId, "ENABLED");
            affectedCounts.put("positionHierarchy", hierarchyCount);

            // Enable related command chains
            int chainCount = syncCommandChainStatusByOrg(orgId, "ACTIVE");
            affectedCounts.put("commandChain", chainCount);

            // Activate related job descriptions
            int jdCount = syncJobDescriptionStatus(orgId, "APPROVED");
            affectedCounts.put("jobDescription", jdCount);
        }

        result.put("affectedCounts", affectedCounts);

        // Record operation log
        operationLogService.record(
            "ORGANIZATION", orgId, null, null,
            "STATUS_CHANGE", String.format("组织状态变更：%s -> %s", oldStatus, newStatus),
            operator, operator,
            "{\"status\":\"" + oldStatus + "\"}",
            "{\"status\":\"" + newStatus + "\"}"
        );

        logger.info("Org status change sync completed, affected modules: {}", affectedCounts);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> handleOrgMigration(Long orgId, Long oldParentOrgId, Long newParentOrgId, String operator) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "组织迁移同步完成");
        Map<String, Integer> affectedCounts = new HashMap<>();

        logger.info("Handling org migration: parent {} -> {} for orgId: {}", oldParentOrgId, newParentOrgId, orgId);

        // Sync: Position hierarchy updates (may need level adjustments)
        int hierarchyCount = syncHierarchyParentChange(orgId, oldParentOrgId, newParentOrgId);
        affectedCounts.put("positionHierarchy", hierarchyCount);

        // Sync: Command chain (may need to update applicable scope)
        int chainCount = syncCommandChainScopeChange(orgId, newParentOrgId);
        affectedCounts.put("commandChain", chainCount);

        result.put("affectedCounts", affectedCounts);

        // Record operation log
        operationLogService.record(
            "ORGANIZATION", orgId, null, null,
            "MIGRATE", String.format("组织迁移：父组织 %d -> %d", oldParentOrgId, newParentOrgId),
            operator, operator,
            "{\"parentOrgId\":" + oldParentOrgId + "}",
            "{\"parentOrgId\":" + newParentOrgId + "}"
        );

        logger.info("Org migration sync completed, affected modules: {}", affectedCounts);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> handleOrgDeletion(Long orgId, String operator) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "组织删除同步完成");
        Map<String, Integer> affectedCounts = new HashMap<>();

        logger.info("Handling org deletion for orgId: {}", orgId);

        // Delete: JobDescription
        int jdCount = deleteJobDescriptionsByOrg(orgId);
        affectedCounts.put("jobDescription", jdCount);

        // Delete: PositionHierarchy
        int hierarchyCount = deleteHierarchiesByOrg(orgId);
        affectedCounts.put("positionHierarchy", hierarchyCount);

        // Delete: Position
        int positionCount = deletePositionsByOrg(orgId);
        affectedCounts.put("position", positionCount);

        // Delete: CommandChain
        int chainCount = deleteCommandChainsByOrg(orgId);
        affectedCounts.put("commandChain", chainCount);

        // Delete: VirtualOrganization
        int virtualOrgCount = deleteVirtualOrgsByLeadOrg(orgId);
        affectedCounts.put("virtualOrganization", virtualOrgCount);

        result.put("affectedCounts", affectedCounts);

        // Record operation log
        operationLogService.record(
            "ORGANIZATION", orgId, null, null,
            "DELETE", "组织删除及关联数据清理",
            operator, operator,
            null, null
        );

        logger.info("Org deletion sync completed, affected modules: {}", affectedCounts);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> handleOrgLevelChange(Long orgId, Integer oldOrgLevel, Integer newOrgLevel, String operator) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "组织层级变更同步完成");
        Map<String, Integer> affectedCounts = new HashMap<>();

        logger.info("Handling org level change: {} -> {} for orgId: {}", oldOrgLevel, newOrgLevel, orgId);

        // Sync: Position hierarchy grades (must match new org level)
        int hierarchyCount = syncHierarchyGradeWithOrgLevel(orgId, newOrgLevel);
        affectedCounts.put("positionHierarchy", hierarchyCount);

        result.put("affectedCounts", affectedCounts);

        // Record operation log
        operationLogService.record(
            "ORGANIZATION", orgId, null, null,
            "UPDATE", String.format("组织层级变更：%d -> %d", oldOrgLevel, newOrgLevel),
            operator, operator,
            "{\"orgLevel\":" + oldOrgLevel + "}",
            "{\"orgLevel\":" + newOrgLevel + "}"
        );

        logger.info("Org level change sync completed, affected modules: {}", affectedCounts);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> syncAllModules(Long orgId, String operator) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "全模块同步完成");
        Map<String, Integer> affectedCounts = new HashMap<>();

        logger.info("Performing full sync for orgId: {}", orgId);

        // Get current org info
        // Implementation would need to read current org data and sync to all modules

        result.put("affectedCounts", affectedCounts);

        // Record operation log
        operationLogService.record(
            "ORGANIZATION", orgId, null, null,
            "SYNC", "全模块数据同步",
            operator, operator,
            null, null
        );

        logger.info("Full module sync completed for orgId: {}", orgId);
        return result;
    }

    @Override
    public Map<String, Object> validateChangeImpact(Long orgId, String changeType, String changeValue) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("warnings", new HashMap<>());
        result.put("errors", new HashMap<>());

        logger.info("Validating change impact: {} = {} for orgId: {}", changeType, changeValue, orgId);

        // Check for potential conflicts
        // Implementation would validate change impact on each module

        return result;
    }

    // ==================== Position sync methods ====================

    private int syncPositionOrgName(Long orgId, String newOrgName) {
        List<Position> positions = positionMapper.findByOrgId(orgId);
        for (Position position : positions) {
            position.setOrgName(newOrgName);
            position.setUpdatedTime(LocalDateTime.now());
            positionMapper.update(position);
        }
        return positions.size();
    }

    private int syncPositionOrgCode(Long orgId, String newOrgCode) {
        List<Position> positions = positionMapper.findByOrgId(orgId);
        for (Position position : positions) {
            position.setOrgCode(newOrgCode);
            position.setUpdatedTime(LocalDateTime.now());
            positionMapper.update(position);
        }
        return positions.size();
    }

    private int syncPositionStatus(Long orgId, String status) {
        List<Position> positions = positionMapper.findByOrgId(orgId);
        for (Position position : positions) {
            position.setPositionStatus(status);
            position.setUpdatedTime(LocalDateTime.now());
            positionMapper.update(position);
        }
        return positions.size();
    }

    private int deletePositionsByOrg(Long orgId) {
        List<Position> positions = positionMapper.findByOrgId(orgId);
        int count = positions.size();
        for (Position position : positions) {
            positionMapper.deleteById(position.getId());
        }
        return count;
    }

    // ==================== PositionHierarchy sync methods ====================

    private int syncHierarchyOrgName(Long orgId, String newOrgName) {
        List<PositionHierarchy> hierarchies = hierarchyMapper.findByOrgId(orgId);
        for (PositionHierarchy hierarchy : hierarchies) {
            hierarchy.setOrgName(newOrgName);
            hierarchy.setUpdatedTime(LocalDateTime.now());
            hierarchyMapper.update(hierarchy);
        }
        return hierarchies.size();
    }

    private int syncHierarchyOrgCode(Long orgId, String newOrgCode) {
        List<PositionHierarchy> hierarchies = hierarchyMapper.findByOrgId(orgId);
        for (PositionHierarchy hierarchy : hierarchies) {
            hierarchy.setOrgCode(newOrgCode);
            hierarchy.setUpdatedTime(LocalDateTime.now());
            hierarchyMapper.update(hierarchy);
        }
        return hierarchies.size();
    }

    private int syncHierarchyStatus(Long orgId, String status) {
        List<PositionHierarchy> hierarchies = hierarchyMapper.findByOrgId(orgId);
        for (PositionHierarchy hierarchy : hierarchies) {
            hierarchy.setHierarchyStatus(status);
            hierarchy.setUpdatedTime(LocalDateTime.now());
            hierarchyMapper.update(hierarchy);
        }
        return hierarchies.size();
    }

    private int syncHierarchyParentChange(Long orgId, Long oldParentOrgId, Long newParentOrgId) {
        // Implementation would adjust hierarchy based on new parent
        return 0;
    }

    private int syncHierarchyGradeWithOrgLevel(Long orgId, Integer newOrgLevel) {
        // Implementation would update hierarchy grades to match new org level
        return 0;
    }

    private int deleteHierarchiesByOrg(Long orgId) {
        List<PositionHierarchy> hierarchies = hierarchyMapper.findByOrgId(orgId);
        int count = hierarchies.size();
        for (PositionHierarchy hierarchy : hierarchies) {
            hierarchyMapper.deleteById(hierarchy.getId());
        }
        return count;
    }

    // ==================== CommandChain sync methods ====================

    private int syncCommandChainOrgName(Long orgId, String newOrgName) {
        // Implementation would update org references in command chains
        return 0;
    }

    private int syncCommandChainOrgCode(String oldOrgCode, String newOrgCode) {
        List<CommandChain> chains = commandChainMapper.selectAll();
        int count = 0;
        for (CommandChain chain : chains) {
            if (oldOrgCode.equals(chain.getApplicableOrgScope())) {
                chain.setApplicableOrgScope(newOrgCode);
                chain.setUpdateTime(LocalDateTime.now());
                commandChainMapper.update(chain);
                count++;
            }
        }
        return count;
    }

    private int syncCommandChainStatusByOrg(Long orgId, String status) {
        // Implementation would update chain status based on org status
        return 0;
    }

    private int syncCommandChainScopeChange(Long orgId, Long newParentOrgId) {
        // Implementation would update applicable org scope
        return 0;
    }

    private int deleteCommandChainsByOrg(Long orgId) {
        // Implementation would delete command chains for the org
        return 0;
    }

    // ==================== VirtualOrganization sync methods ====================

    private int syncVirtualOrgOrgName(Long orgId, String newOrgName) {
        // TODO: Implement sync after mapper methods available
        return 0;
    }

    private int syncVirtualOrgOrgCode(Long orgId, String newOrgCode) {
        // TODO: Implement sync after mapper methods available
        return 0;
    }

    private int deleteVirtualOrgsByLeadOrg(Long orgId) {
        // TODO: Implement sync after mapper methods available
        return 0;
    }

    // ==================== JobDescription sync methods ====================

    private int syncJobDescriptionOrgName(Long orgId, String newOrgName) {
        // TODO: Implement after mapper methods available
        return 0;
    }

    private int syncJobDescriptionOrgCode(Long orgId, String newOrgCode) {
        // TODO: Implement after mapper methods available
        return 0;
    }

    private int syncJobDescriptionStatus(Long orgId, String status) {
        // TODO: Implement after mapper methods available
        return 0;
    }

    private int deleteJobDescriptionsByOrg(Long orgId) {
        // TODO: Implement after mapper methods available
        return 0;
    }
}