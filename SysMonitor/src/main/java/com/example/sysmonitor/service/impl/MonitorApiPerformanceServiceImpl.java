package com.example.sysmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmonitor.entity.MonitorApiPerformance;
import com.example.sysmonitor.mapper.MonitorApiPerformanceMapper;
import com.example.sysmonitor.service.MonitorApiPerformanceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MonitorApiPerformanceServiceImpl extends ServiceImpl<MonitorApiPerformanceMapper, MonitorApiPerformance> implements MonitorApiPerformanceService {

    @Override
    public Page<MonitorApiPerformance> page(int current, int size, String api, String method, String monitorDate) {
        return page(new Page<>(current, size), buildWrapper(api, method, monitorDate));
    }

    @Override
    public List<MonitorApiPerformance> list(String api, String method, String monitorDate) {
        return list(buildWrapper(api, method, monitorDate));
    }

    private LambdaQueryWrapper<MonitorApiPerformance> buildWrapper(String api, String method, String monitorDate) {
        LambdaQueryWrapper<MonitorApiPerformance> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(api)) {
            wrapper.like(MonitorApiPerformance::getApi, api);
        }
        if (StringUtils.hasText(method)) {
            wrapper.eq(MonitorApiPerformance::getMethod, method);
        }
        if (StringUtils.hasText(monitorDate)) {
            wrapper.eq(MonitorApiPerformance::getMonitorDate, monitorDate);
        }
        wrapper.orderByDesc(MonitorApiPerformance::getCreateTime);
        return wrapper;
    }
}
