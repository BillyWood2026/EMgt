package com.example.salary.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.salary.entity.JobValueAssessment;
import com.example.salary.service.JobValueAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位价值评估 Controller（对应前端 Position.vue）
 * base: /api/salary/job-value
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/salary/job-value")
public class JobValueAssessmentController {

    @Autowired
    private JobValueAssessmentService jobValueAssessmentService;

    /**
     * 列表（分页 + 岗位名称 / 价值等级搜索，返回数组）
     */
    @GetMapping("/list")
    public ResponseResult<List<JobValueAssessment>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String positionName,
            @RequestParam(required = false) String valueLevel) {
        IPage<JobValueAssessment> result = jobValueAssessmentService.pageList(
                page, size, positionName, valueLevel);
        return ResponseResult.success(result.getRecords());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<JobValueAssessment> getById(@PathVariable Long id) {
        return ResponseResult.success(jobValueAssessmentService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<JobValueAssessment> create(@RequestBody JobValueAssessment entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setId(null);
        entity.setDeleted(0);
        entity.setVersion(1);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("待评估");
        }
        fillTotal(entity);
        jobValueAssessmentService.save(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 更新（id 在 URL 上）
     */
    @PutMapping("/{id}")
    public ResponseResult<JobValueAssessment> update(@PathVariable Long id,
                                                     @RequestBody JobValueAssessment entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        fillTotal(entity);
        jobValueAssessmentService.updateById(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 更新（id 在 body 中，兼容别名）
     */
    @PutMapping
    public ResponseResult<JobValueAssessment> updateByBody(@RequestBody JobValueAssessment entity) {
        entity.setUpdateTime(LocalDateTime.now());
        fillTotal(entity);
        jobValueAssessmentService.updateById(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        jobValueAssessmentService.removeById(id);
        return ResponseResult.success();
    }

    private void fillTotal(JobValueAssessment entity) {
        if (entity.getTotalScore() == null) {
            int total = nz(entity.getKnowledgeScore())
                    + nz(entity.getResponsibilityScore())
                    + nz(entity.getEffortScore())
                    + nz(entity.getEnvironmentScore());
            entity.setTotalScore(total);
        }
    }

    private int nz(Integer value) {
        return value == null ? 0 : value;
    }
}
