package com.example.sysmonitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmonitor.entity.MonitorDatabase;
import com.example.sysmonitor.service.MonitorDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/monitor/database", "/api/sys-mon/database"})
public class MonitorDatabaseController {

    @Autowired
    private MonitorDatabaseService monitorDatabaseService;

    @GetMapping("/page")
    public ResponseResult<Page<MonitorDatabase>> page(@RequestParam(defaultValue = "1") int current,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String name,
                                                      @RequestParam(required = false) String type,
                                                      @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorDatabaseService.page(current, size, name, type, status));
    }

    @GetMapping("/list")
    public ResponseResult<List<MonitorDatabase>> list(@RequestParam(required = false) String name,
                                                      @RequestParam(required = false) String type,
                                                      @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorDatabaseService.list(name, type, status));
    }

    @GetMapping("/{id}")
    public ResponseResult<MonitorDatabase> getById(@PathVariable Long id) {
        return ResponseResult.success(monitorDatabaseService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody MonitorDatabase monitorDatabase) {
        return ResponseResult.success(monitorDatabaseService.save(monitorDatabase));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody MonitorDatabase monitorDatabase) {
        monitorDatabase.setId(id);
        return ResponseResult.success(monitorDatabaseService.updateById(monitorDatabase));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(monitorDatabaseService.removeById(id));
    }

    @DeleteMapping("/batch")
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        return ResponseResult.success(monitorDatabaseService.removeByIds(ids));
    }
}
