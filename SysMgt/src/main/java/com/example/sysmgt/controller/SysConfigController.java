package com.example.sysmgt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmgt.entity.SysConfig;
import com.example.sysmgt.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/config")
public class SysConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    @GetMapping("/page")
    public ResponseResult<Page<SysConfig>> page(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String configKey,
                                                @RequestParam(required = false) String configName,
                                                @RequestParam(required = false) String status) {
        return ResponseResult.success(sysConfigService.page(current, size, configKey, configName, status));
    }

    @GetMapping("/list")
    public ResponseResult<List<SysConfig>> list(@RequestParam(required = false) String configKey,
                                                @RequestParam(required = false) String configName,
                                                @RequestParam(required = false) String status) {
        return ResponseResult.success(sysConfigService.list(configKey, configName, status));
    }

    @GetMapping("/{id}")
    public ResponseResult<SysConfig> getById(@PathVariable Long id) {
        return ResponseResult.success(sysConfigService.getById(id));
    }

    @GetMapping("/key/{configKey}")
    public ResponseResult<SysConfig> getByConfigKey(@PathVariable String configKey) {
        return ResponseResult.success(sysConfigService.getByConfigKey(configKey));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody SysConfig sysConfig) {
        return ResponseResult.success(sysConfigService.save(sysConfig));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody SysConfig sysConfig) {
        sysConfig.setId(id);
        return ResponseResult.success(sysConfigService.updateById(sysConfig));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(sysConfigService.removeById(id));
    }

    @DeleteMapping("/batch")
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        return ResponseResult.success(sysConfigService.removeByIds(ids));
    }
}
