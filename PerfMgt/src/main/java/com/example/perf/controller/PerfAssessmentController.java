package com.example.perf.controller;

import com.example.common.dto.ResponseResult;
import com.example.perf.entity.PerfAssessment;
import com.example.perf.service.PerfAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * 绩效评估 Controller（前端 Evaluation.vue，base /api/perf/assessment）
 * 页面无新增/编辑弹窗，仅提供列表查询。
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/perf/assessment")
public class PerfAssessmentController {

    /** 前端状态码 -> 列表展示的中文状态 */
    private static final Map<String, String> STATUS_MAP = Map.of(
            "pending", "待评估",
            "evaluating", "评估中",
            "pending_audit", "待审核",
            "completed", "已完成"
    );

    @Autowired
    private PerfAssessmentService perfAssessmentService;

    /**
     * 列表查询：可选 period / status / department
     */
    @GetMapping("/list")
    public ResponseResult<List<PerfAssessment>> list(@RequestParam(required = false) String period,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) String department) {
        String normalizedStatus = STATUS_MAP.getOrDefault(status == null ? "" : status.trim(), status);
        return ResponseResult.success(perfAssessmentService.search(period, normalizedStatus, department));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<PerfAssessment> getById(@PathVariable Long id) {
        return ResponseResult.success(perfAssessmentService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<PerfAssessment> create(@RequestBody PerfAssessment assessment) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        assessment.setId(null);
        assessment.setDeleted(0);
        assessment.setVersion(1);
        assessment.setCreateTime(now);
        assessment.setUpdateTime(now);
        fillScoreAndGrade(assessment);
        perfAssessmentService.save(assessment);
        return ResponseResult.success(assessment);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ResponseResult<PerfAssessment> update(@PathVariable Long id,
                                                 @RequestBody PerfAssessment assessment) {
        assessment.setId(id);
        assessment.setUpdateTime(java.time.LocalDateTime.now());
        fillScoreAndGrade(assessment);
        perfAssessmentService.updateById(assessment);
        return ResponseResult.success(perfAssessmentService.getById(id));
    }

    /**
     * 综合得分 = 自评×0.2 + 上级评×0.6 + 平级×0.2（四舍五入到整数）；
     * 等级阈值：≥90 A、≥80 B、≥60 C、&lt;60 D。
     * 仅在评分齐全时计算；前端已显式提交 score/grade 时以后端公式为准兜底。
     */
    private void fillScoreAndGrade(PerfAssessment a) {
        if (a.getSelfScore() != null && a.getSuperiorScore() != null && a.getPeerScore() != null) {
            BigDecimal composite = a.getSelfScore().multiply(new BigDecimal("0.2"))
                    .add(a.getSuperiorScore().multiply(new BigDecimal("0.6")))
                    .add(a.getPeerScore().multiply(new BigDecimal("0.2")))
                    .setScale(0, RoundingMode.HALF_UP);
            a.setScore(composite);
            if (!StringUtils.hasText(a.getGrade())) {
                int v = composite.intValue();
                a.setGrade(v >= 90 ? "A" : v >= 80 ? "B" : v >= 60 ? "C" : "D");
            }
        }
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        perfAssessmentService.removeById(id);
        return ResponseResult.success();
    }
}
