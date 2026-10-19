package com.example.sysmonitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmonitor.entity.MonitorMetrics;
import com.example.sysmonitor.entity.MonitorApiStats;
import com.example.sysmonitor.service.MonitorMetricsService;
import com.example.sysmonitor.service.MonitorApiStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/monitor/metrics", "/api/sys-mon/metrics"})
public class MonitorMetricsController {

    @Autowired
    private MonitorMetricsService monitorMetricsService;

    @Autowired
    private MonitorApiStatsService monitorApiStatsService;

    @GetMapping("/page")
    public ResponseResult<Page<MonitorMetrics>> page(@RequestParam(defaultValue = "1") int current,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(required = false) String metricsType,
                                                   @RequestParam(required = false) String instanceId) {
        return ResponseResult.success(monitorMetricsService.page(current, size, metricsType, instanceId));
    }

    @GetMapping("/list")
    public ResponseResult<List<MonitorMetrics>> list(@RequestParam(required = false) String metricsType,
                                                     @RequestParam(required = false) String instanceId) {
        return ResponseResult.success(monitorMetricsService.list(metricsType, instanceId));
    }

    @GetMapping("/{id}")
    public ResponseResult<MonitorMetrics> getById(@PathVariable Long id) {
        return ResponseResult.success(monitorMetricsService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody MonitorMetrics monitorMetrics) {
        return ResponseResult.success(monitorMetricsService.save(monitorMetrics));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody MonitorMetrics monitorMetrics) {
        monitorMetrics.setId(id);
        return ResponseResult.success(monitorMetricsService.updateById(monitorMetrics));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(monitorMetricsService.removeById(id));
    }

    @DeleteMapping("/batch")
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        return ResponseResult.success(monitorMetricsService.removeByIds(ids));
    }

    @GetMapping("/api/page")
    public ResponseResult<Page<MonitorApiStats>> apiPage(@RequestParam(defaultValue = "1") int current,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String apiPath,
                                                      @RequestParam(required = false) String serviceName,
                                                      @RequestParam(required = false) String statsDate) {
        return ResponseResult.success(monitorApiStatsService.page(current, size, apiPath, serviceName, statsDate));
    }

    @GetMapping("/api/list")
    public ResponseResult<List<MonitorApiStats>> apiList(@RequestParam(required = false) String apiPath,
                                                       @RequestParam(required = false) String serviceName,
                                                       @RequestParam(required = false) String statsDate) {
        return ResponseResult.success(monitorApiStatsService.list(apiPath, serviceName, statsDate));
    }

    @GetMapping("/api/{id}")
    public ResponseResult<MonitorApiStats> getApiById(@PathVariable Long id) {
        return ResponseResult.success(monitorApiStatsService.getById(id));
    }

    @PostMapping("/api")
    public ResponseResult<Boolean> createApi(@RequestBody MonitorApiStats monitorApiStats) {
        return ResponseResult.success(monitorApiStatsService.save(monitorApiStats));
    }

    @PutMapping("/api/{id}")
    public ResponseResult<Boolean> updateApi(@PathVariable Long id, @RequestBody MonitorApiStats monitorApiStats) {
        monitorApiStats.setId(id);
        return ResponseResult.success(monitorApiStatsService.updateById(monitorApiStats));
    }

    @DeleteMapping("/api/{id}")
    public ResponseResult<Boolean> deleteApi(@PathVariable Long id) {
        return ResponseResult.success(monitorApiStatsService.removeById(id));
    }

    @DeleteMapping("/api/batch")
    public ResponseResult<Boolean> batchDeleteApi(@RequestBody List<Long> ids) {
        return ResponseResult.success(monitorApiStatsService.removeByIds(ids));
    }
}
