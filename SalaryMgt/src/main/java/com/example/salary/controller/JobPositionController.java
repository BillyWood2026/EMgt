package com.example.salary.controller;

import com.example.common.dto.ResponseResult;
import com.example.salary.entity.JobPosition;
import com.example.salary.service.JobPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位评估 Controller
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/salary/position")
public class JobPositionController {

    @Autowired
    private JobPositionService jobPositionService;

    /**
     * 列表查询
     */
    @GetMapping("/list")
    public ResponseResult<List<JobPosition>> list() {
        return ResponseResult.success(jobPositionService.list());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<JobPosition> getById(@PathVariable Long id) {
        return ResponseResult.success(jobPositionService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<JobPosition> create(@RequestBody JobPosition jobPosition) {
        jobPositionService.save(jobPosition);
        return ResponseResult.success(jobPosition);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ResponseResult<JobPosition> update(@PathVariable Long id, @RequestBody JobPosition jobPosition) {
        jobPosition.setId(id);
        jobPositionService.updateById(jobPosition);
        return ResponseResult.success(jobPosition);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        jobPositionService.removeById(id);
        return ResponseResult.success();
    }
}
