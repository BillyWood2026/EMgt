package com.example.occhealthmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.occhealthmgt.common.ResponseResult;
import com.example.occhealthmgt.entity.OhHealthRecord;
import com.example.occhealthmgt.service.OhHealthRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 健康档案
 */
@RestController
@RequestMapping("/api/eehs/health/records")
public class OhHealthRecordController {

    @Autowired
    private OhHealthRecordService service;

    @GetMapping
    public ResponseResult<List<OhHealthRecord>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<OhHealthRecord> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<OhHealthRecord> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<OhHealthRecord> save(@RequestBody OhHealthRecord entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<OhHealthRecord> update(@PathVariable Long id, @RequestBody OhHealthRecord entity) {
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
