package com.example.energymgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.energymgt.common.ResponseResult;
import com.example.energymgt.entity.EnergyAnalysis;
import com.example.energymgt.service.EnergyAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 能源分析
 */
@RestController
@RequestMapping("/api/eehs/energy/analyses")
public class EnergyAnalysisController {

    @Autowired
    private EnergyAnalysisService service;

    @GetMapping
    public ResponseResult<List<EnergyAnalysis>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<EnergyAnalysis> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<EnergyAnalysis> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<EnergyAnalysis> save(@RequestBody EnergyAnalysis entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<EnergyAnalysis> update(@PathVariable Long id, @RequestBody EnergyAnalysis entity) {
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
