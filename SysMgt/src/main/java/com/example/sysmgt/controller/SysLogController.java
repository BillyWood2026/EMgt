package com.example.sysmgt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmgt.entity.SysLoginLog;
import com.example.sysmgt.entity.SysOperLog;
import com.example.sysmgt.service.SysLoginLogService;
import com.example.sysmgt.service.SysOperLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sys/log")
public class SysLogController {

    @Autowired
    private SysOperLogService sysOperLogService;

    @Autowired
    private SysLoginLogService sysLoginLogService;

    @GetMapping("/oper/page")
    public ResponseResult<Page<SysOperLog>> operLogPage(@RequestParam(defaultValue = "1") int current,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String title,
                                                        @RequestParam(required = false) String businessType,
                                                        @RequestParam(required = false) String operName,
                                                        @RequestParam(required = false) Integer status,
                                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
                                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return ResponseResult.success(sysOperLogService.page(current, size, title, businessType, operName, status, beginTime, endTime));
    }

    @GetMapping("/oper/{id}")
    public ResponseResult<SysOperLog> getOperLogById(@PathVariable Long id) {
        return ResponseResult.success(sysOperLogService.getById(id));
    }

    @DeleteMapping("/oper/{id}")
    public ResponseResult<Boolean> deleteOperLog(@PathVariable Long id) {
        return ResponseResult.success(sysOperLogService.removeById(id));
    }

    @DeleteMapping("/oper/clean")
    public ResponseResult<Void> cleanOperLog() {
        sysOperLogService.clean();
        return ResponseResult.success();
    }

    @GetMapping("/login/page")
    public ResponseResult<Page<SysLoginLog>> loginLogPage(@RequestParam(defaultValue = "1") int current,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(required = false) String loginName,
                                                          @RequestParam(required = false) String status,
                                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
                                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return ResponseResult.success(sysLoginLogService.page(current, size, loginName, status, beginTime, endTime));
    }

    @GetMapping("/login/{id}")
    public ResponseResult<SysLoginLog> getLoginLogById(@PathVariable Long id) {
        return ResponseResult.success(sysLoginLogService.getById(id));
    }

    @DeleteMapping("/login/{id}")
    public ResponseResult<Boolean> deleteLoginLog(@PathVariable Long id) {
        return ResponseResult.success(sysLoginLogService.removeById(id));
    }

    @DeleteMapping("/login/clean")
    public ResponseResult<Void> cleanLoginLog() {
        sysLoginLogService.clean();
        return ResponseResult.success();
    }
}
