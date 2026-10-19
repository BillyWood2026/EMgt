package com.example.sysmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmonitor.entity.MonitorSyncLog;
import com.example.sysmonitor.mapper.MonitorSyncLogMapper;
import com.example.sysmonitor.service.MonitorSyncLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MonitorSyncLogServiceImpl extends ServiceImpl<MonitorSyncLogMapper, MonitorSyncLog> implements MonitorSyncLogService {

    @Override
    public Page<MonitorSyncLog> page(int current, int size, String syncType, String status) {
        LambdaQueryWrapper<MonitorSyncLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(syncType)) {
            wrapper.eq(MonitorSyncLog::getSyncType, syncType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorSyncLog::getStatus, status);
        }
        wrapper.orderByDesc(MonitorSyncLog::getSyncTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<MonitorSyncLog> list(String syncType, String status) {
        LambdaQueryWrapper<MonitorSyncLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(syncType)) {
            wrapper.eq(MonitorSyncLog::getSyncType, syncType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorSyncLog::getStatus, status);
        }
        wrapper.orderByDesc(MonitorSyncLog::getSyncTime);
        return list(wrapper);
    }
}
