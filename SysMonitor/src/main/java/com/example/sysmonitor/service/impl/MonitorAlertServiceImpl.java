package com.example.sysmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmonitor.entity.MonitorAlert;
import com.example.sysmonitor.mapper.MonitorAlertMapper;
import com.example.sysmonitor.service.MonitorAlertService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MonitorAlertServiceImpl extends ServiceImpl<MonitorAlertMapper, MonitorAlert> implements MonitorAlertService {

    @Override
    public Page<MonitorAlert> page(int current, int size, String level, String sourceName, String status) {
        return page(new Page<>(current, size), buildWrapper(level, sourceName, status));
    }

    @Override
    public List<MonitorAlert> list(String level, String sourceName, String status) {
        return list(buildWrapper(level, sourceName, status));
    }

    private LambdaQueryWrapper<MonitorAlert> buildWrapper(String level, String sourceName, String status) {
        LambdaQueryWrapper<MonitorAlert> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(level)) {
            wrapper.eq(MonitorAlert::getLevel, level);
        }
        if (StringUtils.hasText(sourceName)) {
            wrapper.like(MonitorAlert::getSourceName, sourceName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorAlert::getStatus, status);
        }
        wrapper.orderByDesc(MonitorAlert::getCreateTime);
        return wrapper;
    }
}
