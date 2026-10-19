package com.example.iotmgt.iot.controller;

import com.example.iotmgt.common.ResponseResult;
import com.example.iotmgt.iot.entity.IotGateway;
import com.example.iotmgt.iot.mapper.IotGatewayMapper;
import com.example.iotmgt.iot.service.IotService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/iot/gateway")
public class IotGatewayController {

    private final IotService iotService;
    private final IotGatewayMapper gatewayMapper;

    public IotGatewayController(IotService iotService, IotGatewayMapper gatewayMapper) {
        this.iotService = iotService;
        this.gatewayMapper = gatewayMapper;
    }

    @GetMapping("/list")
    public ResponseResult<List<IotGateway>> list(@RequestParam(required = false) String onlineStatus) {
        return ResponseResult.ok(iotService.listGateways(onlineStatus));
    }

    @GetMapping("/{id}")
    public ResponseResult<IotGateway> getById(@PathVariable Long id) {
        return ResponseResult.ok(gatewayMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody IotGateway gateway) {
        gateway.setId(null);
        gateway.setCreateTime(LocalDateTime.now());
        if (gateway.getOnlineStatus() == null) gateway.setOnlineStatus("离线");
        if (gateway.getDeviceCount() == null) gateway.setDeviceCount(0);
        return ResponseResult.ok(gatewayMapper.insert(gateway) > 0);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody IotGateway gateway) {
        gateway.setId(id);
        gateway.setUpdateTime(LocalDateTime.now());
        return ResponseResult.ok(gatewayMapper.updateById(gateway) > 0);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(gatewayMapper.deleteById(id) > 0);
    }
}
