package com.example.hrplanning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.hrplanning.entity.TrainingStandard;
import com.example.hrplanning.mapper.TrainingStandardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 培养标准 Controller（base: /api/training-standards）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/training-standards")
public class TrainingStandardController {

    @Autowired
    private TrainingStandardMapper standardMapper;

    @GetMapping("/list")
    public ResponseResult<List<TrainingStandard>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        LambdaQueryWrapper<TrainingStandard> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(TrainingStandard::getStandardName, kw)
                    .or().like(TrainingStandard::getStandardCode, kw));
        }
        if (StringUtils.hasText(category)) {
            qw.eq(TrainingStandard::getCategory, category.trim());
        }
        qw.orderByDesc(TrainingStandard::getId);
        return ResponseResult.success(standardMapper.selectList(qw));
    }

    @GetMapping("/{id}")
    public ResponseResult<TrainingStandard> getById(@PathVariable Long id) {
        return ResponseResult.success(standardMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<TrainingStandard> create(@RequestBody TrainingStandard body) {
        LocalDateTime now = LocalDateTime.now();
        body.setId(null);
        body.setDeleted(0);
        body.setVersion(1);
        body.setCreateTime(now);
        body.setUpdateTime(now);
        if (!StringUtils.hasText(body.getStatus())) {
            body.setStatus("ACTIVE");
        }
        standardMapper.insert(body);
        return ResponseResult.success(body);
    }

    @PutMapping("/{id}")
    public ResponseResult<TrainingStandard> update(@PathVariable Long id, @RequestBody TrainingStandard body) {
        body.setId(id);
        body.setUpdateTime(LocalDateTime.now());
        standardMapper.updateById(body);
        return ResponseResult.success(standardMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        standardMapper.deleteById(id);
        return ResponseResult.success();
    }
}
