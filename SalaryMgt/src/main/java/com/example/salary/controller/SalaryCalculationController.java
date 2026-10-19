package com.example.salary.controller;

import com.example.common.dto.ResponseResult;
import com.example.salary.entity.SalaryCalculation;
import com.example.salary.service.SalaryCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 薪资核算 Controller
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/salary/calculation")
public class SalaryCalculationController {

    @Autowired
    private SalaryCalculationService salaryCalculationService;

    /**
     * 列表查询
     */
    @GetMapping("/list")
    public ResponseResult<List<SalaryCalculation>> list() {
        return ResponseResult.success(salaryCalculationService.list());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<SalaryCalculation> getById(@PathVariable Long id) {
        return ResponseResult.success(salaryCalculationService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<SalaryCalculation> create(@RequestBody SalaryCalculation salaryCalculation) {
        salaryCalculationService.save(salaryCalculation);
        return ResponseResult.success(salaryCalculation);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ResponseResult<SalaryCalculation> update(@PathVariable Long id, @RequestBody SalaryCalculation salaryCalculation) {
        salaryCalculation.setId(id);
        salaryCalculationService.updateById(salaryCalculation);
        return ResponseResult.success(salaryCalculation);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        salaryCalculationService.removeById(id);
        return ResponseResult.success();
    }
}
