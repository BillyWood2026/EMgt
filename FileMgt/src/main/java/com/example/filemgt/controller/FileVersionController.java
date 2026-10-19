package com.example.filemgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.filemgt.common.ResponseResult;
import com.example.filemgt.entity.FileVersion;
import com.example.filemgt.service.FileVersionService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/file/version")
public class FileVersionController {

    private final FileVersionService fileVersionService;

    public FileVersionController(FileVersionService fileVersionService) {
        this.fileVersionService = fileVersionService;
    }

    @GetMapping("/list")
    public ResponseResult<List<FileVersion>> list(@RequestParam(required = false) Long fileId,
                                                  @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<FileVersion> w = new LambdaQueryWrapper<>();
        if (fileId != null) {
            w.eq(FileVersion::getFileId, fileId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(FileVersion::getVersionNo, keyword).or().like(FileVersion::getRemark, keyword));
        }
        w.orderByDesc(FileVersion::getId);
        return ResponseResult.ok(fileVersionService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<FileVersion> getById(@PathVariable Long id) {
        return ResponseResult.ok(fileVersionService.getById(id));
    }

    @PostMapping
    public ResponseResult<FileVersion> create(@RequestBody FileVersion entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        fileVersionService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<FileVersion> update(@PathVariable Long id, @RequestBody FileVersion entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        fileVersionService.updateById(entity);
        return ResponseResult.ok(fileVersionService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(fileVersionService.removeById(id));
    }
}
