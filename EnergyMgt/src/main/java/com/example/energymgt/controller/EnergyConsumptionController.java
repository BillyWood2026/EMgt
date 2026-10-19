package com.example.energymgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.energymgt.common.ResponseResult;
import com.example.energymgt.entity.EnergyConsumption;
import com.example.energymgt.service.EnergyConsumptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 能耗统计
 */
@RestController
@RequestMapping("/api/eehs/energy/consumptions")
public class EnergyConsumptionController {

    @Autowired
    private EnergyConsumptionService service;

    @GetMapping
    public ResponseResult<List<EnergyConsumption>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<EnergyConsumption> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<EnergyConsumption> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<EnergyConsumption> save(@RequestBody EnergyConsumption entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<EnergyConsumption> update(@PathVariable Long id, @RequestBody EnergyConsumption entity) {
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
