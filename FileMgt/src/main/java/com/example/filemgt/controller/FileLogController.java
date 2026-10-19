package com.example.filemgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.filemgt.common.ResponseResult;
import com.example.filemgt.entity.FileLog;
import com.example.filemgt.service.FileLogService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/file/log")
public class FileLogController {

    private final FileLogService fileLogService;

    public FileLogController(FileLogService fileLogService) {
        this.fileLogService = fileLogService;
    }

    @GetMapping("/list")
    public ResponseResult<List<FileLog>> list(@RequestParam(required = false) Long fileId,
                                              @RequestParam(required = false) String opType) {
        LambdaQueryWrapper<FileLog> w = new LambdaQueryWrapper<>();
        if (fileId != null) {
            w.eq(FileLog::getFileId, fileId);
        }
        if (opType != null && !opType.isEmpty()) {
            w.eq(FileLog::getOpType, opType);
        }
        w.orderByDesc(FileLog::getId);
        return ResponseResult.ok(fileLogService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<FileLog> getById(@PathVariable Long id) {
        return ResponseResult.ok(fileLogService.getById(id));
    }

    @PostMapping
    public ResponseResult<FileLog> create(@RequestBody FileLog entity) {
        entity.setId(null);
        if (entity.getOpTime() == null) {
            entity.setOpTime(LocalDateTime.now());
        }
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        fileLogService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<FileLog> update(@PathVariable Long id, @RequestBody FileLog entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        fileLogService.updateById(entity);
        return ResponseResult.ok(fileLogService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(fileLogService.removeById(id));
    }
}
