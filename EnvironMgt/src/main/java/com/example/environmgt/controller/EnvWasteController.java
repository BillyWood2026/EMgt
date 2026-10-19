package com.example.environmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.environmgt.common.ResponseResult;
import com.example.environmgt.entity.EnvWaste;
import com.example.environmgt.service.EnvWasteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 废弃物管理
 */
@RestController
@RequestMapping("/api/eehs/env/wastes")
public class EnvWasteController {

    @Autowired
    private EnvWasteService service;

    @GetMapping
    public ResponseResult<List<EnvWaste>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<EnvWaste> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<EnvWaste> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<EnvWaste> save(@RequestBody EnvWaste entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<EnvWaste> update(@PathVariable Long id, @RequestBody EnvWaste entity) {
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
