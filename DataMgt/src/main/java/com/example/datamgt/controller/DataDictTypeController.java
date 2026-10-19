package com.example.datamgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.datamgt.common.ResponseResult;
import com.example.datamgt.entity.DataDictType;
import com.example.datamgt.service.DataDictTypeService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data/dict/type")
public class DataDictTypeController {

    private final DataDictTypeService dataDictTypeService;

    public DataDictTypeController(DataDictTypeService dataDictTypeService) {
        this.dataDictTypeService = dataDictTypeService;
    }

    @GetMapping("/list")
    public ResponseResult<List<DataDictType>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<DataDictType> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(DataDictType::getTypeName, keyword).or().like(DataDictType::getTypeCode, keyword));
        }
        w.orderByAsc(DataDictType::getSort).orderByDesc(DataDictType::getId);
        return ResponseResult.ok(dataDictTypeService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<DataDictType> getById(@PathVariable Long id) {
        return ResponseResult.ok(dataDictTypeService.getById(id));
    }

    @PostMapping
    public ResponseResult<DataDictType> create(@RequestBody DataDictType entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dataDictTypeService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<DataDictType> update(@PathVariable Long id, @RequestBody DataDictType entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        dataDictTypeService.updateById(entity);
        return ResponseResult.ok(dataDictTypeService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(dataDictTypeService.removeById(id));
    }
}
