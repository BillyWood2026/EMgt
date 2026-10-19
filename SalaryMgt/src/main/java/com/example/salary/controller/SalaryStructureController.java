package com.example.salary.controller;

import com.example.common.dto.ResponseResult;
import com.example.salary.entity.SalaryStructure;
import com.example.salary.service.SalaryStructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 薪酬体系 Controller
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/salary/structure")
public class SalaryStructureController {

    @Autowired
    private SalaryStructureService salaryStructureService;

    /**
     * 列表查询
     */
    @GetMapping("/list")
    public ResponseResult<List<SalaryStructure>> list() {
        return ResponseResult.success(salaryStructureService.list());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<SalaryStructure> getById(@PathVariable Long id) {
        return ResponseResult.success(salaryStructureService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<SalaryStructure> create(@RequestBody SalaryStructure salaryStructure) {
        salaryStructureService.save(salaryStructure);
        return ResponseResult.success(salaryStructure);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ResponseResult<SalaryStructure> update(@PathVariable Long id, @RequestBody SalaryStructure salaryStructure) {
        salaryStructure.setId(id);
        salaryStructureService.updateById(salaryStructure);
        return ResponseResult.success(salaryStructure);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        salaryStructureService.removeById(id);
        return ResponseResult.success();
    }
}
