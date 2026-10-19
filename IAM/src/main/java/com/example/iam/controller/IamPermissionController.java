package com.example.iam.controller;

import com.example.iam.dto.PermissionCheckDTO;
import com.example.iam.dto.PermissionCheckResultDTO;
import com.example.iam.dto.UserPermissionSummaryDTO;
import com.example.iam.service.IamPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * IAM Permission API Controller
 * 
 * Phase 1: Basic permission check endpoints
 * Phase 2: Data permission and expression endpoints
 * Phase 3: Audit and risk endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/iam")
@RequiredArgsConstructor
public class IamPermissionController {
    
    private final IamPermissionService permissionService;
    
    // ==================== Phase 1: RBAC Permission Check APIs ====================
    
    /**
     * Check if user has specific permission
     * 
     * POST /api/iam/permission/check
     */
    @PostMapping("/permission/check")
    public Result<PermissionCheckResultDTO> checkPermission(@RequestBody PermissionCheckDTO request) {
        log.debug("Checking permission: userId={}, permission={}", 
            request.getUserId(), request.getPermissionCode());
        
        PermissionCheckResultDTO result = permissionService.checkPermission(request);
        return Result.success(result);
    }
    
    /**
     * Check if user has any of the specified permissions
     * 
     * POST /api/iam/permission/check-any
     */
    @PostMapping("/permission/check-any")
    public Result<PermissionCheckResultDTO> checkAnyPermission(
            @RequestBody Map<String, Object> request) {
        
        PermissionCheckDTO dto = new PermissionCheckDTO();
        dto.setUserId(getLong(request.get("userId")));
        dto.setEmployeeId((String) request.get("employeeId"));
        dto.setModuleCode((String) request.get("moduleCode"));
        dto.setAction((String) request.get("action"));
        dto.setRequestIp((String) request.get("requestIp"));
        
        String[] permissions = ((java.util.List<String>) request.get("permissions"))
            .toArray(new String[0]);
        
        PermissionCheckResultDTO result = permissionService.checkAnyPermission(dto, permissions);
        return Result.success(result);
    }
    
    /**
     * Check if user has all of the specified permissions
     * 
     * POST /api/iam/permission/check-all
     */
    @PostMapping("/permission/check-all")
    public Result<PermissionCheckResultDTO> checkAllPermissions(
            @RequestBody Map<String, Object> request) {
        
        PermissionCheckDTO dto = new PermissionCheckDTO();
        dto.setUserId(getLong(request.get("userId")));
        dto.setEmployeeId((String) request.get("employeeId"));
        dto.setModuleCode((String) request.get("moduleCode"));
        dto.setAction((String) request.get("action"));
        
        String[] permissions = ((java.util.List<String>) request.get("permissions"))
            .toArray(new String[0]);
        
        PermissionCheckResultDTO result = permissionService.checkAllPermissions(dto, permissions);
        return Result.success(result);
    }
    
    /**
     * Get user permission summary
     * 
     * GET /api/iam/permission/summary/{userId}
     */
    @GetMapping("/permission/summary/{userId}")
    public Result<UserPermissionSummaryDTO> getUserPermissionSummary(@PathVariable Long userId) {
        UserPermissionSummaryDTO summary = permissionService.getUserPermissionSummary(userId);
        return Result.success(summary);
    }
    
    /**
     * Get user permission summary by employee ID
     * 
     * GET /api/iam/permission/summary-by-employee/{employeeId}
     */
    @GetMapping("/permission/summary-by-employee/{employeeId}")
    public Result<UserPermissionSummaryDTO> getUserPermissionSummaryByEmployeeId(
            @PathVariable String employeeId) {
        UserPermissionSummaryDTO summary = permissionService.getUserPermissionSummaryByEmployeeId(employeeId);
        return Result.success(summary);
    }
    
    // ==================== Phase 2: Data Permission APIs ====================
    
    /**
     * Get data scope filter for user and module
     * 
     * GET /api/iam/data-scope/{userId}/{moduleCode}
     */
    @GetMapping("/data-scope/{userId}/{moduleCode}")
    public Result<Map<String, Object>> getDataScope(
            @PathVariable Long userId,
            @PathVariable String moduleCode,
            @RequestParam(required = false) String entityName) {
        
        String filter = permissionService.getDataScopeFilter(userId, moduleCode, entityName);
        
        Map<String, Object> result = Map.of(
            "userId", userId,
            "moduleCode", moduleCode,
            "entityName", entityName != null ? entityName : "",
            "filter", filter != null ? filter : "",
            "filterType", "SQL_WHERE"
        );
        
        return Result.success(result);
    }
    
    /**
     * Get column permissions for user and module
     * 
     * GET /api/iam/column-permissions/{userId}/{moduleCode}
     */
    @GetMapping("/column-permissions/{userId}/{moduleCode}")
    public Result<Map<String, Object>> getColumnPermissions(
            @PathVariable Long userId,
            @PathVariable String moduleCode,
            @RequestParam(required = false) String entityName) {
        
        String permissions = permissionService.getColumnPermissions(userId, moduleCode, entityName);
        
        Map<String, Object> result = Map.of(
            "userId", userId,
            "moduleCode", moduleCode,
            "entityName", entityName != null ? entityName : "",
            "columnPermissions", permissions != null ? permissions : "{}"
        );
        
        return Result.success(result);
    }
    
    /**
     * Evaluate conditional permission expression
     * 
     * POST /api/iam/condition/evaluate
     */
    @PostMapping("/condition/evaluate")
    public Result<Map<String, Object>> evaluateCondition(@RequestBody Map<String, Object> request) {
        Long userId = getLong(request.get("userId"));
        String expression = (String) request.get("expression");
        
        boolean result = permissionService.evaluateConditionalPermission(userId, expression);
        
        return Result.success(Map.of(
            "userId", userId,
            "expression", expression,
            "result", result
        ));
    }
    
    /**
     * Get time restriction for user and module
     * 
     * GET /api/iam/time-restriction/{userId}/{moduleCode}
     */
    @GetMapping("/time-restriction/{userId}/{moduleCode}")
    public Result<Map<String, Object>> getTimeRestriction(
            @PathVariable Long userId,
            @PathVariable String moduleCode) {
        
        String restriction = permissionService.getTimeRestriction(userId, moduleCode);
        
        return Result.success(Map.of(
            "userId", userId,
            "moduleCode", moduleCode,
            "timeRestriction", restriction != null ? restriction : "{}"
        ));
    }
    
    // ==================== Phase 3: Audit & Risk APIs ====================
    
    /**
     * Get user risk score
     * 
     * GET /api/iam/risk/score/{userId}
     */
    @GetMapping("/risk/score/{userId}")
    public Result<Map<String, Object>> getUserRiskScore(@PathVariable Long userId) {
        Integer riskScore = permissionService.getUserRiskScore(userId);
        
        String riskLevel = riskScore < 30 ? "LOW" : 
                          riskScore < 60 ? "MEDIUM" : 
                          riskScore < 80 ? "HIGH" : "CRITICAL";
        
        return Result.success(Map.of(
            "userId", userId,
            "riskScore", riskScore,
            "riskLevel", riskLevel
        ));
    }
    
    /**
     * Check risk events for user action
     * 
     * POST /api/iam/risk/check
     */
    @PostMapping("/risk/check")
    public Result<Map<String, Object>> checkRiskEvents(@RequestBody Map<String, Object> request) {
        Long userId = getLong(request.get("userId"));
        String action = (String) request.get("action");
        String resourceType = (String) request.get("resourceType");
        
        boolean hasRisk = permissionService.checkRiskEvents(userId, action, resourceType);
        
        return Result.success(Map.of(
            "userId", userId,
            "action", action,
            "resourceType", resourceType,
            "hasRisk", hasRisk
        ));
    }
    
    // ==================== Helper Methods ====================
    
    private Long getLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        return null;
    }
    
    /**
     * Unified API result wrapper
     */
    @lombok.Data
    public static class Result<T> {
        private int code;
        private String message;
        private T data;
        private long timestamp;
        
        public static <T> Result<T> success(T data) {
            Result<T> result = new Result<>();
            result.setCode(200);
            result.setMessage("success");
            result.setData(data);
            result.setTimestamp(System.currentTimeMillis());
            return result;
        }
        
        public static <T> Result<T> fail(String message) {
            Result<T> result = new Result<>();
            result.setCode(500);
            result.setMessage(message);
            result.setTimestamp(System.currentTimeMillis());
            return result;
        }
    }
}
