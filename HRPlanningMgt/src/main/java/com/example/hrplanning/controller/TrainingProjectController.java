package com.example.hrplanning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.hrplanning.entity.TrainingProject;
import com.example.hrplanning.entity.TrainingProgress;
import com.example.hrplanning.entity.TrainingStudent;
import com.example.hrplanning.mapper.TrainingProgressMapper;
import com.example.hrplanning.mapper.TrainingProjectMapper;
import com.example.hrplanning.mapper.TrainingStudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 人才培养工程 Controller（base: /api/training-projects）
 * 删除工程时级联删除其学员与进度记录。
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/training-projects")
public class TrainingProjectController {

    @Autowired
    private TrainingProjectMapper projectMapper;
    @Autowired
    private TrainingStudentMapper studentMapper;
    @Autowired
    private TrainingProgressMapper progressMapper;

    @GetMapping("/list")
    public ResponseResult<List<TrainingProject>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<TrainingProject> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(TrainingProject::getProjectName, kw)
                    .or().like(TrainingProject::getProjectCode, kw));
        }
        if (StringUtils.hasText(status)) {
            qw.eq(TrainingProject::getStatus, status.trim());
        }
        qw.orderByDesc(TrainingProject::getId);
        return ResponseResult.success(projectMapper.selectList(qw));
    }

    @GetMapping("/{id}")
    public ResponseResult<TrainingProject> getById(@PathVariable Long id) {
        return ResponseResult.success(projectMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<TrainingProject> create(@RequestBody TrainingProject body) {
        LocalDateTime now = LocalDateTime.now();
        body.setId(null);
        body.setDeleted(0);
        body.setVersion(1);
        body.setCreateTime(now);
        body.setUpdateTime(now);
        if (!StringUtils.hasText(body.getStatus())) {
            body.setStatus("DRAFT");
        }
        projectMapper.insert(body);
        return ResponseResult.success(body);
    }

    @PutMapping("/{id}")
    public ResponseResult<TrainingProject> update(@PathVariable Long id, @RequestBody TrainingProject body) {
        body.setId(id);
        body.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(body);
        return ResponseResult.success(projectMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        studentMapper.delete(new LambdaQueryWrapper<TrainingStudent>()
                .eq(TrainingStudent::getProjectId, id));
        progressMapper.delete(new LambdaQueryWrapper<TrainingProgress>()
                .eq(TrainingProgress::getProjectId, id));
        projectMapper.deleteById(id);
        return ResponseResult.success();
    }
}
