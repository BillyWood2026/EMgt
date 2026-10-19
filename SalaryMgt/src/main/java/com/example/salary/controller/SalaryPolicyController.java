package com.example.salary.controller;

import com.example.common.dto.ResponseResult;
import com.example.salary.entity.SalaryPolicy;
import com.example.salary.service.SalaryPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 薪酬政策 Controller
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/salary/policy")
public class SalaryPolicyController {

    @Autowired
    private SalaryPolicyService salaryPolicyService;

    /**
     * 列表查询
     */
    @GetMapping("/list")
    public ResponseResult<List<SalaryPolicy>> list() {
        return ResponseResult.success(salaryPolicyService.list());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<SalaryPolicy> getById(@PathVariable Long id) {
        return ResponseResult.success(salaryPolicyService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<SalaryPolicy> create(@RequestBody SalaryPolicy salaryPolicy) {
        salaryPolicyService.save(salaryPolicy);
        return ResponseResult.success(salaryPolicy);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ResponseResult<SalaryPolicy> update(@PathVariable Long id, @RequestBody SalaryPolicy salaryPolicy) {
        salaryPolicy.setId(id);
        salaryPolicyService.updateById(salaryPolicy);
        return ResponseResult.success(salaryPolicy);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        salaryPolicyService.removeById(id);
        return ResponseResult.success();
    }
}
