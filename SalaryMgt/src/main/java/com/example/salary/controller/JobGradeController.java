package com.example.salary.controller;

import com.example.common.dto.ResponseResult;
import com.example.salary.entity.JobGrade;
import com.example.salary.service.JobGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位等级 Controller
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/salary/grade")
public class JobGradeController {

    @Autowired
    private JobGradeService jobGradeService;

    /**
     * 列表查询
     */
    @GetMapping("/list")
    public ResponseResult<List<JobGrade>> list() {
        return ResponseResult.success(jobGradeService.list());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<JobGrade> getById(@PathVariable Long id) {
        return ResponseResult.success(jobGradeService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<JobGrade> create(@RequestBody JobGrade jobGrade) {
        jobGradeService.save(jobGrade);
        return ResponseResult.success(jobGrade);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ResponseResult<JobGrade> update(@PathVariable Long id, @RequestBody JobGrade jobGrade) {
        jobGrade.setId(id);
        jobGradeService.updateById(jobGrade);
        return ResponseResult.success(jobGrade);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        jobGradeService.removeById(id);
        return ResponseResult.success();
    }
}
