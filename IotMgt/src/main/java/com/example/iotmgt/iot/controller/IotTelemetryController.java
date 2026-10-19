package com.example.iotmgt.iot.controller;

import com.example.iotmgt.common.ResponseResult;
import com.example.iotmgt.iot.entity.IotTelemetry;
import com.example.iotmgt.iot.mapper.IotTelemetryMapper;
import com.example.iotmgt.iot.service.IotService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/iot/telemetry")
public class IotTelemetryController {

    private final IotService iotService;
    private final IotTelemetryMapper telemetryMapper;

    public IotTelemetryController(IotService iotService, IotTelemetryMapper telemetryMapper) {
        this.iotService = iotService;
        this.telemetryMapper = telemetryMapper;
    }

    @GetMapping("/list")
    public ResponseResult<List<IotTelemetry>> list(@RequestParam(required = false) String deviceCode,
                                                   @RequestParam(required = false) String metricName) {
        return ResponseResult.ok(iotService.listTelemetry(deviceCode, metricName));
    }

    @GetMapping("/{id}")
    public ResponseResult<IotTelemetry> getById(@PathVariable Long id) {
        return ResponseResult.ok(telemetryMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody IotTelemetry telemetry) {
        telemetry.setId(null);
        if (telemetry.getReportTime() == null) telemetry.setReportTime(LocalDateTime.now());
        telemetry.setCreateTime(LocalDateTime.now());
        return ResponseResult.ok(telemetryMapper.insert(telemetry) > 0);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody IotTelemetry telemetry) {
        telemetry.setId(id);
        telemetry.setUpdateTime(LocalDateTime.now());
        return ResponseResult.ok(telemetryMapper.updateById(telemetry) > 0);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(telemetryMapper.deleteById(id) > 0);
    }

    // 设备遥测批量上报
    @PostMapping("/report")
    public ResponseResult<Integer> report(@RequestBody List<IotTelemetry> list) {
        return ResponseResult.ok(iotService.reportTelemetry(list));
    }
}
