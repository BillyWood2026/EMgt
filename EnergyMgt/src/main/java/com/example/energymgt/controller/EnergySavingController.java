package com.example.energymgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.energymgt.common.ResponseResult;
import com.example.energymgt.entity.EnergySaving;
import com.example.energymgt.service.EnergySavingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 节能项目
 */
@RestController
@RequestMapping("/api/eehs/energy/savings")
public class EnergySavingController {

    @Autowired
    private EnergySavingService service;

    @GetMapping
    public ResponseResult<List<EnergySaving>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<EnergySaving> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("name", keyword).or().like("title", keyword);
        }
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<EnergySaving> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<EnergySaving> save(@RequestBody EnergySaving entity) {
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<EnergySaving> update(@PathVariable Long id, @RequestBody EnergySaving entity) {
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
