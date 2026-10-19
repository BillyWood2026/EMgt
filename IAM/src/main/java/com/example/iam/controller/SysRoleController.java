package com.example.iam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.iam.entity.rbac.IamRole;
import com.example.iam.entity.rbac.IamRolePermission;
import com.example.iam.mapper.IamRoleMapper;
import com.example.iam.mapper.IamRolePermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final IamRoleMapper roleMapper;
    private final IamRolePermissionMapper rolePermissionMapper;

    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String status) {

        LambdaQueryWrapper<IamRole> wrapper = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like(IamRole::getRoleName, roleName);
        }
        if (roleCode != null && !roleCode.isEmpty()) {
            wrapper.like(IamRole::getRoleCode, roleCode);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(IamRole::getStatus, status.toUpperCase());
        }
        wrapper.orderByAsc(IamRole::getSortOrder);

        Page<IamRole> page = new Page<>(pageNum, pageSize);
        Page<IamRole> result = roleMapper.selectPage(page, wrapper);

        List<Map<String, Object>> list = result.getRecords().stream().map(role -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", role.getId());
            map.put("roleCode", role.getRoleCode());
            map.put("roleName", role.getRoleName());
            map.put("description", role.getDescription());
            map.put("roleCategory", role.getRoleCategory());
            map.put("roleType", role.getRoleType());
            map.put("status", role.getStatus());
            map.put("sortOrder", role.getSortOrder());
            map.put("isSystemBuiltIn", role.getIsSystemBuiltIn());
            map.put("createTime", role.getCreatedTime());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return buildSuccessResponse(data);
    }

    @GetMapping("/all")
    public Map<String, Object> getAll() {
        LambdaQueryWrapper<IamRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IamRole::getStatus, "ACTIVE");
        wrapper.orderByAsc(IamRole::getSortOrder);
        List<IamRole> roles = roleMapper.selectList(wrapper);
        List<Map<String, Object>> list = roles.stream().map(role -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", role.getId());
            map.put("name", role.getRoleName());
            map.put("code", role.getRoleCode());
            return map;
        }).collect(Collectors.toList());
        return buildSuccessResponse(list);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        IamRole role = roleMapper.selectById(id);
        if (role == null) {
            return buildErrorResponse("角色不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", role.getId());
        data.put("roleCode", role.getRoleCode());
        data.put("roleName", role.getRoleName());
        data.put("roleNameEn", role.getRoleNameEn());
        data.put("description", role.getDescription());
        data.put("roleCategory", role.getRoleCategory());
        data.put("roleType", role.getRoleType());
        data.put("orgScope", role.getOrgScope());
        data.put("status", role.getStatus());
        data.put("sortOrder", role.getSortOrder());
        data.put("canAssignUsers", role.getCanAssignUsers());
        data.put("canModifyPermissions", role.getCanModifyPermissions());
        data.put("canGrantRoles", role.getCanGrantRoles());
        return buildSuccessResponse(data);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> request) {
        String roleCode = (String) request.get("roleCode");
        String roleName = (String) request.get("roleName");
        String description = (String) request.get("description");

        LambdaQueryWrapper<IamRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IamRole::getRoleCode, roleCode);
        if (roleMapper.selectCount(wrapper) > 0) {
            return buildErrorResponse("角色编码已存在");
        }

        IamRole role = new IamRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setDescription(description);
        role.setRoleCategory("SYSTEM");
        role.setRoleType("MANAGEMENT");
        role.setStatus("ACTIVE");
        role.setSortOrder(0);
        role.setIsSystemBuiltIn(false);
        role.setIsSuperAdminRole(false);
        role.setIsPublicRole(false);
        role.setCreatedBy("SYSTEM");
        role.setCreatedTime(LocalDateTime.now());

        roleMapper.insert(role);
        log.info("Role created: {}", roleCode);
        return buildSuccessResponse("创建成功", null);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        IamRole role = roleMapper.selectById(id);
        if (role == null) {
            return buildErrorResponse("角色不存在");
        }

        if (Boolean.TRUE.equals(role.getIsSystemBuiltIn())) {
            return buildErrorResponse("系统内置角色不能修改");
        }

        if (request.containsKey("roleName")) {
            role.setRoleName((String) request.get("roleName"));
        }
        if (request.containsKey("description")) {
            role.setDescription((String) request.get("description"));
        }
        if (request.containsKey("status")) {
            role.setStatus(((String) request.get("status")).toUpperCase());
        }
        if (request.containsKey("sortOrder")) {
            role.setSortOrder((Integer) request.get("sortOrder"));
        }
        role.setUpdatedBy("SYSTEM");
        role.setUpdatedTime(LocalDateTime.now());

        roleMapper.updateById(role);
        return buildSuccessResponse("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        IamRole role = roleMapper.selectById(id);
        if (role == null) {
            return buildErrorResponse("角色不存在");
        }
        if (Boolean.TRUE.equals(role.getIsSystemBuiltIn())) {
            return buildErrorResponse("系统内置角色不能删除");
        }
        roleMapper.deleteById(id);
        return buildSuccessResponse("删除成功", null);
    }

    @GetMapping("/{id}/permissions")
    public Map<String, Object> getRolePermissions(@PathVariable Long id) {
        LambdaQueryWrapper<IamRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IamRolePermission::getRoleId, id);
        List<IamRolePermission> permissions = rolePermissionMapper.selectList(wrapper);
        List<Long> permissionIds = permissions.stream()
            .map(IamRolePermission::getPermissionId)
            .collect(Collectors.toList());
        return buildSuccessResponse(permissionIds);
    }

    @PutMapping("/{id}/permissions")
    public Map<String, Object> setRolePermissions(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        List<Long> permissionIds = (List<Long>) request.get("permissionIds");

        LambdaQueryWrapper<IamRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IamRolePermission::getRoleId, id);
        rolePermissionMapper.delete(wrapper);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permId : permissionIds) {
                IamRolePermission rp = new IamRolePermission();
                rp.setRoleId(id);
                rp.setPermissionId(permId);
                rp.setCreatedBy("SYSTEM");
                rp.setCreatedTime(LocalDateTime.now());
                rolePermissionMapper.insert(rp);
            }
        }

        return buildSuccessResponse("权限分配成功", null);
    }

    private Map<String, Object> buildSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> buildSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    private Map<String, Object> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", message);
        return response;
    }
}
