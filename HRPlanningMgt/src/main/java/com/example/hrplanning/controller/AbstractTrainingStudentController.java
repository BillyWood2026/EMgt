package com.example.hrplanning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.hrplanning.entity.TrainingStudent;
import com.example.hrplanning.mapper.TrainingStudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 培养学员通用 Controller（/api/training-monitoring/students 与 /api/training-students 共用）
 */
public abstract class AbstractTrainingStudentController {

    @Autowired
    protected TrainingStudentMapper studentMapper;

    @GetMapping("/list")
    public ResponseResult<List<TrainingStudent>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String enrollStatus) {
        LambdaQueryWrapper<TrainingStudent> qw = new LambdaQueryWrapper<>();
        if (projectId != null) {
            qw.eq(TrainingStudent::getProjectId, projectId);
        }
        if (StringUtils.hasText(studentName)) {
            qw.like(TrainingStudent::getStudentName, studentName.trim());
        }
        if (StringUtils.hasText(enrollStatus)) {
            qw.eq(TrainingStudent::getEnrollStatus, enrollStatus.trim());
        }
        qw.orderByDesc(TrainingStudent::getId);
        return ResponseResult.success(studentMapper.selectList(qw));
    }

    @GetMapping("/{id}")
    public ResponseResult<TrainingStudent> getById(@PathVariable Long id) {
        return ResponseResult.success(studentMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<TrainingStudent> create(@RequestBody TrainingStudent body) {
        LocalDateTime now = LocalDateTime.now();
        body.setId(null);
        body.setDeleted(0);
        body.setVersion(1);
        body.setCreateTime(now);
        body.setUpdateTime(now);
        if (!StringUtils.hasText(body.getEnrollStatus())) {
            body.setEnrollStatus("ENROLLED");
        }
        studentMapper.insert(body);
        return ResponseResult.success(body);
    }

    @PutMapping("/{id}")
    public ResponseResult<TrainingStudent> update(@PathVariable Long id, @RequestBody TrainingStudent body) {
        body.setId(id);
        body.setUpdateTime(LocalDateTime.now());
        studentMapper.updateById(body);
        return ResponseResult.success(studentMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        studentMapper.deleteById(id);
        return ResponseResult.success();
    }
}
