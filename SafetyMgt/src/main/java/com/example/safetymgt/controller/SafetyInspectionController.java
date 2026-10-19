package com.example.safetymgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.safetymgt.common.ResponseResult;
import com.example.safetymgt.entity.SafetyInspection;
import com.example.safetymgt.service.SafetyInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 安全检查
 */
@RestController
@RequestMapping("/api/eehs/safety/inspections")
public class SafetyInspectionController {

    @Autowired
    private SafetyInspectionService service;

    @GetMapping
    public ResponseResult<List<SafetyInspection>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<SafetyInspection> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<SafetyInspection> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<SafetyInspection> save(@RequestBody SafetyInspection entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<SafetyInspection> update(@PathVariable Long id, @RequestBody SafetyInspection entity) {
        entity.setId(id);
        service.updateById(entity);
        return ResponseResult.success(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return ResponseResult.success();
    }
}
