package com.example.projectmgt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.projectmgt.entity.PmWbsTask;
import com.example.projectmgt.service.PmWbsTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project/wbs")
public class WbsTaskController {

    @Autowired
    private PmWbsTaskService wbsTaskService;

    @GetMapping({"/list", ""})
    public ResponseResult<List<PmWbsTask>> list() {
        return ResponseResult.success(wbsTaskService.list());
    }

    @GetMapping("/page")
    public ResponseResult<IPage<PmWbsTask>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long projectId) {
        Page<PmWbsTask> page = new Page<>(current, size);
        return ResponseResult.success(wbsTaskService.page(page, status, projectId));
    }

    @GetMapping("/{id}")
    public ResponseResult<PmWbsTask> getById(@PathVariable Long id) {
        return ResponseResult.success(wbsTaskService.getById(id));
    }

    @PostMapping
    public ResponseResult<PmWbsTask> create(@RequestBody PmWbsTask task) {
        return ResponseResult.success(wbsTaskService.create(task));
    }

    @PutMapping("/{id}")
    public ResponseResult<PmWbsTask> update(@PathVariable Long id, @RequestBody PmWbsTask task) {
        task.setId(id);
        return ResponseResult.success(wbsTaskService.update(task));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        wbsTaskService.deleteById(id);
        return ResponseResult.success();
    }

    @DeleteMapping("/batch")
    public ResponseResult<Void> batchDelete(@RequestBody List<Long> ids) {
        wbsTaskService.batchDelete(ids);
        return ResponseResult.success();
    }

    @GetMapping("/weight-summary")
    public ResponseResult<java.util.Map<String, Object>> weightSummary(@RequestParam Long projectId) {
        return ResponseResult.success(wbsTaskService.weightSummary(projectId));
    }
}
