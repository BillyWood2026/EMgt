package com.example.iotmgt.iot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.iotmgt.common.BizException;
import com.example.iotmgt.iot.entity.*;
import com.example.iotmgt.iot.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IotService extends ServiceImpl<IotDeviceMapper, IotDevice> {

    private final IotThingModelMapper thingModelMapper;
    private final IotTelemetryMapper telemetryMapper;
    private final IotCommandMapper commandMapper;
    private final IotRuleMapper ruleMapper;
    private final IotAlertMapper alertMapper;
    private final IotGatewayMapper gatewayMapper;

    public IotService(IotThingModelMapper thingModelMapper, IotTelemetryMapper telemetryMapper,
                      IotCommandMapper commandMapper, IotRuleMapper ruleMapper,
                      IotAlertMapper alertMapper, IotGatewayMapper gatewayMapper) {
        this.thingModelMapper = thingModelMapper;
        this.telemetryMapper = telemetryMapper;
        this.commandMapper = commandMapper;
        this.ruleMapper = ruleMapper;
        this.alertMapper = alertMapper;
        this.gatewayMapper = gatewayMapper;
    }

    // ==================== 设备接入 ====================
    public List<IotDevice> listDevices(String deviceType, String onlineStatus) {
        LambdaQueryWrapper<IotDevice> w = new LambdaQueryWrapper<>();
        if (deviceType != null) w.eq(IotDevice::getDeviceType, deviceType);
        if (onlineStatus != null) w.eq(IotDevice::getOnlineStatus, onlineStatus);
        w.orderByDesc(IotDevice::getCreateTime);
        return baseMapper.selectList(w);
    }

    // ==================== 物模型 ====================
    public List<IotThingModel> listThingModels() {
        return thingModelMapper.selectList(null);
    }

    // ==================== 遥测数据 ====================
    public List<IotTelemetry> listTelemetry(String deviceCode, String metricName) {
        LambdaQueryWrapper<IotTelemetry> w = new LambdaQueryWrapper<>();
        if (deviceCode != null) w.eq(IotTelemetry::getDeviceCode, deviceCode);
        if (metricName != null) w.eq(IotTelemetry::getMetricName, metricName);
        w.orderByDesc(IotTelemetry::getReportTime);
        return telemetryMapper.selectList(w);
    }

    // 遥测批量上报：批量写入 + 刷新设备在线状态
    @Transactional
    public int reportTelemetry(List<IotTelemetry> list) {
        if (list == null || list.isEmpty()) throw new BizException("遥测数据不能为空");
        LocalDateTime now = LocalDateTime.now();
        for (IotTelemetry t : list) {
            t.setId(null);
            if (t.getReportTime() == null) t.setReportTime(now);
            t.setCreateTime(now);
            telemetryMapper.insert(t);
            // 上报即视为设备在线，刷新最近上线时间
            IotDevice device = baseMapper.selectOne(
                    new LambdaQueryWrapper<IotDevice>().eq(IotDevice::getDeviceCode, t.getDeviceCode()));
            if (device != null) {
                device.setOnlineStatus("在线");
                device.setLastOnlineTime(now);
                baseMapper.updateById(device);
            }
        }
        return list.size();
    }

    // ==================== 指令下发 ====================
    public List<IotCommand> listCommands(String deviceCode, String execStatus) {
        LambdaQueryWrapper<IotCommand> w = new LambdaQueryWrapper<>();
        if (deviceCode != null) w.eq(IotCommand::getDeviceCode, deviceCode);
        if (execStatus != null) w.eq(IotCommand::getExecStatus, execStatus);
        w.orderByDesc(IotCommand::getIssueTime);
        return commandMapper.selectList(w);
    }

    // 指令执行回执：标记已执行
    @Transactional
    public IotCommand executeCommand(Long id) {
        IotCommand command = commandMapper.selectById(id);
        if (command == null) throw new BizException("指令不存在");
        if ("已执行".equals(command.getExecStatus())) throw new BizException("指令已执行，请勿重复回执");
        command.setExecStatus("已执行");
        command.setUpdateTime(LocalDateTime.now());
        commandMapper.updateById(command);
        return command;
    }

    // ==================== 规则引擎 ====================
    public List<IotRule> listRules(String status) {
        LambdaQueryWrapper<IotRule> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(IotRule::getStatus, status);
        return ruleMapper.selectList(w);
    }

    // ==================== 告警联动 ====================
    public List<IotAlert> listAlerts(String deviceCode, String alertLevel, String status) {
        LambdaQueryWrapper<IotAlert> w = new LambdaQueryWrapper<>();
        if (deviceCode != null) w.eq(IotAlert::getDeviceCode, deviceCode);
        if (alertLevel != null) w.eq(IotAlert::getAlertLevel, alertLevel);
        if (status != null) w.eq(IotAlert::getStatus, status);
        w.orderByDesc(IotAlert::getCreateTime);
        return alertMapper.selectList(w);
    }

    // ==================== 边缘网关 ====================
    public List<IotGateway> listGateways(String onlineStatus) {
        LambdaQueryWrapper<IotGateway> w = new LambdaQueryWrapper<>();
        if (onlineStatus != null) w.eq(IotGateway::getOnlineStatus, onlineStatus);
        return gatewayMapper.selectList(w);
    }
}
