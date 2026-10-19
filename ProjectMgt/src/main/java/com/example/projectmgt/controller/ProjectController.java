package com.example.projectmgt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.projectmgt.entity.PmProject;
import com.example.projectmgt.service.PmProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project/pm")
public class ProjectController {

    @Autowired
    private PmProjectService projectService;

    @GetMapping({"/list", ""})
    public ResponseResult<List<PmProject>> list() {
        return ResponseResult.success(projectService.list());
    }

    @GetMapping("/page")
    public ResponseResult<IPage<PmProject>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name) {
        Page<PmProject> page = new Page<>(current, size);
        return ResponseResult.success(projectService.page(page, status, type, name));
    }

    @GetMapping("/{id}")
    public ResponseResult<PmProject> getById(@PathVariable Long id) {
        return ResponseResult.success(projectService.getById(id));
    }

    @PostMapping
    public ResponseResult<PmProject> create(@RequestBody PmProject project) {
        return ResponseResult.success(projectService.create(project));
    }

    @PutMapping("/{id}")
    public ResponseResult<PmProject> update(@PathVariable Long id, @RequestBody PmProject project) {
        project.setId(id);
        return ResponseResult.success(projectService.update(project));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        projectService.deleteById(id);
        return ResponseResult.success();
    }

    @DeleteMapping("/batch")
    public ResponseResult<Void> batchDelete(@RequestBody List<Long> ids) {
        projectService.batchDelete(ids);
        return ResponseResult.success();
    }

    @PostMapping("/{id}/start")
    public ResponseResult<PmProject> start(@PathVariable Long id) {
        return ResponseResult.success(projectService.start(id));
    }

    @PostMapping("/{id}/suspend")
    public ResponseResult<PmProject> suspend(@PathVariable Long id) {
        return ResponseResult.success(projectService.suspend(id));
    }

    @PostMapping("/{id}/resume")
    public ResponseResult<PmProject> resume(@PathVariable Long id) {
        return ResponseResult.success(projectService.resume(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseResult<PmProject> complete(@PathVariable Long id) {
        return ResponseResult.success(projectService.complete(id));
    }

    @PostMapping("/{id}/monthly-lock")
    public ResponseResult<PmProject> monthlyLock(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "true") boolean locked) {
        return ResponseResult.success(projectService.setMonthlyLock(id, locked));
    }

    @PostMapping("/{id}/baseline")
    public ResponseResult<PmProject> baseline(@PathVariable Long id) {
        return ResponseResult.success(projectService.freezeBaseline(id));
    }

    @PostMapping("/{id}/recalc-budget")
    public ResponseResult<PmProject> recalcBudget(@PathVariable Long id) {
        return ResponseResult.success(projectService.recalcBudget(id));
    }
}
