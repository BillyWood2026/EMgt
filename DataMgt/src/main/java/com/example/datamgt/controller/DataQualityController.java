package com.example.datamgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.datamgt.common.ResponseResult;
import com.example.datamgt.entity.DataQuality;
import com.example.datamgt.service.DataQualityService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data/quality")
public class DataQualityController {

    private final DataQualityService dataQualityService;

    public DataQualityController(DataQualityService dataQualityService) {
        this.dataQualityService = dataQualityService;
    }

    @GetMapping("/list")
    public ResponseResult<List<DataQuality>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<DataQuality> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(DataQuality::getRuleName, keyword).or().like(DataQuality::getTargetObject, keyword));
        }
        w.orderByDesc(DataQuality::getId);
        return ResponseResult.ok(dataQualityService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<DataQuality> getById(@PathVariable Long id) {
        return ResponseResult.ok(dataQualityService.getById(id));
    }

    @PostMapping
    public ResponseResult<DataQuality> create(@RequestBody DataQuality entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dataQualityService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<DataQuality> update(@PathVariable Long id, @RequestBody DataQuality entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        dataQualityService.updateById(entity);
        return ResponseResult.ok(dataQualityService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(dataQualityService.removeById(id));
    }
}
