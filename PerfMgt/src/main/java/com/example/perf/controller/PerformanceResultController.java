package com.example.perf.controller;

import com.example.common.dto.ResponseResult;
import com.example.perf.entity.PerfArchive;
import com.example.perf.service.PerfArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 兼容旧路径 /api/perf/result 的 Controller，
 * 底层复用 perf_archive 真实持久层。
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/perf/result")
public class PerformanceResultController {

    @Autowired
    private PerfArchiveService perfArchiveService;

    @GetMapping("/list")
    public ResponseResult<List<PerfArchive>> list() {
        return ResponseResult.success(perfArchiveService.listOrdered());
    }

    @GetMapping("/{id}")
    public ResponseResult<PerfArchive> getById(@PathVariable Long id) {
        return ResponseResult.success(perfArchiveService.getDetail(id));
    }

    @PostMapping
    public ResponseResult<PerfArchive> create(@RequestBody PerfArchive archive) {
        LocalDateTime now = LocalDateTime.now();
        archive.setId(null);
        archive.setDeleted(0);
        archive.setVersion(1);
        archive.setCreateTime(now);
        archive.setUpdateTime(now);
        perfArchiveService.save(archive);
        return ResponseResult.success(archive);
    }

    @PutMapping("/{id}")
    public ResponseResult<PerfArchive> update(@PathVariable Long id,
                                              @RequestBody PerfArchive archive) {
        archive.setId(id);
        archive.setUpdateTime(LocalDateTime.now());
        perfArchiveService.updateById(archive);
        return ResponseResult.success(perfArchiveService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        perfArchiveService.removeById(id);
        return ResponseResult.success();
    }
}
