package com.example.safetymgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.safetymgt.common.ResponseResult;
import com.example.safetymgt.entity.SafetyPolicy;
import com.example.safetymgt.service.SafetyPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 安全制度
 */
@RestController
@RequestMapping("/api/eehs/safety/policies")
public class SafetyPolicyController {

    @Autowired
    private SafetyPolicyService service;

    @GetMapping
    public ResponseResult<List<SafetyPolicy>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<SafetyPolicy> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<SafetyPolicy> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<SafetyPolicy> save(@RequestBody SafetyPolicy entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<SafetyPolicy> update(@PathVariable Long id, @RequestBody SafetyPolicy entity) {
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
