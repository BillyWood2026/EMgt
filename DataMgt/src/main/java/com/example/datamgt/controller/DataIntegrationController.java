package com.example.datamgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.datamgt.common.ResponseResult;
import com.example.datamgt.entity.DataIntegration;
import com.example.datamgt.service.DataIntegrationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data/integration")
public class DataIntegrationController {

    private final DataIntegrationService dataIntegrationService;

    public DataIntegrationController(DataIntegrationService dataIntegrationService) {
        this.dataIntegrationService = dataIntegrationService;
    }

    @GetMapping("/list")
    public ResponseResult<List<DataIntegration>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<DataIntegration> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(DataIntegration::getTaskName, keyword);
        }
        w.orderByDesc(DataIntegration::getId);
        return ResponseResult.ok(dataIntegrationService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<DataIntegration> getById(@PathVariable Long id) {
        return ResponseResult.ok(dataIntegrationService.getById(id));
    }

    @PostMapping
    public ResponseResult<DataIntegration> create(@RequestBody DataIntegration entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dataIntegrationService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<DataIntegration> update(@PathVariable Long id, @RequestBody DataIntegration entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        dataIntegrationService.updateById(entity);
        return ResponseResult.ok(dataIntegrationService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(dataIntegrationService.removeById(id));
    }
}
