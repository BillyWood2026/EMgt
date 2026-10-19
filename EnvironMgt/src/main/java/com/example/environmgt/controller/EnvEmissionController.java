package com.example.environmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.environmgt.common.ResponseResult;
import com.example.environmgt.entity.EnvEmission;
import com.example.environmgt.service.EnvEmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排污管理
 */
@RestController
@RequestMapping("/api/eehs/env/emissions")
public class EnvEmissionController {

    @Autowired
    private EnvEmissionService service;

    @GetMapping
    public ResponseResult<List<EnvEmission>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<EnvEmission> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<EnvEmission> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<EnvEmission> save(@RequestBody EnvEmission entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<EnvEmission> update(@PathVariable Long id, @RequestBody EnvEmission entity) {
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
