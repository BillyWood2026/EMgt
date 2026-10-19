package com.example.talent.controller;

import com.example.common.dto.ResponseResult;
import com.example.talent.entity.EmployeeEvaluation;
import com.example.talent.service.EmployeeEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 人才模块通用 / 健康检查接口
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/talent")
public class TalentController {

    @Autowired
    private EmployeeEvaluationService employeeEvaluationService;

    /**
     * 健康检查 / 模块探活：返回员工测评列表（数组），HTTP 200。
     */
    @GetMapping("/list")
    public ResponseResult<List<EmployeeEvaluation>> list() {
        return ResponseResult.success(employeeEvaluationService.list());
    }
}
