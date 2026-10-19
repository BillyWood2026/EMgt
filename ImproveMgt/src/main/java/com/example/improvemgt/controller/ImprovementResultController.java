package com.example.improvemgt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.improvemgt.entity.ImprovementResult;
import com.example.improvemgt.service.ImprovementResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/improve/result")
public class ImprovementResultController {

    @Autowired
    private ImprovementResultService resultService;

    @GetMapping({"/list", ""})
    public ResponseResult<List<ImprovementResult>> list() {
        return ResponseResult.success(resultService.list());
    }

    @GetMapping("/page")
    public ResponseResult<IPage<ImprovementResult>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        Page<ImprovementResult> page = new Page<>(current, size);
        return ResponseResult.success(resultService.page(page, status, type));
    }

    @GetMapping("/{id}")
    public ResponseResult<ImprovementResult> getById(@PathVariable Long id) {
        return ResponseResult.success(resultService.getById(id));
    }

    @PostMapping
    public ResponseResult<ImprovementResult> create(@RequestBody ImprovementResult result) {
        return ResponseResult.success(resultService.create(result));
    }

    @PutMapping("/{id}")
    public ResponseResult<ImprovementResult> update(@PathVariable Long id, @RequestBody ImprovementResult result) {
        result.setId(id);
        return ResponseResult.success(resultService.update(result));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        resultService.deleteById(id);
        return ResponseResult.success();
    }

    @DeleteMapping("/batch")
    public ResponseResult<Void> batchDelete(@RequestBody List<Long> ids) {
        resultService.batchDelete(ids);
        return ResponseResult.success();
    }

    @PostMapping("/{id}/confirm")
    public ResponseResult<ImprovementResult> confirm(@PathVariable Long id) {
        return ResponseResult.success(resultService.confirm(id));
    }

    @PostMapping("/{id}/award")
    public ResponseResult<ImprovementResult> award(@PathVariable Long id,
                                                   @RequestBody(required = false) java.util.Map<String, Object> body) {
        java.math.BigDecimal amount = null;
        if (body != null && body.get("rewardAmount") != null) {
            amount = new java.math.BigDecimal(String.valueOf(body.get("rewardAmount")));
        }
        return ResponseResult.success(resultService.award(id, amount));
    }
}
