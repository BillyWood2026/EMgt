package com.example.onlineexam.controller;

import com.example.common.dto.ResponseResult;
import com.example.onlineexam.entity.ExamEvaluation;
import com.example.onlineexam.service.ExamEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考试成绩/结果（learn-to-exam/e-examing/Result.vue，前缀 /api/learnexam/evaluation）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/learnexam/evaluation")
public class ExamEvaluationController {

    @Autowired
    private ExamEvaluationService examEvaluationService;

    /** 成绩列表（前端读取 data.records || data.data，返回数组即可） */
    @GetMapping("/list")
    public ResponseResult<List<ExamEvaluation>> list(@RequestParam(required = false) Integer pageNum,
                                                       @RequestParam(required = false) Integer pageSize,
                                                       @RequestParam(required = false) String employee,
                                                       @RequestParam(required = false) String examName,
                                                       @RequestParam(required = false) String passed) {
        return ResponseResult.success(examEvaluationService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<ExamEvaluation> getById(@PathVariable Long id) {
        return ResponseResult.success(examEvaluationService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody ExamEvaluation examEvaluation) {
        return ResponseResult.success(examEvaluationService.save(examEvaluation));
    }

    /** 更新（id 在请求体中，路径无 /{id}） */
    @PutMapping
    public ResponseResult<Boolean> update(@RequestBody ExamEvaluation examEvaluation) {
        return ResponseResult.success(examEvaluationService.updateById(examEvaluation));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(examEvaluationService.removeById(id));
    }

    /**
     * 发放证书：成绩达到及格线方可发放，hasCertificate 置 1，certificateStatus 置 issued。
     * 兼容前端两种路径：/{id}/issue-certificate 与 /issue-certificate/{id}。
     */
    @PostMapping({"/{id}/issue-certificate", "/issue-certificate/{id}"})
    public ResponseResult<Boolean> issueCertificate(@PathVariable Long id) {
        ExamEvaluation evaluation = examEvaluationService.getById(id);
        if (evaluation == null) {
            return ResponseResult.success(false);
        }
        // 后端合格校验，防止绕过前端直接发证。
        if (evaluation.getPassScore() != null && evaluation.getOverallScore() != null
                && evaluation.getOverallScore().compareTo(java.math.BigDecimal.valueOf(evaluation.getPassScore())) < 0) {
            return ResponseResult.error(400, "成绩未达到及格线，无法发放证书");
        }
        evaluation.setHasCertificate(1);
        evaluation.setCertificateStatus("issued");
        return ResponseResult.success(examEvaluationService.updateById(evaluation));
    }
}
