package com.example.sysmonitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmonitor.entity.MonitorAlert;
import com.example.sysmonitor.service.MonitorAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/monitor/alert", "/api/sys-mon/alert"})
public class MonitorAlertController {

    @Autowired
    private MonitorAlertService monitorAlertService;

    @GetMapping("/page")
    public ResponseResult<Page<MonitorAlert>> page(@RequestParam(defaultValue = "1") int current,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(required = false) String level,
                                                   @RequestParam(required = false) String sourceName,
                                                   @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorAlertService.page(current, size, level, sourceName, status));
    }

    @GetMapping("/list")
    public ResponseResult<List<MonitorAlert>> list(@RequestParam(required = false) String level,
                                                   @RequestParam(required = false) String sourceName,
                                                   @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorAlertService.list(level, sourceName, status));
    }

    @GetMapping("/{id}")
    public ResponseResult<MonitorAlert> getById(@PathVariable Long id) {
        return ResponseResult.success(monitorAlertService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody MonitorAlert monitorAlert) {
        return ResponseResult.success(monitorAlertService.save(monitorAlert));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody MonitorAlert monitorAlert) {
        monitorAlert.setId(id);
        return ResponseResult.success(monitorAlertService.updateById(monitorAlert));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(monitorAlertService.removeById(id));
    }

    @DeleteMapping("/batch")
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        return ResponseResult.success(monitorAlertService.removeByIds(ids));
    }
}
