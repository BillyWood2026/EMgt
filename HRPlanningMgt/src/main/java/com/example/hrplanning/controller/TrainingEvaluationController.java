package com.example.hrplanning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.hrplanning.entity.TrainingEvaluation;
import com.example.hrplanning.mapper.TrainingEvaluationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 培养效果评价 Controller（base: /api/training-evaluations）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/training-evaluations")
public class TrainingEvaluationController {

    @Autowired
    private TrainingEvaluationMapper evaluationMapper;

    @GetMapping("/list")
    public ResponseResult<List<TrainingEvaluation>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<TrainingEvaluation> qw = new LambdaQueryWrapper<>();
        if (projectId != null) {
            qw.eq(TrainingEvaluation::getProjectId, projectId);
        }
        if (StringUtils.hasText(studentName)) {
            qw.like(TrainingEvaluation::getStudentName, studentName.trim());
        }
        if (StringUtils.hasText(status)) {
            qw.eq(TrainingEvaluation::getStatus, status.trim());
        }
        qw.orderByDesc(TrainingEvaluation::getId);
        return ResponseResult.success(evaluationMapper.selectList(qw));
    }

    @GetMapping("/{id}")
    public ResponseResult<TrainingEvaluation> getById(@PathVariable Long id) {
        return ResponseResult.success(evaluationMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<TrainingEvaluation> create(@RequestBody TrainingEvaluation body) {
        LocalDateTime now = LocalDateTime.now();
        body.setId(null);
        body.setDeleted(0);
        body.setVersion(1);
        body.setCreateTime(now);
        body.setUpdateTime(now);
        if (!StringUtils.hasText(body.getStatus())) {
            body.setStatus("DRAFT");
        }
        evaluationMapper.insert(body);
        return ResponseResult.success(body);
    }

    @PutMapping("/{id}")
    public ResponseResult<TrainingEvaluation> update(@PathVariable Long id,
                                                     @RequestBody TrainingEvaluation body) {
        body.setId(id);
        body.setUpdateTime(LocalDateTime.now());
        evaluationMapper.updateById(body);
        return ResponseResult.success(evaluationMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        evaluationMapper.deleteById(id);
        return ResponseResult.success();
    }
}
