package com.example.hrplanning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.hrplanning.entity.TrainingProgress;
import com.example.hrplanning.mapper.TrainingProgressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 培养过程监控通用 Controller（/api/training-monitoring/progress 与 /api/training-progress 共用）。
 * 综合进度由后端统一计算：overall = theory*0.1 + practice*0.7 + mentor*0.2（四舍五入）。
 */
public abstract class AbstractTrainingProgressController {

    @Autowired
    protected TrainingProgressMapper progressMapper;

    /**
     * 综合进度计算；任一维度缺失按 0 处理。
     */
    static int calcOverall(Integer theory, Integer practice, Integer mentor) {
        int t = theory == null ? 0 : theory;
        int p = practice == null ? 0 : practice;
        int m = mentor == null ? 0 : mentor;
        return (int) Math.round(t * 0.1 + p * 0.7 + m * 0.2);
    }

    private void fill(TrainingProgress body) {
        if (body.getTheoryProgress() != null || body.getPracticeProgress() != null
                || body.getMentorProgress() != null) {
            body.setOverallProgress(calcOverall(body.getTheoryProgress(),
                    body.getPracticeProgress(), body.getMentorProgress()));
        }
        if ("CLOSED".equals(body.getStatus()) && !StringUtils.hasText(body.getCloseTime())) {
            body.setCloseTime(LocalDate.now().toString());
        }
    }

    @GetMapping("/list")
    public ResponseResult<List<TrainingProgress>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<TrainingProgress> qw = new LambdaQueryWrapper<>();
        if (projectId != null) {
            qw.eq(TrainingProgress::getProjectId, projectId);
        }
        if (StringUtils.hasText(studentName)) {
            qw.like(TrainingProgress::getStudentName, studentName.trim());
        }
        if (StringUtils.hasText(status)) {
            qw.eq(TrainingProgress::getStatus, status.trim());
        }
        qw.orderByDesc(TrainingProgress::getId);
        return ResponseResult.success(progressMapper.selectList(qw));
    }

    @GetMapping("/{id}")
    public ResponseResult<TrainingProgress> getById(@PathVariable Long id) {
        return ResponseResult.success(progressMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<TrainingProgress> create(@RequestBody TrainingProgress body) {
        LocalDateTime now = LocalDateTime.now();
        body.setId(null);
        body.setDeleted(0);
        body.setVersion(1);
        body.setCreateTime(now);
        body.setUpdateTime(now);
        if (!StringUtils.hasText(body.getStatus())) {
            body.setStatus("ONGOING");
        }
        fill(body);
        progressMapper.insert(body);
        return ResponseResult.success(body);
    }

    @PutMapping("/{id}")
    public ResponseResult<TrainingProgress> update(@PathVariable Long id, @RequestBody TrainingProgress body) {
        body.setId(id);
        body.setUpdateTime(LocalDateTime.now());
        fill(body);
        progressMapper.updateById(body);
        return ResponseResult.success(progressMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        progressMapper.deleteById(id);
        return ResponseResult.success();
    }
}
