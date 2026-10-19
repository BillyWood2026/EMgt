package com.example.datamgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.datamgt.common.ResponseResult;
import com.example.datamgt.entity.Metadata;
import com.example.datamgt.service.MetadataService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/list")
    public ResponseResult<List<Metadata>> list(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String dbName) {
        LambdaQueryWrapper<Metadata> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(Metadata::getTableName, keyword).or()
                    .like(Metadata::getFieldName, keyword).or()
                    .like(Metadata::getCnName, keyword));
        }
        if (dbName != null && !dbName.isEmpty()) {
            w.eq(Metadata::getDbName, dbName);
        }
        w.orderByDesc(Metadata::getId);
        return ResponseResult.ok(metadataService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<Metadata> getById(@PathVariable Long id) {
        return ResponseResult.ok(metadataService.getById(id));
    }

    @PostMapping
    public ResponseResult<Metadata> create(@RequestBody Metadata entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        metadataService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<Metadata> update(@PathVariable Long id, @RequestBody Metadata entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        metadataService.updateById(entity);
        return ResponseResult.ok(metadataService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(metadataService.removeById(id));
    }
}
