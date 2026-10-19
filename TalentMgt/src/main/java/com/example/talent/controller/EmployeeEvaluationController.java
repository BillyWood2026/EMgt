package com.example.talent.controller;

import com.example.common.dto.ResponseResult;
import com.example.talent.entity.EmployeeEvaluation;
import com.example.talent.service.EmployeeEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工测评 / 员工评估 Controller
 * 对齐页面: talent-devep/Assess.vue、comp-assess/Evaluation.vue
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/evaluation/employee")
public class EmployeeEvaluationController {

    @Autowired
    private EmployeeEvaluationService employeeEvaluationService;

    /**
     * 测评/评估列表（数组）。所有筛选参数可选。
     */
    @GetMapping("/search")
    public ResponseResult<List<EmployeeEvaluation>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String orgName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String batchCode,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String status) {
        String dept = StringUtils.hasText(orgName) ? orgName : department;
        String cycle = StringUtils.hasText(batchCode) ? batchCode : period;
        return ResponseResult.success(
                employeeEvaluationService.search(keyword, employeeName, dept, cycle, status));
    }

    /**
     * 发起测评 / 新建评估。body 带 id 或已存在的 recordId 时执行更新（幂等 upsert）。
     */
    @PostMapping
    public ResponseResult<EmployeeEvaluation> create(@RequestBody EmployeeEvaluation body) {
        return ResponseResult.success(employeeEvaluationService.saveOrUpdateByBusinessKey(body));
    }

    /**
     * 编辑测评/评估（id 在 body 中；前端亦可能只提交 recordId 作为定位键）。
     */
    @PutMapping
    public ResponseResult<EmployeeEvaluation> update(@RequestBody EmployeeEvaluation body) {
        return ResponseResult.success(employeeEvaluationService.saveOrUpdateByBusinessKey(body));
    }

    /**
     * 删除测评/评估。路径键支持数据库自增 id 或业务编号 recordId。
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable("id") String id) {
        employeeEvaluationService.removeByIdOrKey(id);
        return ResponseResult.success();
    }
}
