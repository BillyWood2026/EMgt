package com.example.sysmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmonitor.entity.MonitorApiStats;
import com.example.sysmonitor.mapper.MonitorApiStatsMapper;
import com.example.sysmonitor.service.MonitorApiStatsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MonitorApiStatsServiceImpl extends ServiceImpl<MonitorApiStatsMapper, MonitorApiStats> implements MonitorApiStatsService {

    @Override
    public Page<MonitorApiStats> page(int current, int size, String apiPath, String serviceName, String statsDate) {
        LambdaQueryWrapper<MonitorApiStats> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(apiPath)) {
            wrapper.like(MonitorApiStats::getApiPath, apiPath);
        }
        if (StringUtils.hasText(serviceName)) {
            wrapper.like(MonitorApiStats::getServiceName, serviceName);
        }
        if (StringUtils.hasText(statsDate)) {
            wrapper.eq(MonitorApiStats::getStatsDate, java.time.LocalDate.parse(statsDate));
        }
        wrapper.orderByDesc(MonitorApiStats::getStatsDate);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<MonitorApiStats> list(String apiPath, String serviceName, String statsDate) {
        LambdaQueryWrapper<MonitorApiStats> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(apiPath)) {
            wrapper.like(MonitorApiStats::getApiPath, apiPath);
        }
        if (StringUtils.hasText(serviceName)) {
            wrapper.like(MonitorApiStats::getServiceName, serviceName);
        }
        if (StringUtils.hasText(statsDate)) {
            wrapper.eq(MonitorApiStats::getStatsDate, java.time.LocalDate.parse(statsDate));
        }
        wrapper.orderByDesc(MonitorApiStats::getStatsDate);
        return list(wrapper);
    }
}
