package com.example.sysmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmonitor.entity.MonitorHealthLog;
import com.example.sysmonitor.mapper.MonitorHealthLogMapper;
import com.example.sysmonitor.service.MonitorHealthLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MonitorHealthLogServiceImpl extends ServiceImpl<MonitorHealthLogMapper, MonitorHealthLog> implements MonitorHealthLogService {

    @Override
    public Page<MonitorHealthLog> page(int current, int size, String serviceName, String status) {
        LambdaQueryWrapper<MonitorHealthLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(serviceName)) {
            wrapper.like(MonitorHealthLog::getServiceName, serviceName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorHealthLog::getStatus, status);
        }
        wrapper.orderByDesc(MonitorHealthLog::getCheckTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<MonitorHealthLog> list(String serviceName, String status) {
        LambdaQueryWrapper<MonitorHealthLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(serviceName)) {
            wrapper.like(MonitorHealthLog::getServiceName, serviceName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorHealthLog::getStatus, status);
        }
        wrapper.orderByDesc(MonitorHealthLog::getCheckTime);
        return list(wrapper);
    }
}
