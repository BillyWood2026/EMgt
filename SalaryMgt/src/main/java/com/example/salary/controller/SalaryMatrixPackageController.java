package com.example.salary.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.salary.entity.SalaryMatrixPackage;
import com.example.salary.service.SalaryMatrixPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 矩阵包 / 薪酬结构 Controller（对应前端 Structure.vue）
 * base: /api/salary/matrix-package
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/salary/matrix-package")
public class SalaryMatrixPackageController {

    @Autowired
    private SalaryMatrixPackageService salaryMatrixPackageService;

    /**
     * 列表（分页 + 关键字搜索，返回数组）
     */
    @GetMapping("/list")
    public ResponseResult<List<SalaryMatrixPackage>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "100") long size,
            @RequestParam(required = false) String keyword) {
        IPage<SalaryMatrixPackage> result = salaryMatrixPackageService.pageList(page, size, keyword);
        return ResponseResult.success(result.getRecords());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<SalaryMatrixPackage> getById(@PathVariable Long id) {
        return ResponseResult.success(salaryMatrixPackageService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<SalaryMatrixPackage> create(@RequestBody SalaryMatrixPackage entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setId(null);
        entity.setDeleted(0);
        entity.setVersion(1);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("生效");
        }
        fillTotal(entity);
        salaryMatrixPackageService.save(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 更新（id 在 URL 上，Structure.vue 使用此调用）
     */
    @PutMapping("/{id}")
    public ResponseResult<SalaryMatrixPackage> update(@PathVariable Long id,
                                                      @RequestBody SalaryMatrixPackage entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        fillTotal(entity);
        salaryMatrixPackageService.updateById(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 更新（id 在 body 中，兼容别名）
     */
    @PutMapping
    public ResponseResult<SalaryMatrixPackage> updateByBody(@RequestBody SalaryMatrixPackage entity) {
        entity.setUpdateTime(LocalDateTime.now());
        fillTotal(entity);
        salaryMatrixPackageService.updateById(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        salaryMatrixPackageService.removeById(id);
        return ResponseResult.success();
    }

    private void fillTotal(SalaryMatrixPackage entity) {
        if (entity.getTotalSalary() == null) {
            BigDecimal basic = entity.getBasicSalary() == null ? BigDecimal.ZERO : entity.getBasicSalary();
            BigDecimal performance = entity.getPerformanceSalary() == null
                    ? BigDecimal.ZERO : entity.getPerformanceSalary();
            entity.setTotalSalary(basic.add(performance));
        }
    }
}
