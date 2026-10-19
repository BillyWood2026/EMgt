package com.example.iam.adapter.impl;

import com.example.iam.adapter.IamModulePermissionAdapter;
import com.example.iam.dto.PermissionCheckDTO;
import com.example.iam.dto.PermissionCheckResultDTO;
import com.example.iam.dto.UserPermissionSummaryDTO;
import com.example.iam.service.IamPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * IAM Module Permission Adapter Implementation
 * 
 * Wraps IamPermissionService to implement IamModulePermissionAdapter interface.
 * This adapter provides a simplified, module-friendly API for permission checking.
 * 
 * Usage:
 * - For modules in the same JVM: Autowire IamModulePermissionAdapter directly
 * - For microservices: Use IamModuleFeignClient
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamModulePermissionAdapterImpl implements IamModulePermissionAdapter {
    
    private final IamPermissionService permissionService;
    
    @Override
    public boolean checkPermission(Long userId, String moduleCode, String permissionCode) {
        try {
            PermissionCheckDTO request = new PermissionCheckDTO();
            request.setUserId(userId);
            request.setModuleCode(moduleCode);
            request.setPermissionCode(permissionCode);
            request.setAction(permissionCode); // Use permission code as action
            
            PermissionCheckResultDTO result = permissionService.checkPermission(request);
            return result.isAllowed();
        } catch (Exception e) {
            log.error("Permission check failed: userId={}, module={}, permission={}", 
                userId, moduleCode, permissionCode, e);
            return false;
        }
    }
    
    @Override
    public boolean checkPermissionWithOrg(Long userId, String moduleCode, String permissionCode, Long orgId) {
        try {
            PermissionCheckDTO request = new PermissionCheckDTO();
            request.setUserId(userId);
            request.setModuleCode(moduleCode);
            request.setPermissionCode(permissionCode);
            request.setOrgId(orgId);
            request.setAction(permissionCode);
            
            PermissionCheckResultDTO result = permissionService.checkPermission(request);
            return result.isAllowed();
        } catch (Exception e) {
            log.error("Permission check with org failed: userId={}, module={}, permission={}, orgId={}", 
                userId, moduleCode, permissionCode, orgId, e);
            return false;
        }
    }
    
    @Override
    public PermissionSummary getPermissionSummary(Long userId) {
        try {
            UserPermissionSummaryDTO summaryDTO = permissionService.getUserPermissionSummary(userId);
            
            PermissionSummary summary = new PermissionSummary();
            summary.setUserId(summaryDTO.getUserId());
            summary.setEmployeeId(summaryDTO.getEmployeeId());
            summary.setDirectRoles(summaryDTO.getDirectRoles().stream()
                    .map(UserPermissionSummaryDTO.RoleAssignmentDTO::getRoleCode)
                    .toArray(String[]::new));
            summary.setEffectiveRoles(summaryDTO.getEffectiveRoles().stream()
                    .map(UserPermissionSummaryDTO.RoleAssignmentDTO::getRoleCode)
                    .toArray(String[]::new));
            summary.setDirectPermissions(summaryDTO.getDirectPermissions().toArray(new String[0]));
            summary.setEffectivePermissions(summaryDTO.getEffectivePermissions().toArray(new String[0]));
            summary.setDataPermissionLevel(summaryDTO.getDataPermissionLevel());
            summary.setOrgScope(summaryDTO.getOrgScope());
            summary.setPrimaryOrgId(summaryDTO.getPrimaryOrgId());
            
            return summary;
        } catch (Exception e) {
            log.error("Failed to get permission summary: userId={}", userId, e);
            PermissionSummary emptySummary = new PermissionSummary();
            emptySummary.setUserId(userId);
            return emptySummary;
        }
    }
    
    @Override
    public String getDataScopeFilter(Long userId, String moduleCode, String entityName) {
        try {
            return permissionService.getDataScopeFilter(userId, moduleCode, entityName);
        } catch (Exception e) {
            log.error("Failed to get data scope filter: userId={}, module={}, entity={}", 
                userId, moduleCode, entityName, e);
            return null;
        }
    }
    
    @Override
    public String getColumnPermissions(Long userId, String moduleCode, String entityName) {
        try {
            return permissionService.getColumnPermissions(userId, moduleCode, entityName);
        } catch (Exception e) {
            log.error("Failed to get column permissions: userId={}, module={}, entity={}", 
                userId, moduleCode, entityName, e);
            return null;
        }
    }
    
    @Override
    public boolean canAccessRecord(Long userId, String moduleCode, String entityName, 
                                   String recordId, Long recordOrgId) {
        try {
            // Get user's org scope
            PermissionSummary summary = getPermissionSummary(userId);
            
            // ADMIN can access all
            for (String role : summary.getEffectiveRoles()) {
                if ("SUPER_ADMIN".equals(role) || "SYSTEM_ADMIN".equals(role)) {
                    return true;
                }
            }
            
            // PERSONAL scope can only access own records
            if ("PERSONAL".equals(summary.getOrgScope())) {
                return recordId != null && recordId.equals(userId.toString());
            }
            
            // OWN_DEPT scope - would need to check if record's org is in user's dept hierarchy
            if ("OWN_DEPT".equals(summary.getOrgScope())) {
                // TODO: Implement dept hierarchy check via Org service
                return true; // Placeholder
            }
            
            // GLOBAL can access all
            if ("GLOBAL".equals(summary.getOrgScope())) {
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("Failed to check record access: userId={}, recordId={}", userId, recordId, e);
            return false;
        }
    }
    
    @Override
    public boolean evaluateCondition(Long userId, String conditionExpression) {
        try {
            return permissionService.evaluateConditionalPermission(userId, conditionExpression);
        } catch (Exception e) {
            log.error("Failed to evaluate condition: userId={}, expression={}", 
                userId, conditionExpression, e);
            return false;
        }
    }
    
    @Override
    public Integer getUserRiskScore(Long userId) {
        // TODO: Integrate with risk monitoring service
        return 0;
    }
    
    @Override
    public boolean checkRiskAlert(Long userId, String moduleCode, String permissionCode, String resourceType) {
        // TODO: Integrate with risk monitoring service
        return false;
    }
    
    @Override
    public void recordAccess(Long userId, String moduleCode, String permissionCode,
                            String resourceType, String resourceId, String result) {
        try {
            PermissionCheckDTO request = new PermissionCheckDTO();
            request.setUserId(userId);
            request.setModuleCode(moduleCode);
            request.setPermissionCode(permissionCode);
            request.setResourceType(resourceType);
            request.setResourceId(resourceId);
            
            PermissionCheckResultDTO checkResult = new PermissionCheckResultDTO();
            checkResult.setAllowed("ALLOWED".equals(result));
            checkResult.setDecision(result);
            
            permissionService.recordPermissionAccess(request, checkResult);
        } catch (Exception e) {
            log.error("Failed to record access: userId={}, module={}, permission={}", 
                userId, moduleCode, permissionCode, e);
        }
    }
}
