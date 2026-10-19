package com.example.hrplanning.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.hrplanning.entity.KeyMetrics;
import com.example.hrplanning.service.KeyMetricsService;
import com.example.hrplanning.util.PageResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 业务指标 /api/hr-planning/metrics
 * 前端 hr-planning/Index.vue「业务指标」tab：GET /page 直接读取 {records,total}
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/hr-planning/metrics")
public class KeyMetricsController {

    @Autowired
    private KeyMetricsService keyMetricsService;

    @GetMapping("/page")
    public Map<String, Object> page(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "planNo", required = false) String planNo,
            @RequestParam(value = "metricsType", required = false) String metricsType) {
        IPage<KeyMetrics> page = keyMetricsService.page(pageNum, pageSize, planNo, metricsType);
        return PageResponses.of(page);
    }

    @GetMapping("/list")
    public ResponseResult<java.util.List<KeyMetrics>> list() {
        return ResponseResult.success(keyMetricsService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<KeyMetrics> getById(@PathVariable Long id) {
        return ResponseResult.success(keyMetricsService.getById(id));
    }

    @PostMapping
    public ResponseResult<Void> create(@RequestBody KeyMetrics metrics) {
        keyMetricsService.save(metrics);
        return ResponseResult.success();
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(@PathVariable Long id, @RequestBody KeyMetrics metrics) {
        metrics.setId(id);
        keyMetricsService.updateById(metrics);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        keyMetricsService.removeById(id);
        return ResponseResult.success();
    }
}
