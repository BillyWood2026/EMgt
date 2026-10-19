package com.example.iotmgt.iot.controller;

import com.example.iotmgt.common.ResponseResult;
import com.example.iotmgt.iot.entity.IotAlert;
import com.example.iotmgt.iot.mapper.IotAlertMapper;
import com.example.iotmgt.iot.service.IotService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/iot/alert")
public class IotAlertController {

    private final IotService iotService;
    private final IotAlertMapper alertMapper;

    public IotAlertController(IotService iotService, IotAlertMapper alertMapper) {
        this.iotService = iotService;
        this.alertMapper = alertMapper;
    }

    @GetMapping("/list")
    public ResponseResult<List<IotAlert>> list(@RequestParam(required = false) String deviceCode,
                                               @RequestParam(required = false) String alertLevel,
                                               @RequestParam(required = false) String status) {
        return ResponseResult.ok(iotService.listAlerts(deviceCode, alertLevel, status));
    }

    @GetMapping("/{id}")
    public ResponseResult<IotAlert> getById(@PathVariable Long id) {
        return ResponseResult.ok(alertMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody IotAlert alert) {
        alert.setId(null);
        alert.setCreateTime(LocalDateTime.now());
        if (alert.getStatus() == null) alert.setStatus("未处理");
        return ResponseResult.ok(alertMapper.insert(alert) > 0);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody IotAlert alert) {
        alert.setId(id);
        alert.setUpdateTime(LocalDateTime.now());
        return ResponseResult.ok(alertMapper.updateById(alert) > 0);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(alertMapper.deleteById(id) > 0);
    }
}
