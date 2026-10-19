package com.example.sysmonitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmonitor.entity.MonitorServer;
import com.example.sysmonitor.service.MonitorServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/monitor/server", "/api/sys-mon/server"})
public class MonitorServerController {

    @Autowired
    private MonitorServerService monitorServerService;

    @GetMapping("/page")
    public ResponseResult<Page<MonitorServer>> page(@RequestParam(defaultValue = "1") int current,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(required = false) String name,
                                                    @RequestParam(required = false) String ip,
                                                    @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorServerService.page(current, size, name, ip, status));
    }

    @GetMapping("/list")
    public ResponseResult<List<MonitorServer>> list(@RequestParam(required = false) String name,
                                                    @RequestParam(required = false) String ip,
                                                    @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorServerService.list(name, ip, status));
    }

    @GetMapping("/{id}")
    public ResponseResult<MonitorServer> getById(@PathVariable Long id) {
        return ResponseResult.success(monitorServerService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody MonitorServer monitorServer) {
        return ResponseResult.success(monitorServerService.save(monitorServer));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody MonitorServer monitorServer) {
        monitorServer.setId(id);
        return ResponseResult.success(monitorServerService.updateById(monitorServer));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(monitorServerService.removeById(id));
    }

    @DeleteMapping("/batch")
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        return ResponseResult.success(monitorServerService.removeByIds(ids));
    }
}
