package com.example.occhealthmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.occhealthmgt.common.ResponseResult;
import com.example.occhealthmgt.entity.OhExamination;
import com.example.occhealthmgt.service.OhExaminationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职业健康检查
 */
@RestController
@RequestMapping("/api/eehs/health/examinations")
public class OhExaminationController {

    @Autowired
    private OhExaminationService service;

    @GetMapping
    public ResponseResult<List<OhExamination>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<OhExamination> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<OhExamination> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<OhExamination> save(@RequestBody OhExamination entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<OhExamination> update(@PathVariable Long id, @RequestBody OhExamination entity) {
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
