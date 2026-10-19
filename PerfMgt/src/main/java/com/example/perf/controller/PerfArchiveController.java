package com.example.perf.controller;

import com.example.common.dto.ResponseResult;
import com.example.perf.entity.PerfArchive;
import com.example.perf.service.PerfArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 绩效结果（评估归档）Controller（前端 Result.vue，base /api/perf/archive）
 * 页面无新增/编辑 UI，仅提供列表查询。
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/perf/archive")
public class PerfArchiveController {

    @Autowired
    private PerfArchiveService perfArchiveService;

    /**
     * 列表查询（按排名升序）
     */
    @GetMapping("/list")
    public ResponseResult<List<PerfArchive>> list() {
        return ResponseResult.success(perfArchiveService.listOrdered());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<PerfArchive> getById(@PathVariable Long id) {
        return ResponseResult.success(perfArchiveService.getDetail(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<PerfArchive> create(@RequestBody PerfArchive archive) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        archive.setId(null);
        archive.setDeleted(0);
        archive.setVersion(1);
        archive.setCreateTime(now);
        archive.setUpdateTime(now);
        perfArchiveService.save(archive);
        return ResponseResult.success(archive);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ResponseResult<PerfArchive> update(@PathVariable Long id,
                                              @RequestBody PerfArchive archive) {
        archive.setId(id);
        archive.setUpdateTime(java.time.LocalDateTime.now());
        perfArchiveService.updateById(archive);
        return ResponseResult.success(perfArchiveService.getById(id));
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        perfArchiveService.removeById(id);
        return ResponseResult.success();
    }
}
