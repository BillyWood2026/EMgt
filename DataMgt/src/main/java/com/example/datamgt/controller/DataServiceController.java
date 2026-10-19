package com.example.datamgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.datamgt.common.ResponseResult;
import com.example.datamgt.entity.DataService;
import com.example.datamgt.service.DataServiceService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data/service")
public class DataServiceController {

    private final DataServiceService dataServiceService;

    public DataServiceController(DataServiceService dataServiceService) {
        this.dataServiceService = dataServiceService;
    }

    @GetMapping("/list")
    public ResponseResult<List<DataService>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<DataService> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(DataService::getServiceName, keyword).or().like(DataService::getServiceCode, keyword));
        }
        w.orderByDesc(DataService::getId);
        return ResponseResult.ok(dataServiceService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<DataService> getById(@PathVariable Long id) {
        return ResponseResult.ok(dataServiceService.getById(id));
    }

    @PostMapping
    public ResponseResult<DataService> create(@RequestBody DataService entity) {
        entity.setId(null);
        if (entity.getInvokeCount() == null) {
            entity.setInvokeCount(0L);
        }
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dataServiceService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<DataService> update(@PathVariable Long id, @RequestBody DataService entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        dataServiceService.updateById(entity);
        return ResponseResult.ok(dataServiceService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(dataServiceService.removeById(id));
    }
}
