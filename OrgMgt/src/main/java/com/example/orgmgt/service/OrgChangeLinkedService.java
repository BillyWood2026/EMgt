package com.example.orgmgt.service;

import java.util.Map;

/**
 * COMM-004: Organization Change Linked Service
 * Handles cross-module synchronization when organization changes occur
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
 * - StaffingQuota (编制管理)
 * - Permission (权限配置)
 */
public interface OrgChangeLinkedService {
    
    /**
     * Handles organization name change and propagates to all related modules
     * 
     * @param orgId Organization ID
     * @param oldOrgName Old organization name
     * @param newOrgName New organization name
     * @param operator Operator
     * @return Sync result with affected module counts
     */
    Map<String, Object> handleOrgNameChange(Long orgId, String oldOrgName, String newOrgName, String operator);
    
    /**
     * Handles organization code change and propagates to all related modules
     * 
     * @param orgId Organization ID
     * @param oldOrgCode Old organization code
     * @param newOrgCode New organization code
     * @param operator Operator
     * @return Sync result with affected module counts
     */
    Map<String, Object> handleOrgCodeChange(Long orgId, String oldOrgCode, String newOrgCode, String operator);
    
    /**
     * Handles organization status change and cascades to all related modules
     * 
     * @param orgId Organization ID
     * @param oldStatus Old status
     * @param newStatus New status
     * @param operator Operator
     * @return Sync result with affected module counts
     */
    Map<String, Object> handleOrgStatusChange(Long orgId, String oldStatus, String newStatus, String operator);
    
    /**
     * Handles organization migration and cascades to all related modules
     * 
     * @param orgId Organization ID
     * @param oldParentOrgId Old parent organization ID
     * @param newParentOrgId New parent organization ID
     * @param operator Operator
     * @return Sync result with affected module counts
     */
    Map<String, Object> handleOrgMigration(Long orgId, Long oldParentOrgId, Long newParentOrgId, String operator);
    
    /**
     * Handles organization deletion and cascades to all related modules
     * 
     * @param orgId Organization ID
     * @param operator Operator
     * @return Sync result with affected module counts
     */
    Map<String, Object> handleOrgDeletion(Long orgId, String operator);
    
    /**
     * Handles organization level change and propagates to position hierarchy
     * 
     * @param orgId Organization ID
     * @param oldOrgLevel Old organization level
     * @param newOrgLevel New organization level
     * @param operator Operator
     * @return Sync result with affected module counts
     */
    Map<String, Object> handleOrgLevelChange(Long orgId, Integer oldOrgLevel, Integer newOrgLevel, String operator);
    
    /**
     * Performs full synchronization of organization data across all modules
     * 
     * @param orgId Organization ID
     * @param operator Operator
     * @return Sync result
     */
    Map<String, Object> syncAllModules(Long orgId, String operator);
    
    /**
     * Validates if an organization change will cause conflicts in other modules
     * 
     * @param orgId Organization ID
     * @param changeType Type of change (NAME/CODE/STATUS/MIGRATE/DELETE/LEVEL)
     * @param changeValue New value
     * @return Validation result with potential conflicts
     */
    Map<String, Object> validateChangeImpact(Long orgId, String changeType, String changeValue);
}