package com.example.iam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.iam.entity.rbac.IamPermission;
import com.example.iam.mapper.IamPermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final IamPermissionMapper permissionMapper;

    @GetMapping("/tree")
    public Map<String, Object> getMenuTree() {
        LambdaQueryWrapper<IamPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IamPermission::getPermissionType, "MENU");
        wrapper.eq(IamPermission::getStatus, "ACTIVE");
        wrapper.orderByAsc(IamPermission::getSortOrder);

        List<IamPermission> permissions = permissionMapper.selectList(wrapper);
        List<Map<String, Object>> tree = buildTree(permissions);
        return buildSuccessResponse(tree);
    }

    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(required = false) String permissionName,
            @RequestParam(required = false) String permissionType,
            @RequestParam(required = false) String status) {

        LambdaQueryWrapper<IamPermission> wrapper = new LambdaQueryWrapper<>();
        if (permissionName != null && !permissionName.isEmpty()) {
            wrapper.like(IamPermission::getPermissionName, permissionName);
        }
        if (permissionType != null && !permissionType.isEmpty()) {
            wrapper.eq(IamPermission::getPermissionType, permissionType.toUpperCase());
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(IamPermission::getStatus, status.toUpperCase());
        }
        wrapper.orderByAsc(IamPermission::getSortOrder);

        List<IamPermission> permissions = permissionMapper.selectList(wrapper);
        List<Map<String, Object>> list = permissions.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("permissionCode", p.getPermissionCode());
            map.put("permissionName", p.getPermissionName());
            map.put("permissionType", p.getPermissionType());
            map.put("parentId", p.getParentId());
            map.put("icon", p.getIcon());
            map.put("routePath", p.getRoutePath());
            map.put("componentPath", p.getComponentPath());
            map.put("moduleCode", p.getModuleCode());
            map.put("sortOrder", p.getSortOrder());
            map.put("status", p.getStatus());
            map.put("action", p.getAction());
            map.put("createTime", p.getCreatedTime());
            return map;
        }).collect(Collectors.toList());

        return buildSuccessResponse(list);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        IamPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            return buildErrorResponse("菜单不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", permission.getId());
        data.put("permissionCode", permission.getPermissionCode());
        data.put("permissionName", permission.getPermissionName());
        data.put("permissionType", permission.getPermissionType());
        data.put("parentId", permission.getParentId());
        data.put("icon", permission.getIcon());
        data.put("routePath", permission.getRoutePath());
        data.put("componentPath", permission.getComponentPath());
        data.put("moduleCode", permission.getModuleCode());
        data.put("sortOrder", permission.getSortOrder());
        data.put("status", permission.getStatus());
        data.put("action", permission.getAction());
        data.put("description", permission.getDescription());
        data.put("resourceType", permission.getResourceType());
        data.put("resourceCode", permission.getResourceCode());
        return buildSuccessResponse(data);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> request) {
        String permissionCode = (String) request.get("permissionCode");
        String permissionName = (String) request.get("permissionName");
        String permissionType = (String) request.get("permissionType");

        LambdaQueryWrapper<IamPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IamPermission::getPermissionCode, permissionCode);
        if (permissionMapper.selectCount(wrapper) > 0) {
            return buildErrorResponse("权限编码已存在");
        }

        IamPermission permission = new IamPermission();
        permission.setPermissionCode(permissionCode);
        permission.setPermissionName(permissionName);
        permission.setPermissionType(permissionType != null ? permissionType.toUpperCase() : "MENU");
        permission.setStatus("ACTIVE");
        permission.setSortOrder(0);
        permission.setIsSystemBuiltIn(false);
        permission.setIsDangerous(false);
        permission.setRequiresConfirmation(false);
        permission.setCreatedBy("SYSTEM");
        permission.setCreatedTime(LocalDateTime.now());

        if (request.containsKey("parentId") && request.get("parentId") != null) {
            permission.setParentId(Long.valueOf(request.get("parentId").toString()));
        }
        if (request.containsKey("icon")) {
            permission.setIcon((String) request.get("icon"));
        }
        if (request.containsKey("routePath")) {
            permission.setRoutePath((String) request.get("routePath"));
        }
        if (request.containsKey("componentPath")) {
            permission.setComponentPath((String) request.get("componentPath"));
        }
        if (request.containsKey("moduleCode")) {
            permission.setModuleCode((String) request.get("moduleCode"));
        }
        if (request.containsKey("sortOrder")) {
            permission.setSortOrder((Integer) request.get("sortOrder"));
        }
        if (request.containsKey("action")) {
            permission.setAction((String) request.get("action"));
        }
        if (request.containsKey("description")) {
            permission.setDescription((String) request.get("description"));
        }

        permissionMapper.insert(permission);
        log.info("Menu created: {}", permissionCode);
        return buildSuccessResponse("创建成功", null);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        IamPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            return buildErrorResponse("菜单不存在");
        }

        if (Boolean.TRUE.equals(permission.getIsSystemBuiltIn())) {
            return buildErrorResponse("系统内置菜单不能修改");
        }

        if (request.containsKey("permissionName")) {
            permission.setPermissionName((String) request.get("permissionName"));
        }
        if (request.containsKey("parentId")) {
            permission.setParentId(request.get("parentId") != null ? 
                Long.valueOf(request.get("parentId").toString()) : null);
        }
        if (request.containsKey("icon")) {
            permission.setIcon((String) request.get("icon"));
        }
        if (request.containsKey("routePath")) {
            permission.setRoutePath((String) request.get("routePath"));
        }
        if (request.containsKey("componentPath")) {
            permission.setComponentPath((String) request.get("componentPath"));
        }
        if (request.containsKey("sortOrder")) {
            permission.setSortOrder((Integer) request.get("sortOrder"));
        }
        if (request.containsKey("status")) {
            permission.setStatus(((String) request.get("status")).toUpperCase());
        }
        if (request.containsKey("description")) {
            permission.setDescription((String) request.get("description"));
        }
        permission.setUpdatedBy("SYSTEM");
        permission.setUpdatedTime(LocalDateTime.now());

        permissionMapper.updateById(permission);
        return buildSuccessResponse("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        IamPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            return buildErrorResponse("菜单不存在");
        }
        if (Boolean.TRUE.equals(permission.getIsSystemBuiltIn())) {
            return buildErrorResponse("系统内置菜单不能删除");
        }
        LambdaQueryWrapper<IamPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IamPermission::getParentId, id);
        if (permissionMapper.selectCount(wrapper) > 0) {
            return buildErrorResponse("存在子菜单，不能删除");
        }
        permissionMapper.deleteById(id);
        return buildSuccessResponse("删除成功", null);
    }

    private List<Map<String, Object>> buildTree(List<IamPermission> permissions) {
        Map<Long, Map<String, Object>> nodeMap = new HashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();

        for (IamPermission p : permissions) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", p.getId());
            node.put("label", p.getPermissionName());
            node.put("icon", p.getIcon());
            node.put("path", p.getRoutePath());
            node.put("children", new ArrayList<Map<String, Object>>());
            nodeMap.put(p.getId(), node);
        }

        for (IamPermission p : permissions) {
            if (p.getParentId() == null || p.getParentId() == 0) {
                roots.add(nodeMap.get(p.getId()));
            } else {
                Map<String, Object> parent = nodeMap.get(p.getParentId());
                if (parent != null) {
                    ((List<Map<String, Object>>) parent.get("children")).add(nodeMap.get(p.getId()));
                } else {
                    roots.add(nodeMap.get(p.getId()));
                }
            }
        }

        return roots;
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
