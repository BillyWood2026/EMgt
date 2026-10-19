package com.example.sysmonitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmonitor.entity.MonitorService;
import com.example.sysmonitor.service.MonitorServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/monitor/service", "/api/sys-mon/service"})
public class MonitorServiceController {

    @Autowired
    private MonitorServiceService monitorServiceService;

    @GetMapping("/page")
    public ResponseResult<Page<MonitorService>> page(@RequestParam(defaultValue = "1") int current,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String serviceName,
                                                      @RequestParam(required = false) String serviceType,
                                                      @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorServiceService.page(current, size, serviceName, serviceType, status));
    }

    @GetMapping("/list")
    public ResponseResult<List<MonitorService>> list(@RequestParam(required = false) String serviceName,
                                                    @RequestParam(required = false) String serviceType,
                                                    @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorServiceService.list(serviceName, serviceType, status));
    }

    @GetMapping("/{id}")
    public ResponseResult<MonitorService> getById(@PathVariable Long id) {
        return ResponseResult.success(monitorServiceService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody MonitorService monitorService) {
        return ResponseResult.success(monitorServiceService.save(monitorService));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody MonitorService monitorService) {
        monitorService.setId(id);
        return ResponseResult.success(monitorServiceService.updateById(monitorService));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(monitorServiceService.removeById(id));
    }

    @DeleteMapping("/batch")
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        return ResponseResult.success(monitorServiceService.removeByIds(ids));
    }
}
