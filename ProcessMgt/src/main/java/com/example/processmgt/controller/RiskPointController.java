package com.example.processmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.processmgt.entity.RiskPoint;
import com.example.processmgt.service.RiskPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 流程风险点 Controller
 */
@RestController
@RequestMapping("/api/process/risk-point")
public class RiskPointController {

    private static final List<String> SEVERITIES = Arrays.asList("high", "medium", "low");

    @Autowired
    private RiskPointService service;

    @GetMapping("/list")
    public ResponseResult<List<RiskPoint>> list(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String severity) {
        QueryWrapper<RiskPoint> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("name", keyword).or().like("code", keyword)
                    .or().like("related_flow", keyword).or().like("control_measure", keyword));
        }
        if (StringUtils.hasText(severity)) wrapper.eq("severity", severity);
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<RiskPoint> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<RiskPoint> create(@RequestBody RiskPoint entity) {
        validate(entity);
        entity.setId(null);
        entity.setCode(nextCode());
        if (!StringUtils.hasText(entity.getSeverity())) entity.setSeverity("medium");
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        service.save(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<RiskPoint> update(@PathVariable Long id, @RequestBody RiskPoint entity) {
        validate(entity);
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        service.updateById(entity);
        return ResponseResult.success(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return ResponseResult.success();
    }

    private void validate(RiskPoint entity) {
        if (!StringUtils.hasText(entity.getName())) {
            throw new IllegalArgumentException("风险点名称必填");
        }
        if (StringUtils.hasText(entity.getSeverity()) && !SEVERITIES.contains(entity.getSeverity())) {
            throw new IllegalArgumentException("风险等级仅支持 high/medium/low");
        }
    }

    private String nextCode() {
        String prefix = "RP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long cnt = service.count(new QueryWrapper<RiskPoint>().likeRight("code", prefix));
        return prefix + String.format("%04d", cnt + 1);
    }
}
