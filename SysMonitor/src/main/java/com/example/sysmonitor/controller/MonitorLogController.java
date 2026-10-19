package com.example.sysmonitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmonitor.entity.MonitorHealthLog;
import com.example.sysmonitor.entity.MonitorJobLog;
import com.example.sysmonitor.entity.MonitorSyncLog;
import com.example.sysmonitor.service.MonitorHealthLogService;
import com.example.sysmonitor.service.MonitorJobLogService;
import com.example.sysmonitor.service.MonitorSyncLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/monitor/log", "/api/sys-mon/log", "/api/log-mgt"})
public class MonitorLogController {

    @Autowired
    private MonitorHealthLogService monitorHealthLogService;

    @Autowired
    private MonitorJobLogService monitorJobLogService;

    @Autowired
    private MonitorSyncLogService monitorSyncLogService;

    @GetMapping("/health/page")
    public ResponseResult<Page<MonitorHealthLog>> healthPage(@RequestParam(defaultValue = "1") int current,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(required = false) String serviceName,
                                                             @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorHealthLogService.page(current, size, serviceName, status));
    }

    @GetMapping("/health/list")
    public ResponseResult<List<MonitorHealthLog>> healthList(@RequestParam(required = false) String serviceName,
                                                         @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorHealthLogService.list(serviceName, status));
    }

    @GetMapping("/health/{id}")
    public ResponseResult<MonitorHealthLog> getHealthById(@PathVariable Long id) {
        return ResponseResult.success(monitorHealthLogService.getById(id));
    }

    @PostMapping("/health")
    public ResponseResult<Boolean> createHealth(@RequestBody MonitorHealthLog monitorHealthLog) {
        return ResponseResult.success(monitorHealthLogService.save(monitorHealthLog));
    }

    @DeleteMapping("/health/{id}")
    public ResponseResult<Boolean> deleteHealth(@PathVariable Long id) {
        return ResponseResult.success(monitorHealthLogService.removeById(id));
    }

    @DeleteMapping("/health/batch")
    public ResponseResult<Boolean> batchDeleteHealth(@RequestBody List<Long> ids) {
        return ResponseResult.success(monitorHealthLogService.removeByIds(ids));
    }

    @GetMapping("/job/page")
    public ResponseResult<Page<MonitorJobLog>> jobPage(@RequestParam(defaultValue = "1") int current,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) Long jobId,
                                                     @RequestParam(required = false) String jobName,
                                                     @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorJobLogService.page(current, size, jobId, jobName, status));
    }

    @GetMapping("/job/list")
    public ResponseResult<List<MonitorJobLog>> jobList(@RequestParam(required = false) Long jobId,
                                                       @RequestParam(required = false) String jobName,
                                                       @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorJobLogService.list(jobId, jobName, status));
    }

    @GetMapping("/job/{id}")
    public ResponseResult<MonitorJobLog> getJobById(@PathVariable Long id) {
        return ResponseResult.success(monitorJobLogService.getById(id));
    }

    @PostMapping("/job")
    public ResponseResult<Boolean> createJob(@RequestBody MonitorJobLog monitorJobLog) {
        return ResponseResult.success(monitorJobLogService.save(monitorJobLog));
    }

    @DeleteMapping("/job/{id}")
    public ResponseResult<Boolean> deleteJob(@PathVariable Long id) {
        return ResponseResult.success(monitorJobLogService.removeById(id));
    }

    @DeleteMapping("/job/batch")
    public ResponseResult<Boolean> batchDeleteJob(@RequestBody List<Long> ids) {
        return ResponseResult.success(monitorJobLogService.removeByIds(ids));
    }

    @GetMapping("/sync/page")
    public ResponseResult<Page<MonitorSyncLog>> syncPage(@RequestParam(defaultValue = "1") int current,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(required = false) String syncType,
                                                          @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorSyncLogService.page(current, size, syncType, status));
    }

    @GetMapping("/sync/list")
    public ResponseResult<List<MonitorSyncLog>> syncList(@RequestParam(required = false) String syncType,
                                                        @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorSyncLogService.list(syncType, status));
    }

    @GetMapping("/sync/{id}")
    public ResponseResult<MonitorSyncLog> getSyncById(@PathVariable Long id) {
        return ResponseResult.success(monitorSyncLogService.getById(id));
    }

    @PostMapping("/sync")
    public ResponseResult<Boolean> createSync(@RequestBody MonitorSyncLog monitorSyncLog) {
        return ResponseResult.success(monitorSyncLogService.save(monitorSyncLog));
    }

    @DeleteMapping("/sync/{id}")
    public ResponseResult<Boolean> deleteSync(@PathVariable Long id) {
        return ResponseResult.success(monitorSyncLogService.removeById(id));
    }

    @DeleteMapping("/sync/batch")
    public ResponseResult<Boolean> batchDeleteSync(@RequestBody List<Long> ids) {
        return ResponseResult.success(monitorSyncLogService.removeByIds(ids));
    }
}
