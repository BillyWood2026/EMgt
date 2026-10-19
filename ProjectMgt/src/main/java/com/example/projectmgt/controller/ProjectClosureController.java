package com.example.projectmgt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.projectmgt.entity.PmProjectClosure;
import com.example.projectmgt.service.PmProjectClosureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project/closure")
public class ProjectClosureController {

    @Autowired
    private PmProjectClosureService closureService;

    @GetMapping({"/list", ""})
    public ResponseResult<List<PmProjectClosure>> list() {
        return ResponseResult.success(closureService.list());
    }

    @GetMapping("/page")
    public ResponseResult<IPage<PmProjectClosure>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long projectId) {
        Page<PmProjectClosure> page = new Page<>(current, size);
        return ResponseResult.success(closureService.page(page, status, projectId));
    }

    @GetMapping("/{id}")
    public ResponseResult<PmProjectClosure> getById(@PathVariable Long id) {
        return ResponseResult.success(closureService.getById(id));
    }

    @PostMapping
    public ResponseResult<PmProjectClosure> create(@RequestBody PmProjectClosure closure) {
        return ResponseResult.success(closureService.create(closure));
    }

    @PutMapping("/{id}")
    public ResponseResult<PmProjectClosure> update(@PathVariable Long id, @RequestBody PmProjectClosure closure) {
        closure.setId(id);
        return ResponseResult.success(closureService.update(closure));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        closureService.deleteById(id);
        return ResponseResult.success();
    }

    @DeleteMapping("/batch")
    public ResponseResult<Void> batchDelete(@RequestBody List<Long> ids) {
        closureService.batchDelete(ids);
        return ResponseResult.success();
    }

    @PostMapping("/{id}/submit")
    public ResponseResult<PmProjectClosure> submit(@PathVariable Long id) {
        return ResponseResult.success(closureService.submit(id));
    }

    @PostMapping("/{id}/review")
    public ResponseResult<PmProjectClosure> review(@PathVariable Long id,
                                                   @RequestBody(required = false) java.util.Map<String, Object> body) {
        boolean passed = true;
        String opinion = null;
        if (body != null) {
            Object p = body.get("passed");
            if (p != null) {
                passed = Boolean.parseBoolean(String.valueOf(p));
            }
            Object o = body.get("opinion");
            if (o != null) {
                opinion = String.valueOf(o);
            }
        }
        return ResponseResult.success(closureService.review(id, passed, opinion));
    }

    @PostMapping("/{id}/archive")
    public ResponseResult<PmProjectClosure> archive(@PathVariable Long id) {
        return ResponseResult.success(closureService.archive(id));
    }
}
