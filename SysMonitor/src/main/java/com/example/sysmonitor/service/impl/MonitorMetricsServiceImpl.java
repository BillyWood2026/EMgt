package com.example.sysmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmonitor.entity.MonitorMetrics;
import com.example.sysmonitor.mapper.MonitorMetricsMapper;
import com.example.sysmonitor.service.MonitorMetricsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MonitorMetricsServiceImpl extends ServiceImpl<MonitorMetricsMapper, MonitorMetrics> implements MonitorMetricsService {

    @Override
    public Page<MonitorMetrics> page(int current, int size, String metricsType, String instanceId) {
        LambdaQueryWrapper<MonitorMetrics> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(metricsType)) {
            wrapper.eq(MonitorMetrics::getMetricsType, metricsType);
        }
        if (StringUtils.hasText(instanceId)) {
            wrapper.eq(MonitorMetrics::getInstanceId, instanceId);
        }
        wrapper.orderByDesc(MonitorMetrics::getCollectTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<MonitorMetrics> list(String metricsType, String instanceId) {
        LambdaQueryWrapper<MonitorMetrics> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(metricsType)) {
            wrapper.eq(MonitorMetrics::getMetricsType, metricsType);
        }
        if (StringUtils.hasText(instanceId)) {
            wrapper.eq(MonitorMetrics::getInstanceId, instanceId);
        }
        wrapper.orderByDesc(MonitorMetrics::getCollectTime);
        return list(wrapper);
    }
}
