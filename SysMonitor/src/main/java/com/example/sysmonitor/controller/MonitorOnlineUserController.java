package com.example.sysmonitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmonitor.entity.MonitorOnlineUser;
import com.example.sysmonitor.service.MonitorOnlineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/monitor/online", "/api/sys-mon/online"})
public class MonitorOnlineUserController {

    @Autowired
    private MonitorOnlineUserService monitorOnlineUserService;

    @GetMapping("/page")
    public ResponseResult<Page<MonitorOnlineUser>> page(@RequestParam(defaultValue = "1") int current,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String username,
                                                        @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorOnlineUserService.page(current, size, username, status));
    }

    @GetMapping("/list")
    public ResponseResult<List<MonitorOnlineUser>> list(@RequestParam(required = false) String username,
                                                        @RequestParam(required = false) String status) {
        return ResponseResult.success(monitorOnlineUserService.list(username, status));
    }

    @GetMapping("/{id}")
    public ResponseResult<MonitorOnlineUser> getById(@PathVariable Long id) {
        return ResponseResult.success(monitorOnlineUserService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody MonitorOnlineUser monitorOnlineUser) {
        return ResponseResult.success(monitorOnlineUserService.save(monitorOnlineUser));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody MonitorOnlineUser monitorOnlineUser) {
        monitorOnlineUser.setId(id);
        return ResponseResult.success(monitorOnlineUserService.updateById(monitorOnlineUser));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(monitorOnlineUserService.removeById(id));
    }

    @DeleteMapping("/batch")
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        return ResponseResult.success(monitorOnlineUserService.removeByIds(ids));
    }
}
