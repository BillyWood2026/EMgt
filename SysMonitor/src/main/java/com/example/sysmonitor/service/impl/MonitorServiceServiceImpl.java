package com.example.sysmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmonitor.entity.MonitorService;
import com.example.sysmonitor.mapper.MonitorServiceMapper;
import com.example.sysmonitor.service.MonitorServiceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MonitorServiceServiceImpl extends ServiceImpl<MonitorServiceMapper, MonitorService> implements MonitorServiceService {

    @Override
    public Page<MonitorService> page(int current, int size, String serviceName, String serviceType, String status) {
        LambdaQueryWrapper<MonitorService> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(serviceName)) {
            wrapper.like(MonitorService::getServiceName, serviceName);
        }
        if (StringUtils.hasText(serviceType)) {
            wrapper.eq(MonitorService::getServiceType, serviceType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorService::getStatus, status);
        }
        wrapper.orderByDesc(MonitorService::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<MonitorService> list(String serviceName, String serviceType, String status) {
        LambdaQueryWrapper<MonitorService> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(serviceName)) {
            wrapper.like(MonitorService::getServiceName, serviceName);
        }
        if (StringUtils.hasText(serviceType)) {
            wrapper.eq(MonitorService::getServiceType, serviceType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorService::getStatus, status);
        }
        wrapper.orderByDesc(MonitorService::getCreateTime);
        return list(wrapper);
    }
}
