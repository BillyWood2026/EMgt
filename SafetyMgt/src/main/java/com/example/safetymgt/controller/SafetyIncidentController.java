package com.example.safetymgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.safetymgt.common.ResponseResult;
import com.example.safetymgt.entity.SafetyIncident;
import com.example.safetymgt.service.SafetyIncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 事故管理
 */
@RestController
@RequestMapping("/api/eehs/safety/incidents")
public class SafetyIncidentController {

    @Autowired
    private SafetyIncidentService service;

    @GetMapping
    public ResponseResult<List<SafetyIncident>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<SafetyIncident> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<SafetyIncident> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<SafetyIncident> save(@RequestBody SafetyIncident entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<SafetyIncident> update(@PathVariable Long id, @RequestBody SafetyIncident entity) {
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
