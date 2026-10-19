package com.example.sysmonitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmonitor.entity.MonitorJob;
import com.example.sysmonitor.service.MonitorJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/monitor/job", "/api/sys-mon/job"})
public class MonitorJobController {

    @Autowired
    private MonitorJobService monitorJobService;

    @GetMapping("/page")
    public ResponseResult<Page<MonitorJob>> page(@RequestParam(defaultValue = "1") int current,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(required = false) String jobName,
                                                 @RequestParam(required = false) String jobGroup,
                                                 @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorJobService.page(current, size, jobName, jobGroup, status));
    }

    @GetMapping("/list")
    public ResponseResult<List<MonitorJob>> list(@RequestParam(required = false) String jobName,
                                                 @RequestParam(required = false) String jobGroup,
                                                 @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorJobService.list(jobName, jobGroup, status));
    }

    @GetMapping("/{id}")
    public ResponseResult<MonitorJob> getById(@PathVariable Long id) {
        return ResponseResult.success(monitorJobService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody MonitorJob monitorJob) {
        return ResponseResult.success(monitorJobService.save(monitorJob));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody MonitorJob monitorJob) {
        monitorJob.setId(id);
        return ResponseResult.success(monitorJobService.updateById(monitorJob));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(monitorJobService.removeById(id));
    }

    @DeleteMapping("/batch")
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        return ResponseResult.success(monitorJobService.removeByIds(ids));
    }
}
