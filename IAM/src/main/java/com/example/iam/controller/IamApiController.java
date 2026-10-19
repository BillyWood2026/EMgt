package com.example.iam.controller;

import com.example.iam.adapter.IamModulePermissionAdapter;
import com.example.iam.adapter.IamModulePermissionAdapter.PermissionSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * IAM API Controller for Module Integration
 * 
 * Provides REST API endpoints for microservices to access IAM services.
 * This controller is called by IamFeignClient.
 */
@RestController
@RequestMapping("/iam/api")
@RequiredArgsConstructor
public class IamApiController {
    
    private final IamModulePermissionAdapter permissionAdapter;
    
    @GetMapping("/permission/check")
    public boolean checkPermission(
            @RequestParam Long userId,
            @RequestParam String moduleCode,
            @RequestParam String permissionCode) {
        return permissionAdapter.checkPermission(userId, moduleCode, permissionCode);
    }
    
    @GetMapping("/permission/check-with-org")
    public boolean checkPermissionWithOrg(
            @RequestParam Long userId,
            @RequestParam String moduleCode,
            @RequestParam String permissionCode,
            @RequestParam Long orgId) {
        return permissionAdapter.checkPermissionWithOrg(userId, moduleCode, permissionCode, orgId);
    }
    
    @GetMapping("/permission/summary/{userId}")
    public PermissionSummary getPermissionSummary(@PathVariable Long userId) {
        return permissionAdapter.getPermissionSummary(userId);
    }
    
    @GetMapping("/permission/data-scope")
    public String getDataScopeFilter(
            @RequestParam Long userId,
            @RequestParam String moduleCode,
            @RequestParam String entityName) {
        return permissionAdapter.getDataScopeFilter(userId, moduleCode, entityName);
    }
    
    @GetMapping("/permission/column-permissions")
    public String getColumnPermissions(
            @RequestParam Long userId,
            @RequestParam String moduleCode,
            @RequestParam String entityName) {
        return permissionAdapter.getColumnPermissions(userId, moduleCode, entityName);
    }
    
    @GetMapping("/permission/can-access-record")
    public boolean canAccessRecord(
            @RequestParam Long userId,
            @RequestParam String moduleCode,
            @RequestParam String entityName,
            @RequestParam String recordId,
            @RequestParam Long recordOrgId) {
        return permissionAdapter.canAccessRecord(userId, moduleCode, entityName, recordId, recordOrgId);
    }
    
    @PostMapping("/permission/evaluate-condition")
    public boolean evaluateCondition(
            @RequestParam Long userId,
            @RequestBody String conditionExpression) {
        return permissionAdapter.evaluateCondition(userId, conditionExpression);
    }
}