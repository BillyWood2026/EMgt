package com.example.occhealthmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.occhealthmgt.common.ResponseResult;
import com.example.occhealthmgt.entity.OhProtection;
import com.example.occhealthmgt.service.OhProtectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 防护管理
 */
@RestController
@RequestMapping("/api/eehs/health/protections")
public class OhProtectionController {

    @Autowired
    private OhProtectionService service;

    @GetMapping
    public ResponseResult<List<OhProtection>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<OhProtection> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<OhProtection> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<OhProtection> save(@RequestBody OhProtection entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<OhProtection> update(@PathVariable Long id, @RequestBody OhProtection entity) {
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
