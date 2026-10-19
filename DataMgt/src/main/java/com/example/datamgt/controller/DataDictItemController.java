package com.example.datamgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.datamgt.common.ResponseResult;
import com.example.datamgt.entity.DataDictItem;
import com.example.datamgt.service.DataDictItemService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data/dict/item")
public class DataDictItemController {

    private final DataDictItemService dataDictItemService;

    public DataDictItemController(DataDictItemService dataDictItemService) {
        this.dataDictItemService = dataDictItemService;
    }

    @GetMapping("/list")
    public ResponseResult<List<DataDictItem>> list(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String typeCode) {
        LambdaQueryWrapper<DataDictItem> w = new LambdaQueryWrapper<>();
        if (typeCode != null && !typeCode.isEmpty()) {
            w.eq(DataDictItem::getTypeCode, typeCode);
        }
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(DataDictItem::getItemLabel, keyword).or().like(DataDictItem::getItemValue, keyword));
        }
        w.orderByAsc(DataDictItem::getSort).orderByDesc(DataDictItem::getId);
        return ResponseResult.ok(dataDictItemService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<DataDictItem> getById(@PathVariable Long id) {
        return ResponseResult.ok(dataDictItemService.getById(id));
    }

    @PostMapping
    public ResponseResult<DataDictItem> create(@RequestBody DataDictItem entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dataDictItemService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<DataDictItem> update(@PathVariable Long id, @RequestBody DataDictItem entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        dataDictItemService.updateById(entity);
        return ResponseResult.ok(dataDictItemService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(dataDictItemService.removeById(id));
    }
}
