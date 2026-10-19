package com.example.projectmgt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.projectmgt.entity.PmProgressTrack;
import com.example.projectmgt.service.PmProgressTrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project/progress")
public class ProgressTrackController {

    @Autowired
    private PmProgressTrackService progressTrackService;

    @GetMapping({"/list", ""})
    public ResponseResult<List<PmProgressTrack>> list() {
        return ResponseResult.success(progressTrackService.list());
    }

    @GetMapping("/page")
    public ResponseResult<IPage<PmProgressTrack>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String lagStatus,
            @RequestParam(required = false) Long projectId) {
        Page<PmProgressTrack> page = new Page<>(current, size);
        return ResponseResult.success(progressTrackService.page(page, lagStatus, projectId));
    }

    @GetMapping("/{id}")
    public ResponseResult<PmProgressTrack> getById(@PathVariable Long id) {
        return ResponseResult.success(progressTrackService.getById(id));
    }

    @PostMapping
    public ResponseResult<PmProgressTrack> create(@RequestBody PmProgressTrack track) {
        return ResponseResult.success(progressTrackService.create(track));
    }

    @PutMapping("/{id}")
    public ResponseResult<PmProgressTrack> update(@PathVariable Long id, @RequestBody PmProgressTrack track) {
        track.setId(id);
        return ResponseResult.success(progressTrackService.update(track));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        progressTrackService.deleteById(id);
        return ResponseResult.success();
    }

    @DeleteMapping("/batch")
    public ResponseResult<Void> batchDelete(@RequestBody List<Long> ids) {
        progressTrackService.batchDelete(ids);
        return ResponseResult.success();
    }
}
