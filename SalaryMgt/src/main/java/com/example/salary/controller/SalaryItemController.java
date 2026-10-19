package com.example.salary.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.salary.entity.SalaryItem;
import com.example.salary.service.SalaryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 薪资项目 / 薪资政策 Controller（对应前端 Policy.vue）
 * base: /api/salary/salary-item
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/salary/salary-item")
public class SalaryItemController {

    @Autowired
    private SalaryItemService salaryItemService;

    /**
     * 列表（分页，返回数组）
     */
    @GetMapping("/list")
    public ResponseResult<List<SalaryItem>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "100") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type) {
        IPage<SalaryItem> result = salaryItemService.pageList(page, size, name, type);
        return ResponseResult.success(result.getRecords());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<SalaryItem> getById(@PathVariable Long id) {
        return ResponseResult.success(salaryItemService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<SalaryItem> create(@RequestBody SalaryItem entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setId(null);
        entity.setDeleted(0);
        entity.setVersion(1);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("生效");
        }
        salaryItemService.save(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 更新（id 在 URL 上，Policy.vue 的编辑 / 停用均使用此调用）
     */
    @PutMapping("/{id}")
    public ResponseResult<SalaryItem> update(@PathVariable Long id,
                                             @RequestBody SalaryItem entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        salaryItemService.updateById(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 更新（id 在 body 中，兼容别名）
     */
    @PutMapping
    public ResponseResult<SalaryItem> updateByBody(@RequestBody SalaryItem entity) {
        entity.setUpdateTime(LocalDateTime.now());
        salaryItemService.updateById(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        salaryItemService.removeById(id);
        return ResponseResult.success();
    }
}
