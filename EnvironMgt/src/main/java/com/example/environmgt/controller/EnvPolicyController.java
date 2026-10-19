package com.example.environmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.environmgt.common.ResponseResult;
import com.example.environmgt.entity.EnvPolicy;
import com.example.environmgt.service.EnvPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 环保政策
 */
@RestController
@RequestMapping("/api/eehs/env/policies")
public class EnvPolicyController {

    @Autowired
    private EnvPolicyService service;

    @GetMapping
    public ResponseResult<List<EnvPolicy>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<EnvPolicy> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<EnvPolicy> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<EnvPolicy> save(@RequestBody EnvPolicy entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<EnvPolicy> update(@PathVariable Long id, @RequestBody EnvPolicy entity) {
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
