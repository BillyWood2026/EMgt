package com.example.datamgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.datamgt.common.ResponseResult;
import com.example.datamgt.entity.DataAsset;
import com.example.datamgt.service.DataAssetService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data/asset")
public class DataAssetController {

    private final DataAssetService dataAssetService;

    public DataAssetController(DataAssetService dataAssetService) {
        this.dataAssetService = dataAssetService;
    }

    @GetMapping("/list")
    public ResponseResult<List<DataAsset>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<DataAsset> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(DataAsset::getAssetName, keyword).or().like(DataAsset::getCategory, keyword));
        }
        w.orderByDesc(DataAsset::getId);
        return ResponseResult.ok(dataAssetService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<DataAsset> getById(@PathVariable Long id) {
        return ResponseResult.ok(dataAssetService.getById(id));
    }

    @PostMapping
    public ResponseResult<DataAsset> create(@RequestBody DataAsset entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dataAssetService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<DataAsset> update(@PathVariable Long id, @RequestBody DataAsset entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        dataAssetService.updateById(entity);
        return ResponseResult.ok(dataAssetService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(dataAssetService.removeById(id));
    }
}
