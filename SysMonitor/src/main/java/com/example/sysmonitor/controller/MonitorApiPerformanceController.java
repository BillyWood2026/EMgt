package com.example.sysmonitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmonitor.entity.MonitorApiPerformance;
import com.example.sysmonitor.service.MonitorApiPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/monitor/performance", "/api/sys-mon/performance"})
public class MonitorApiPerformanceController {

    @Autowired
    private MonitorApiPerformanceService monitorApiPerformanceService;

    @GetMapping("/page")
    public ResponseResult<Page<MonitorApiPerformance>> page(@RequestParam(defaultValue = "1") int current,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(required = false) String api,
                                                            @RequestParam(required = false) String method,
                                                            @RequestParam(required = false) String monitorDate) {
        return ResponseResult.success(monitorApiPerformanceService.page(current, size, api, method, monitorDate));
    }

    @GetMapping("/list")
    public ResponseResult<List<MonitorApiPerformance>> list(@RequestParam(required = false) String api,
                                                            @RequestParam(required = false) String method,
                                                            @RequestParam(required = false) String monitorDate) {
        return ResponseResult.success(monitorApiPerformanceService.list(api, method, monitorDate));
    }

    @GetMapping("/{id}")
    public ResponseResult<MonitorApiPerformance> getById(@PathVariable Long id) {
        return ResponseResult.success(monitorApiPerformanceService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody MonitorApiPerformance monitorApiPerformance) {
        return ResponseResult.success(monitorApiPerformanceService.save(monitorApiPerformance));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody MonitorApiPerformance monitorApiPerformance) {
        monitorApiPerformance.setId(id);
        return ResponseResult.success(monitorApiPerformanceService.updateById(monitorApiPerformance));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(monitorApiPerformanceService.removeById(id));
    }

    @DeleteMapping("/batch")
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        return ResponseResult.success(monitorApiPerformanceService.removeByIds(ids));
    }
}
