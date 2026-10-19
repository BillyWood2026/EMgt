package com.example.datamgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.datamgt.common.ResponseResult;
import com.example.datamgt.entity.MasterData;
import com.example.datamgt.service.MasterDataService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data/master")
public class MasterDataController {

    private final MasterDataService masterDataService;

    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping("/list")
    public ResponseResult<List<MasterData>> list(@RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String dataType) {
        LambdaQueryWrapper<MasterData> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(MasterData::getDataName, keyword).or().like(MasterData::getDataCode, keyword));
        }
        if (dataType != null && !dataType.isEmpty()) {
            w.eq(MasterData::getDataType, dataType);
        }
        w.orderByDesc(MasterData::getId);
        return ResponseResult.ok(masterDataService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<MasterData> getById(@PathVariable Long id) {
        return ResponseResult.ok(masterDataService.getById(id));
    }

    @PostMapping
    public ResponseResult<MasterData> create(@RequestBody MasterData entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        masterDataService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<MasterData> update(@PathVariable Long id, @RequestBody MasterData entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        masterDataService.updateById(entity);
        return ResponseResult.ok(masterDataService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(masterDataService.removeById(id));
    }
}
