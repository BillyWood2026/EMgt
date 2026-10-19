package com.example.safetymgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.safetymgt.common.ResponseResult;
import com.example.safetymgt.entity.SafetyTraining;
import com.example.safetymgt.service.SafetyTrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 安全培训
 */
@RestController
@RequestMapping("/api/eehs/safety/trainings")
public class SafetyTrainingController {

    @Autowired
    private SafetyTrainingService service;

    @GetMapping
    public ResponseResult<List<SafetyTraining>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<SafetyTraining> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<SafetyTraining> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<SafetyTraining> save(@RequestBody SafetyTraining entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<SafetyTraining> update(@PathVariable Long id, @RequestBody SafetyTraining entity) {
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
