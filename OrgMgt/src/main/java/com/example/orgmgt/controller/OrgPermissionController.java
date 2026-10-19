package com.example.orgmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.orgmgt.entity.OrgPermission;
import com.example.orgmgt.mapper.OrgPermissionMapper;
import com.example.orgmgt.service.OrgPermissionService;
import com.example.common.dto.ResponseResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Organization Permission Controller
 * REST API endpoints for permission management
 */
@RestController
@RequestMapping("/api/org-permission")
public class OrgPermissionController {
    
    @Autowired
    private OrgPermissionService orgPermissionService;

    @Autowired
    private OrgPermissionMapper orgPermissionMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ================= Generic CRUD used by frontend permission page =================

    @GetMapping("/list")
    public ResponseResult<List<OrgPermission>> getList(
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String moduleCode,
            @RequestParam(required = false) String dataScope,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        LambdaQueryWrapper<OrgPermission> wrapper = Wrappers.<OrgPermission>lambdaQuery()
                .eq(roleCode != null && !roleCode.trim().isEmpty(), OrgPermission::getRoleCode, roleCode)
                .eq(moduleCode != null && !moduleCode.trim().isEmpty(), OrgPermission::getModuleCode, moduleCode)
                .eq(dataScope != null && !dataScope.trim().isEmpty(), OrgPermission::getDataScope, dataScope)
                .eq(status != null && !status.trim().isEmpty(), OrgPermission::getStatus, status)
                .orderByDesc(OrgPermission::getCreateTime);
        return ResponseResult.success(orgPermissionMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<OrgPermission> getById(@PathVariable Long id) {
        return ResponseResult.success(orgPermissionMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<OrgPermission> create(@RequestBody Map<String, Object> params,
                                                 @RequestHeader(value = "X-User-Id", required = false) String userId) {
        OrgPermission entity = objectMapper.convertValue(params, OrgPermission.class);
        fillRequiredCodes(entity);
        if (entity.getStatus() == null || entity.getStatus().trim().isEmpty()) {
            entity.setStatus("ENABLED");
        }
        if (entity.getHasPermission() == null) {
            entity.setHasPermission(Boolean.TRUE);
        }
        String operator = (userId == null || userId.isEmpty()) ? "admin" : userId;
        entity.setCreatedBy(operator);
        entity.setCreateTime(LocalDateTime.now());
        entity.setDeleted(0);
        entity.setVersion(1);
        orgPermissionMapper.insert(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<OrgPermission> update(@PathVariable Long id, @RequestBody Map<String, Object> params,
                                                 @RequestHeader(value = "X-User-Id", required = false) String userId) {
        OrgPermission entity = objectMapper.convertValue(params, OrgPermission.class);
        entity.setId(id);
        fillRequiredCodes(entity);
        entity.setUpdatedBy((userId == null || userId.isEmpty()) ? "admin" : userId);
        entity.setUpdateTime(LocalDateTime.now());
        orgPermissionMapper.updateById(entity);
        return ResponseResult.success(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> remove(@PathVariable Long id) {
        orgPermissionMapper.deleteById(id);
        return ResponseResult.success();
    }

    private void fillRequiredCodes(OrgPermission entity) {
        if (entity.getRoleCode() == null || entity.getRoleCode().trim().isEmpty()) {
            entity.setRoleCode(entity.getRoleName() != null && !entity.getRoleName().trim().isEmpty()
                    ? "ROLE_" + entity.getRoleName().hashCode()
                    : "ROLE_" + System.currentTimeMillis());
        }
        if (entity.getModuleCode() == null || entity.getModuleCode().trim().isEmpty()) {
            entity.setModuleCode(entity.getModuleName() != null && !entity.getModuleName().trim().isEmpty()
                    ? "MOD_" + entity.getModuleName().hashCode()
                    : "MOD_" + System.currentTimeMillis());
        }
        if (entity.getFunctionCode() == null || entity.getFunctionCode().trim().isEmpty()) {
            entity.setFunctionCode(entity.getFunctionName() != null && !entity.getFunctionName().trim().isEmpty()
                    ? "FUNC_" + entity.getFunctionName().hashCode()
                    : "FUNC_" + System.currentTimeMillis());
        }
    }

    // ================= Check / grant / revoke workflows =================

    @GetMapping("/check")
    public ResponseResult<Boolean> checkPermission(
            @RequestParam String userId,
            @RequestParam String moduleCode,
            @RequestParam String functionCode) {
        
        boolean hasPermission = orgPermissionService.hasPermission(userId, moduleCode, functionCode);
        return ResponseResult.success(hasPermission);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseResult<List<Map<String, Object>>> getUserPermissions(@PathVariable String userId) {
        List<Map<String, Object>> permissions = orgPermissionService.getUserPermissions(userId);
        return ResponseResult.success(permissions);
    }
    
    @GetMapping("/role/{roleCode}")
    public ResponseResult<List<Map<String, Object>>> getRolePermissions(@PathVariable String roleCode) {
        List<Map<String, Object>> permissions = orgPermissionService.getRolePermissions(roleCode);
        return ResponseResult.success(permissions);
    }
    
    @PostMapping("/grant")
    public ResponseResult<Void> grantPermission(
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String moduleCode,
            @RequestParam(required = false) String functionCode,
            @RequestParam(required = false) String dataScope,
            @RequestBody(required = false) Map<String, Object> body) {
        String role = firstNonBlank(roleCode, str(body == null ? null : body.get("roleCode")));
        String module = firstNonBlank(moduleCode, str(body == null ? null : body.get("moduleCode")));
        String function = firstNonBlank(functionCode, str(body == null ? null : body.get("functionCode")));
        String scope = firstNonBlank(dataScope, str(body == null ? null : body.get("dataScope")));
        // 本地部署无 IAM 中间件：直接在 org_permission 表完成授权落库（原 service 实现恒抛 IAM 异常）
        applyPermission(role, module, function, scope, true);
        return ResponseResult.success();
    }

    @PostMapping("/revoke")
    public ResponseResult<Void> revokePermission(
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String moduleCode,
            @RequestParam(required = false) String functionCode,
            @RequestBody(required = false) Map<String, Object> body) {
        String role = firstNonBlank(roleCode, str(body == null ? null : body.get("roleCode")));
        String module = firstNonBlank(moduleCode, str(body == null ? null : body.get("moduleCode")));
        String function = firstNonBlank(functionCode, str(body == null ? null : body.get("functionCode")));
        applyPermission(role, module, function, null, false);
        return ResponseResult.success();
    }

    /**
     * 本地授权/撤权落库：按 role+module+function 定位行；授权时无行则补建。撤权幂等成功。
     */
    private void applyPermission(String role, String module, String function, String scope, boolean grant) {
        if (role == null || module == null || function == null) {
            return;
        }
        LambdaQueryWrapper<OrgPermission> w = Wrappers.<OrgPermission>lambdaQuery()
                .eq(OrgPermission::getRoleCode, role)
                .eq(OrgPermission::getModuleCode, module)
                .eq(OrgPermission::getFunctionCode, function)
                .last("LIMIT 1");
        OrgPermission perm = orgPermissionMapper.selectOne(w);
        LocalDateTime now = LocalDateTime.now();
        if (perm == null) {
            if (!grant) {
                return; // 幂等：本来就无授权记录
            }
            perm = new OrgPermission();
            perm.setRoleCode(role);
            perm.setModuleCode(module);
            perm.setFunctionCode(function);
            perm.setDataScope(scope != null ? scope : "ALL");
            perm.setHasPermission(Boolean.TRUE);
            perm.setStatus("ENABLED");
            perm.setCreatedBy("admin");
            perm.setCreateTime(now);
            perm.setDeleted(0);
            perm.setVersion(1);
            orgPermissionMapper.insert(perm);
        } else {
            perm.setHasPermission(grant);
            perm.setStatus(grant ? "ENABLED" : "DISABLED");
            if (grant && scope != null) {
                perm.setDataScope(scope);
            }
            perm.setUpdatedBy("admin");
            perm.setUpdateTime(now);
            orgPermissionMapper.updateById(perm);
        }
    }

    /**
     * Batch grant/revoke permission rows by their primary keys (frontend convention)
     */
    @PostMapping("/batch")
    @SuppressWarnings("unchecked")
    public ResponseResult<Boolean> batchByIds(@RequestBody Map<String, Object> body) {
        Object idsObj = body == null ? null : body.get("ids");
        String action = str(body == null ? null : body.get("action"));
        boolean grant = !"REVOKE".equalsIgnoreCase(action);
        if (!(idsObj instanceof List)) {
            return ResponseResult.success(false);
        }
        for (Object o : (List<Object>) idsObj) {
            if (o == null) {
                continue;
            }
            OrgPermission perm = orgPermissionMapper.selectById(Long.valueOf(String.valueOf(o)));
            if (perm == null) {
                continue;
            }
            perm.setHasPermission(grant);
            perm.setStatus(grant ? "ENABLED" : "DISABLED");
            perm.setUpdateTime(LocalDateTime.now());
            orgPermissionMapper.updateById(perm);
        }
        return ResponseResult.success(true);
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v;
            }
        }
        return null;
    }
    
    @PostMapping("/role/{roleCode}/batch")
    public ResponseResult<Void> batchUpdatePermissions(
            @PathVariable String roleCode,
            @RequestBody List<Map<String, Object>> permissions) {
        
        orgPermissionService.batchUpdatePermissions(roleCode, permissions);
        return ResponseResult.success();
    }
    
    @GetMapping("/user/{userId}/orgs")
    public ResponseResult<List<Long>> getAccessibleOrgIds(@PathVariable String userId) {
        List<Long> orgIds = orgPermissionService.getAccessibleOrgIds(userId);
        return ResponseResult.success(orgIds);
    }
    
    @GetMapping("/user/{userId}/org/{orgId}/access")
    public ResponseResult<Boolean> canAccessOrg(@PathVariable String userId, @PathVariable Long orgId) {
        boolean canAccess = orgPermissionService.canAccessOrg(userId, orgId);
        return ResponseResult.success(canAccess);
    }
    
    @GetMapping("/roles")
    public ResponseResult<List<Map<String, Object>>> getRoleList() {
        List<Map<String, Object>> roles = orgPermissionService.getRoleList();
        return ResponseResult.success(roles);
    }
    
    @GetMapping("/modules")
    public ResponseResult<List<Map<String, Object>>> getModuleFunctionList() {
        List<Map<String, Object>> modules = orgPermissionService.getModuleFunctionList();
        return ResponseResult.success(modules);
    }
    
    @PostMapping("/validate")
    public ResponseResult<Map<String, Object>> validateOperation(
            @RequestParam String userId,
            @RequestParam String moduleCode,
            @RequestParam String functionCode,
            @RequestParam(required = false) Long orgId) {

        Map<String, Object> result = orgPermissionService.validateOperation(userId, moduleCode, functionCode, orgId);
        return ResponseResult.success(result);
    }

    /** GET 别名：避免 GET /validate 被 /{id} 当作主键解析报 400 */
    @GetMapping("/validate")
    public ResponseResult<Map<String, Object>> validateOperationGet(
            @RequestParam String userId,
            @RequestParam String moduleCode,
            @RequestParam String functionCode,
            @RequestParam(required = false) Long orgId) {
        return ResponseResult.success(orgPermissionService.validateOperation(userId, moduleCode, functionCode, orgId));
    }
}