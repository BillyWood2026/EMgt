package com.example.sysmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmonitor.entity.MonitorDatabase;
import com.example.sysmonitor.mapper.MonitorDatabaseMapper;
import com.example.sysmonitor.service.MonitorDatabaseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MonitorDatabaseServiceImpl extends ServiceImpl<MonitorDatabaseMapper, MonitorDatabase> implements MonitorDatabaseService {

    @Override
    public Page<MonitorDatabase> page(int current, int size, String name, String type, String status) {
        return page(new Page<>(current, size), buildWrapper(name, type, status));
    }

    @Override
    public List<MonitorDatabase> list(String name, String type, String status) {
        return list(buildWrapper(name, type, status));
    }

    private LambdaQueryWrapper<MonitorDatabase> buildWrapper(String name, String type, String status) {
        LambdaQueryWrapper<MonitorDatabase> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(MonitorDatabase::getName, name);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(MonitorDatabase::getType, type);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorDatabase::getStatus, status);
        }
        wrapper.orderByDesc(MonitorDatabase::getCreateTime);
        return wrapper;
    }
}
