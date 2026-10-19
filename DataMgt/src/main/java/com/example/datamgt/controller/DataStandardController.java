package com.example.datamgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.datamgt.common.ResponseResult;
import com.example.datamgt.entity.DataStandard;
import com.example.datamgt.service.DataStandardService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data/standard")
public class DataStandardController {

    private final DataStandardService dataStandardService;

    public DataStandardController(DataStandardService dataStandardService) {
        this.dataStandardService = dataStandardService;
    }

    @GetMapping("/list")
    public ResponseResult<List<DataStandard>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<DataStandard> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(DataStandard::getStdName, keyword).or().like(DataStandard::getStdCode, keyword));
        }
        w.orderByDesc(DataStandard::getId);
        return ResponseResult.ok(dataStandardService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<DataStandard> getById(@PathVariable Long id) {
        return ResponseResult.ok(dataStandardService.getById(id));
    }

    @PostMapping
    public ResponseResult<DataStandard> create(@RequestBody DataStandard entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dataStandardService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<DataStandard> update(@PathVariable Long id, @RequestBody DataStandard entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        dataStandardService.updateById(entity);
        return ResponseResult.ok(dataStandardService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(dataStandardService.removeById(id));
    }
}
