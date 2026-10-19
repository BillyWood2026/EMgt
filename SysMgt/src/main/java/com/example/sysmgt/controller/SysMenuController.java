package com.example.sysmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.sysmgt.entity.SysMenu;
import com.example.sysmgt.mapper.SysMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单管理 Controller（对齐前端 Menu.vue）：
 * GET    /api/sys/menu/tree   菜单树
 * GET    /api/sys/menu/list   扁平列表
 * GET    /api/sys/menu/{id}   详情
 * POST   /api/sys/menu        新增
 * PUT    /api/sys/menu/{id}   修改
 * DELETE /api/sys/menu/{id}   删除（含子孙菜单）
 */
@RestController
@RequestMapping("/api/sys/menu")
public class SysMenuController {

    @Autowired
    private SysMenuMapper menuMapper;

    @GetMapping("/tree")
    public ResponseResult<List<Map<String, Object>>> tree() {
        List<SysMenu> all = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder, SysMenu::getId));
        Map<Long, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (SysMenu m : all) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", m.getId());
            node.put("parentId", m.getParentId());
            node.put("menuCode", m.getMenuCode());
            node.put("menuName", m.getMenuName());
            node.put("menuType", m.getMenuType());
            node.put("path", m.getPath());
            node.put("component", m.getComponent());
            node.put("icon", m.getIcon());
            node.put("sortOrder", m.getSortOrder());
            node.put("status", m.getStatus());
            node.put("children", new ArrayList<Map<String, Object>>());
            nodeMap.put(m.getId(), node);
        }
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> node : nodeMap.values()) {
            Long pid = (Long) node.get("parentId");
            Map<String, Object> parent = pid == null ? null : nodeMap.get(pid);
            if (parent != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                children.add(node);
            } else {
                roots.add(node);
            }
        }
        return ResponseResult.success(roots);
    }

    @GetMapping("/list")
    public ResponseResult<List<SysMenu>> list() {
        return ResponseResult.success(menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder, SysMenu::getId)));
    }

    @GetMapping("/{id}")
    public ResponseResult<SysMenu> getById(@PathVariable Long id) {
        return ResponseResult.success(menuMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<SysMenu> create(@RequestBody SysMenu menu) {
        if (menu.getMenuName() == null || menu.getMenuName().trim().isEmpty()) {
            return ResponseResult.error("菜单名称不能为空");
        }
        if (menu.getMenuCode() == null || menu.getMenuCode().trim().isEmpty()) {
            menu.setMenuCode("MENU" + System.currentTimeMillis());
        }
        if (menu.getMenuType() == null) {
            menu.setMenuType("MENU");
        }
        if (menu.getStatus() == null) {
            menu.setStatus("ACTIVE");
        }
        if (menu.getSortOrder() == null) {
            menu.setSortOrder(0);
        }
        if (menu.getParentId() != null && menu.getParentId() <= 0) {
            menu.setParentId(null);
        }
        menu.setId(null);
        menuMapper.insert(menu);
        return ResponseResult.success(menu);
    }

    @PutMapping("/{id}")
    public ResponseResult<SysMenu> update(@PathVariable Long id, @RequestBody SysMenu menu) {
        menu.setId(id);
        if (menu.getParentId() != null && menu.getParentId() <= 0) {
            menu.setParentId(null);
        }
        menuMapper.updateById(menu);
        return ResponseResult.success(menuMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        List<SysMenu> all = menuMapper.selectList(null);
        Map<Long, List<Long>> childrenMap = all.stream()
                .filter(m -> m.getParentId() != null)
                .collect(Collectors.groupingBy(SysMenu::getParentId,
                        Collectors.mapping(SysMenu::getId, Collectors.toList())));
        List<Long> toDelete = new ArrayList<>();
        List<Long> frontier = new ArrayList<>();
        frontier.add(id);
        while (!frontier.isEmpty()) {
            Long current = frontier.remove(frontier.size() - 1);
            if (toDelete.contains(current)) {
                continue;
            }
            toDelete.add(current);
            List<Long> children = childrenMap.get(current);
            if (children != null) {
                frontier.addAll(children);
            }
        }
        menuMapper.deleteBatchIds(toDelete);
        return ResponseResult.success(true);
    }
}
