package com.example.perf.controller;

import com.example.common.dto.ResponseResult;
import com.example.perf.entity.PerfAssessment;
import com.example.perf.service.PerfAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 兼容旧路径 /api/perf/evaluation 的 Controller（健康检查使用 GET /list），
 * 底层复用 perf_assessment 真实持久层。
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/perf/evaluation")
public class PerformanceEvaluationController {

    private static final Map<String, String> STATUS_MAP = Map.of(
            "pending", "待评估",
            "evaluating", "评估中",
            "pending_audit", "待审核",
            "completed", "已完成"
    );

    @Autowired
    private PerfAssessmentService perfAssessmentService;

    @GetMapping("/list")
    public ResponseResult<List<PerfAssessment>> list(@RequestParam(required = false) String period,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) String department) {
        String normalizedStatus = STATUS_MAP.getOrDefault(status == null ? "" : status.trim(), status);
        return ResponseResult.success(perfAssessmentService.search(period, normalizedStatus, department));
    }

    @GetMapping("/{id}")
    public ResponseResult<PerfAssessment> getById(@PathVariable Long id) {
        return ResponseResult.success(perfAssessmentService.getById(id));
    }

    @PostMapping
    public ResponseResult<PerfAssessment> create(@RequestBody PerfAssessment assessment) {
        LocalDateTime now = LocalDateTime.now();
        assessment.setId(null);
        assessment.setDeleted(0);
        assessment.setVersion(1);
        assessment.setCreateTime(now);
        assessment.setUpdateTime(now);
        perfAssessmentService.save(assessment);
        return ResponseResult.success(assessment);
    }

    @PutMapping("/{id}")
    public ResponseResult<PerfAssessment> update(@PathVariable Long id,
                                                 @RequestBody PerfAssessment assessment) {
        assessment.setId(id);
        assessment.setUpdateTime(LocalDateTime.now());
        perfAssessmentService.updateById(assessment);
        return ResponseResult.success(perfAssessmentService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        perfAssessmentService.removeById(id);
        return ResponseResult.success();
    }
}
