package com.example.environmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.environmgt.common.ResponseResult;
import com.example.environmgt.entity.EnvMonitor;
import com.example.environmgt.service.EnvMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 环境监测
 */
@RestController
@RequestMapping("/api/eehs/env/monitors")
public class EnvMonitorController {

    @Autowired
    private EnvMonitorService service;

    @GetMapping
    public ResponseResult<List<EnvMonitor>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<EnvMonitor> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<EnvMonitor> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<EnvMonitor> save(@RequestBody EnvMonitor entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<EnvMonitor> update(@PathVariable Long id, @RequestBody EnvMonitor entity) {
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
