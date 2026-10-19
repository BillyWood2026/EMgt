package com.example.salary.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.salary.entity.EmployeeSalary;
import com.example.salary.service.EmployeeSalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工薪资 / 职级薪资 Controller（对应前端 Grade.vue）
 * base: /api/salary/employee-salary
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/salary/employee-salary")
public class EmployeeSalaryController {

    @Autowired
    private EmployeeSalaryService employeeSalaryService;

    /**
     * 列表（分页 + 员工姓名 / 职级 / 部门搜索，返回数组）
     */
    @GetMapping("/list")
    public ResponseResult<List<EmployeeSalary>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String jobLevel,
            @RequestParam(required = false) String department) {
        IPage<EmployeeSalary> result = employeeSalaryService.pageList(
                page, size, employeeName, jobLevel, department);
        return ResponseResult.success(result.getRecords());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<EmployeeSalary> getById(@PathVariable Long id) {
        return ResponseResult.success(employeeSalaryService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<EmployeeSalary> create(@RequestBody EmployeeSalary entity) {
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
        employeeSalaryService.save(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 更新（id 在 URL 上）
     */
    @PutMapping("/{id}")
    public ResponseResult<EmployeeSalary> update(@PathVariable Long id,
                                                 @RequestBody EmployeeSalary entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        fillTotal(entity);
        employeeSalaryService.updateById(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 更新（id 在 body 中，兼容别名）
     */
    @PutMapping
    public ResponseResult<EmployeeSalary> updateByBody(@RequestBody EmployeeSalary entity) {
        entity.setUpdateTime(LocalDateTime.now());
        fillTotal(entity);
        employeeSalaryService.updateById(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        employeeSalaryService.removeById(id);
        return ResponseResult.success();
    }

    private void fillTotal(EmployeeSalary entity) {
        if (entity.getTotalSalary() == null) {
            BigDecimal basic = entity.getBasicSalary() == null ? BigDecimal.ZERO : entity.getBasicSalary();
            BigDecimal performance = entity.getPerformanceSalary() == null
                    ? BigDecimal.ZERO : entity.getPerformanceSalary();
            BigDecimal allowance = entity.getAllowance() == null
                    ? BigDecimal.ZERO : entity.getAllowance();
            entity.setTotalSalary(basic.add(performance).add(allowance));
        }
    }
}
