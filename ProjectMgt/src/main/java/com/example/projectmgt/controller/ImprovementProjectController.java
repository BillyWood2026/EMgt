package com.example.projectmgt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.projectmgt.entity.ImprovementProject;
import com.example.projectmgt.service.ImprovementProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project/improvement")
public class ImprovementProjectController {

    @Autowired
    private ImprovementProjectService projectService;

    @GetMapping({"/list", ""})
    public ResponseResult<List<ImprovementProject>> list() {
        return ResponseResult.success(projectService.list());
    }

    @GetMapping("/page")
    public ResponseResult<IPage<ImprovementProject>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String department) {
        Page<ImprovementProject> page = new Page<>(current, size);
        return ResponseResult.success(projectService.page(page, status, type, department));
    }

    @GetMapping("/{id}")
    public ResponseResult<ImprovementProject> getById(@PathVariable Long id) {
        return ResponseResult.success(projectService.getById(id));
    }

    @PostMapping
    public ResponseResult<ImprovementProject> create(@RequestBody ImprovementProject project) {
        return ResponseResult.success(projectService.create(project));
    }

    @PutMapping("/{id}")
    public ResponseResult<ImprovementProject> update(@PathVariable Long id, @RequestBody ImprovementProject project) {
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
}
