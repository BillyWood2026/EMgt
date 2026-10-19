package com.example.filemgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.filemgt.common.ResponseResult;
import com.example.filemgt.entity.FileRecord;
import com.example.filemgt.service.FileRecordService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/file/record")
public class FileRecordController {

    private final FileRecordService fileRecordService;

    public FileRecordController(FileRecordService fileRecordService) {
        this.fileRecordService = fileRecordService;
    }

    @GetMapping("/list")
    public ResponseResult<List<FileRecord>> list(@RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String serviceCode) {
        LambdaQueryWrapper<FileRecord> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(FileRecord::getFileName, keyword).or().like(FileRecord::getMd5, keyword));
        }
        if (serviceCode != null && !serviceCode.isEmpty()) {
            w.eq(FileRecord::getServiceCode, serviceCode);
        }
        w.orderByDesc(FileRecord::getId);
        return ResponseResult.ok(fileRecordService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<FileRecord> getById(@PathVariable Long id) {
        return ResponseResult.ok(fileRecordService.getById(id));
    }

    @PostMapping
    public ResponseResult<FileRecord> create(@RequestBody FileRecord entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        fileRecordService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<FileRecord> update(@PathVariable Long id, @RequestBody FileRecord entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        fileRecordService.updateById(entity);
        return ResponseResult.ok(fileRecordService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(fileRecordService.removeById(id));
    }
}
