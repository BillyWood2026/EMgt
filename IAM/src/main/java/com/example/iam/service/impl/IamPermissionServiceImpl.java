package com.example.iam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.iam.dto.PermissionCheckDTO;
import com.example.iam.dto.PermissionCheckResultDTO;
import com.example.iam.dto.UserPermissionSummaryDTO;
import com.example.iam.engine.PermissionExpressionEngine;
import com.example.iam.engine.PermissionExpressionEngine.EvaluationResult;
import com.example.iam.engine.PermissionExpressionEngine.PermissionContext;
import com.example.iam.entity.expression.IamDataPermissionMatrix;
import com.example.iam.entity.rbac.*;
import com.example.iam.entity.user.IamUser;
import com.example.iam.mapper.*;
import com.example.iam.service.IamPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * IAM Permission Service Implementation
 * 
 * Phase 1: Basic RBAC permission checking
 * Phase 2: Data permission with expression engine
 * Phase 3: Audit logging and risk monitoring
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IamPermissionServiceImpl implements IamPermissionService {
    
    private final IamUserMapper userMapper;
    private final IamRoleMapper roleMapper;
    private final IamPermissionMapper permissionMapper;
    private final IamUserRoleMapper userRoleMapper;
    private final IamRolePermissionMapper rolePermissionMapper;
    private final IamDataPermissionMatrixMapper dataPermissionMatrixMapper;
    private final PermissionExpressionEngine expressionEngine;
    
    // ==================== Phase 1: RBAC Basic Permissions ====================
    
    @Override
    @Transactional(readOnly = true)
    public PermissionCheckResultDTO checkPermission(PermissionCheckDTO request) {
        long startTime = System.currentTimeMillis();
        PermissionCheckResultDTO result = new PermissionCheckResultDTO();
        
        try {
            // Build permission context
            PermissionContext context = buildPermissionContext(request);
            
            // Get user's effective permissions
            Set<String> effectivePermissions = getEffectivePermissions(context.getUserId(), context.getOrgId());
            
            // Check direct permission
            result.setAllowed(effectivePermissions.contains(request.getPermissionCode()));
            result.setPermissionCode(request.getPermissionCode());
            result.setGrantedPermissions(effectivePermissions.toArray(new String[0]));
            
            if (!result.isAllowed()) {
                result.setReason("User does not have permission: " + request.getPermissionCode());
            }
            
            // Evaluate risk
            result.setRiskLevel(evaluateRiskLevel(context));
            
        } catch (Exception e) {
            log.error("Error checking permission", e);
            result.setAllowed(false);
            result.setReason("Error: " + e.getMessage());
            result.setRiskLevel("HIGH");
        }
        
        result.setEvaluationTimeMs(System.currentTimeMillis() - startTime);
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public PermissionCheckResultDTO checkAnyPermission(PermissionCheckDTO request, String[] permissionCodes) {
        PermissionCheckResultDTO result = checkPermission(request);
        
        if (result.isAllowed()) {
            return result;
        }
        
        // Check if user has any of the permissions
        Set<String> effectivePermissions = getEffectivePermissions(
            request.getUserId(), request.getOrgId());
        
        for (String code : permissionCodes) {
            if (effectivePermissions.contains(code)) {
                result.setAllowed(true);
                result.setReason(null);
                break;
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public PermissionCheckResultDTO checkAllPermissions(PermissionCheckDTO request, String[] permissionCodes) {
        PermissionCheckResultDTO result = checkPermission(request);
        
        if (!result.isAllowed()) {
            return result;
        }
        
        // Check if user has all permissions
        Set<String> effectivePermissions = getEffectivePermissions(
            request.getUserId(), request.getOrgId());
        
        for (String code : permissionCodes) {
            if (!effectivePermissions.contains(code)) {
                result.setAllowed(false);
                result.setReason("Missing required permission: " + code);
                break;
            }
        }
        
        return result;
    }
    
    @Override
    @Cacheable(value = "userPermissions", key = "#userId")
    public UserPermissionSummaryDTO getUserPermissionSummary(Long userId) {
        UserPermissionSummaryDTO summary = new UserPermissionSummaryDTO();
        
        // Get user info
        IamUser user = userMapper.selectById(userId);
        if (user == null) {
            return summary;
        }
        
        summary.setUserId(userId);
        summary.setEmployeeId(user.getEmployeeId());
        summary.setUsername(user.getUsername());
        summary.setDataPermissionLevel(user.getDataPermissionLevel());
        
        // Get direct roles
        List<IamUserRole> directRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<IamUserRole>()
                .eq(IamUserRole::getUserId, userId)
                .eq(IamUserRole::getStatus, "ACTIVE")
        );
        
        summary.setDirectRoles(directRoles.stream()
            .map(this::mapToRoleAssignment)
            .collect(Collectors.toList()));
        
        // Get effective roles (including inherited from org)
        List<IamUserRole> allRoles = new ArrayList<>(directRoles);
        // TODO: Add inherited roles from organization
        
        summary.setEffectiveRoles(allRoles.stream()
            .map(this::mapToRoleAssignment)
            .collect(Collectors.toList()));
        
        // Get direct permissions
        Set<String> directPerms = new HashSet<>();
        for (IamUserRole ur : directRoles) {
            List<IamRolePermission> perms = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<IamRolePermission>()
                    .eq(IamRolePermission::getRoleId, ur.getRoleId())
                    .eq(IamRolePermission::getGrantType, "GRANT")
                    .eq(IamRolePermission::getStatus, "ACTIVE")
            );
            perms.forEach(p -> directPerms.add(p.getPermissionCode()));
        }
        summary.setDirectPermissions(new ArrayList<>(directPerms));
        
        // Get effective permissions (including inherited)
        summary.setEffectivePermissions(new ArrayList<>(getEffectivePermissions(userId, user.getDefaultOrgId())));
        
        // Build module permission map
        summary.setModulePermissions(buildModulePermissionMap(summary.getEffectivePermissions()));
        
        return summary;
    }
    
    @Override
    public UserPermissionSummaryDTO getUserPermissionSummaryByEmployeeId(String employeeId) {
        IamUser user = userMapper.selectOne(
            new LambdaQueryWrapper<IamUser>()
                .eq(IamUser::getEmployeeId, employeeId)
        );
        if (user == null) {
            return new UserPermissionSummaryDTO();
        }
        return getUserPermissionSummary(user.getId());
    }
    
    // ==================== Phase 2: Data Permissions ====================
    
    @Override
    @Transactional(readOnly = true)
    public String getDataScopeFilter(Long userId, String moduleCode, String entityName) {
        PermissionContext context = buildContextFromUser(userId);
        context.setModuleCode(moduleCode);
        
        // Get user's data permission matrices
        List<IamDataPermissionMatrix> matrices = getApplicableMatrices(userId, moduleCode, entityName);
        
        if (matrices.isEmpty()) {
            return null;
        }
        
        // Combine matrix expressions
        List<String> expressions = matrices.stream()
            .map(IamDataPermissionMatrix::getRowFilterExpression)
            .filter(expr -> expr != null && !expr.isBlank())
            .collect(Collectors.toList());
        
        if (expressions.isEmpty()) {
            return null;
        }
        
        // Evaluate and combine expressions
        StringBuilder combinedFilter = new StringBuilder();
        for (String expr : expressions) {
            String result = expressionEngine.evaluateDataScope(expr, context);
            if (result != null && !result.isBlank()) {
                if (combinedFilter.length() > 0) {
                    combinedFilter.append(" AND ");
                }
                combinedFilter.append("(").append(result).append(")");
            }
        }
        
        return combinedFilter.length() > 0 ? combinedFilter.toString() : null;
    }
    
    @Override
    @Transactional(readOnly = true)
    public String getColumnPermissions(Long userId, String moduleCode, String entityName) {
        List<IamDataPermissionMatrix> matrices = getApplicableMatrices(userId, moduleCode, entityName);
        
        if (matrices.isEmpty()) {
            return null;
        }
        
        // Merge column permissions from all applicable matrices
        Map<String, String> mergedPermissions = new HashMap<>();
        for (IamDataPermissionMatrix matrix : matrices) {
            if (matrix.getColumnPermissions() != null) {
                // Parse and merge JSON
                // Simplified - actual implementation would parse JSON
            }
        }
        
        return matrices.get(0).getColumnPermissions();
    }
    
    @Override
    @Transactional(readOnly = true)
    public String getRowFilterExpression(Long userId, String moduleCode, String entityName) {
        return getDataScopeFilter(userId, moduleCode, entityName);
    }
    
    @Override
    public boolean evaluateConditionalPermission(Long userId, String conditionExpression) {
        PermissionContext context = buildContextFromUser(userId);
        EvaluationResult result = expressionEngine.evaluate(conditionExpression, context);
        return result.isAllowed();
    }
    
    @Override
    @Transactional(readOnly = true)
    public String getTimeRestriction(Long userId, String moduleCode) {
        List<IamDataPermissionMatrix> matrices = getApplicableMatrices(userId, moduleCode, null);
        
        return matrices.stream()
            .map(IamDataPermissionMatrix::getTimeRangeRestriction)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }
    
    // ==================== Phase 3: Audit & Risk ====================
    
    @Override
    public void recordPermissionAccess(PermissionCheckDTO request, PermissionCheckResultDTO result) {
        // TODO: Implement audit logging
        log.info("Permission access recorded: userId={}, permission={}, allowed={}", 
            request.getUserId(), request.getPermissionCode(), result.isAllowed());
    }
    
    @Override
    public boolean checkRiskEvents(Long userId, String action, String resourceType) {
        // TODO: Implement risk event checking
        return false;
    }
    
    @Override
    public Integer getUserRiskScore(Long userId) {
        // TODO: Implement risk score calculation
        return 0;
    }
    
    // ==================== Helper Methods ====================
    
    private PermissionContext buildPermissionContext(PermissionCheckDTO request) {
        PermissionContext context = new PermissionContext();
        context.setUserId(request.getUserId());
        context.setOrgId(request.getOrgId());
        context.setModuleCode(request.getModuleCode());
        context.setAction(request.getAction());
        context.setResourceType(request.getResourceType());
        context.setResourceId(request.getResourceId());
        context.setRequestIp(request.getRequestIp());
        
        if (request.getAttributes() != null) {
            context.setAttributes(request.getAttributes());
        }
        
        // Load user context if userId is provided
        if (request.getUserId() != null) {
            buildContextFromUser(request.getUserId(), context);
        } else if (request.getEmployeeId() != null) {
            buildContextFromEmployee(request.getEmployeeId(), context);
        }
        
        return context;
    }
    
    private PermissionContext buildContextFromUser(Long userId) {
        PermissionContext context = new PermissionContext();
        context.setUserId(userId);
        buildContextFromUser(userId, context);
        return context;
    }
    
    private void buildContextFromUser(Long userId, PermissionContext context) {
        IamUser user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        
        context.setEmployeeId(user.getEmployeeId());
        context.setUsername(user.getUsername());
        context.setOrgId(user.getDefaultOrgId());
        context.setDataPermissionLevel(user.getDataPermissionLevel());
        context.setCostCenter(user.getCostCenter());
        context.setManagerEmployeeId(user.getManagerEmployeeId());
    }
    
    private void buildContextFromEmployee(String employeeId, PermissionContext context) {
        IamUser user = userMapper.selectOne(
            new LambdaQueryWrapper<IamUser>()
                .eq(IamUser::getEmployeeId, employeeId)
        );
        if (user != null) {
            context.setUserId(user.getId());
            context.setEmployeeId(user.getEmployeeId());
            context.setUsername(user.getUsername());
            context.setOrgId(user.getDefaultOrgId());
            context.setDataPermissionLevel(user.getDataPermissionLevel());
        }
    }
    
    private Set<String> getEffectivePermissions(Long userId, Long orgId) {
        Set<String> permissions = new HashSet<>();
        
        // Get direct role permissions
        List<IamUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<IamUserRole>()
                .eq(IamUserRole::getUserId, userId)
                .eq(IamUserRole::getStatus, "ACTIVE")
        );
        
        for (IamUserRole ur : userRoles) {
            List<IamRolePermission> rolePerms = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<IamRolePermission>()
                    .eq(IamRolePermission::getRoleId, ur.getRoleId())
                    .eq(IamRolePermission::getGrantType, "GRANT")
                    .eq(IamRolePermission::getStatus, "ACTIVE")
            );
            
            for (IamRolePermission rp : rolePerms) {
                permissions.add(rp.getPermissionCode());
            }
        }
        
        // TODO: Add inherited permissions from org roles
        
        return permissions;
    }
    
    private List<IamDataPermissionMatrix> getApplicableMatrices(Long userId, String moduleCode, String entityName) {
        // Get user's effective role IDs
        List<IamUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<IamUserRole>()
                .eq(IamUserRole::getUserId, userId)
                .eq(IamUserRole::getStatus, "ACTIVE")
        );
        
        List<Long> roleIds = userRoles.stream()
            .map(IamUserRole::getRoleId)
            .collect(Collectors.toList());
        
        // Query matrices matching user's roles or global matrices
        QueryWrapper<IamDataPermissionMatrix> query = new QueryWrapper<>();
        query.and(w -> w
            .eq("module_code", moduleCode)
            .or()
            .isNull("module_code")
        );
        
        if (entityName != null) {
            query.and(w -> w
                .eq("entity_name", entityName)
                .or()
                .isNull("entity_name")
            );
        }
        
        query.and(w -> w
            .in("role_id", roleIds)
            .or()
            .isNull("role_id")
        );
        
        return dataPermissionMatrixMapper.selectList(query);
    }
    
    private UserPermissionSummaryDTO.RoleAssignmentDTO mapToRoleAssignment(IamUserRole ur) {
        UserPermissionSummaryDTO.RoleAssignmentDTO dto = new UserPermissionSummaryDTO.RoleAssignmentDTO();
        dto.setRoleId(ur.getRoleId());
        dto.setRoleCode(ur.getRoleCode());
        dto.setScopeType(ur.getScopeType());
        dto.setScopeOrgId(ur.getScopeOrgId());
        dto.setStartDate(ur.getStartDate());
        dto.setEndDate(ur.getEndDate());
        dto.setStatus(ur.getStatus());
        dto.setAssignmentType(ur.getAssignmentType());
        
        // Get role name
        IamRole role = roleMapper.selectById(ur.getRoleId());
        if (role != null) {
            dto.setRoleName(role.getRoleName());
        }
        
        return dto;
    }
    
    private Map<String, List<String>> buildModulePermissionMap(List<String> permissions) {
        Map<String, List<String>> moduleMap = new HashMap<>();
        
        for (String perm : permissions) {
            // Parse permission code: MODULE:SUB_MODULE:ACTION
            String[] parts = perm.split(":");
            String moduleCode = parts.length > 0 ? parts[0] : "UNKNOWN";
            
            moduleMap.computeIfAbsent(moduleCode, k -> new ArrayList<>()).add(perm);
        }
        
        return moduleMap;
    }
    
    private String evaluateRiskLevel(PermissionContext context) {
        // Simple risk evaluation - can be enhanced
        if (context.getDataPermissionLevel() != null && context.getDataPermissionLevel() > 7) {
            return "HIGH";
        }
        return "LOW";
    }
}
