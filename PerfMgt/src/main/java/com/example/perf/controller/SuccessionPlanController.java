package com.example.perf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.perf.entity.SuccessionPlan;
import com.example.perf.mapper.SuccessionPlanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 继任计划 Controller（base: /api/success-plan）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/success-plan")
public class SuccessionPlanController {

    @Autowired
    private SuccessionPlanMapper successionPlanMapper;

    @GetMapping("/list")
    public ResponseResult<List<SuccessionPlan>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<SuccessionPlan> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(SuccessionPlan::getPlanName, kw)
                    .or().like(SuccessionPlan::getPlanNo, kw)
                    .or().like(SuccessionPlan::getPositionName, kw));
        }
        if (StringUtils.hasText(status)) {
            qw.eq(SuccessionPlan::getStatus, status.trim());
        }
        qw.orderByDesc(SuccessionPlan::getId);
        return ResponseResult.success(successionPlanMapper.selectList(qw));
    }

    @GetMapping("/{id}")
    public ResponseResult<SuccessionPlan> getById(@PathVariable Long id) {
        return ResponseResult.success(successionPlanMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<SuccessionPlan> create(@RequestBody SuccessionPlan body) {
        LocalDateTime now = LocalDateTime.now();
        body.setId(null);
        body.setDeleted(0);
        body.setVersion(1);
        body.setCreateTime(now);
        body.setUpdateTime(now);
        if (!StringUtils.hasText(body.getStatus())) {
            body.setStatus("DRAFT");
        }
        successionPlanMapper.insert(body);
        return ResponseResult.success(body);
    }

    @PutMapping("/{id}")
    public ResponseResult<SuccessionPlan> update(@PathVariable Long id, @RequestBody SuccessionPlan body) {
        body.setId(id);
        body.setUpdateTime(LocalDateTime.now());
        successionPlanMapper.updateById(body);
        return ResponseResult.success(successionPlanMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        successionPlanMapper.deleteById(id);
        return ResponseResult.success();
    }
}
