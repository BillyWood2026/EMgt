package com.example.improvemgt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.improvemgt.entity.ImprovementImplementation;
import com.example.improvemgt.service.ImprovementImplementationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/improve/implementation")
public class ImprovementImplementationController {

    @Autowired
    private ImprovementImplementationService implementationService;

    @GetMapping({"/list", ""})
    public ResponseResult<List<ImprovementImplementation>> list() {
        return ResponseResult.success(implementationService.list());
    }

    @GetMapping("/page")
    public ResponseResult<IPage<ImprovementImplementation>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department) {
        Page<ImprovementImplementation> page = new Page<>(current, size);
        return ResponseResult.success(implementationService.page(page, status, department));
    }

    @GetMapping("/{id}")
    public ResponseResult<ImprovementImplementation> getById(@PathVariable Long id) {
        return ResponseResult.success(implementationService.getById(id));
    }

    @PostMapping
    public ResponseResult<ImprovementImplementation> create(@RequestBody ImprovementImplementation implementation) {
        return ResponseResult.success(implementationService.create(implementation));
    }

    @PutMapping("/{id}")
    public ResponseResult<ImprovementImplementation> update(@PathVariable Long id, @RequestBody ImprovementImplementation implementation) {
        implementation.setId(id);
        return ResponseResult.success(implementationService.update(implementation));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        implementationService.deleteById(id);
        return ResponseResult.success();
    }

    @DeleteMapping("/batch")
    public ResponseResult<Void> batchDelete(@RequestBody List<Long> ids) {
        implementationService.batchDelete(ids);
        return ResponseResult.success();
    }

    @PostMapping("/{id}/start")
    public ResponseResult<ImprovementImplementation> start(@PathVariable Long id) {
        return ResponseResult.success(implementationService.start(id));
    }

    @PostMapping("/{id}/pause")
    public ResponseResult<ImprovementImplementation> pause(@PathVariable Long id) {
        return ResponseResult.success(implementationService.pause(id));
    }

    @PostMapping("/{id}/resume")
    public ResponseResult<ImprovementImplementation> resume(@PathVariable Long id) {
        return ResponseResult.success(implementationService.resume(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseResult<ImprovementImplementation> complete(@PathVariable Long id) {
        return ResponseResult.success(implementationService.complete(id));
    }
}
