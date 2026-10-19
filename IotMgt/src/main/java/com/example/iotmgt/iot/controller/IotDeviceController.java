package com.example.iotmgt.iot.controller;

import com.example.iotmgt.common.ResponseResult;
import com.example.iotmgt.iot.entity.IotDevice;
import com.example.iotmgt.iot.service.IotService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/iot/device")
public class IotDeviceController {

    private final IotService iotService;

    public IotDeviceController(IotService iotService) {
        this.iotService = iotService;
    }

    @GetMapping("/list")
    public ResponseResult<List<IotDevice>> list(@RequestParam(required = false) String deviceType,
                                                @RequestParam(required = false) String onlineStatus) {
        return ResponseResult.ok(iotService.listDevices(deviceType, onlineStatus));
    }

    @GetMapping("/{id}")
    public ResponseResult<IotDevice> getById(@PathVariable Long id) {
        return ResponseResult.ok(iotService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody IotDevice device) {
        device.setId(null);
        device.setCreateTime(LocalDateTime.now());
        if (device.getOnlineStatus() == null) device.setOnlineStatus("未激活");
        return ResponseResult.ok(iotService.save(device));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody IotDevice device) {
        device.setId(id);
        device.setUpdateTime(LocalDateTime.now());
        return ResponseResult.ok(iotService.updateById(device));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(iotService.removeById(id));
    }
}
