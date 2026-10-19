package com.example.sysmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmonitor.entity.MonitorServer;
import com.example.sysmonitor.mapper.MonitorServerMapper;
import com.example.sysmonitor.service.MonitorServerService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MonitorServerServiceImpl extends ServiceImpl<MonitorServerMapper, MonitorServer> implements MonitorServerService {

    @Override
    public Page<MonitorServer> page(int current, int size, String name, String ip, String status) {
        return page(new Page<>(current, size), buildWrapper(name, ip, status));
    }

    @Override
    public List<MonitorServer> list(String name, String ip, String status) {
        return list(buildWrapper(name, ip, status));
    }

    private LambdaQueryWrapper<MonitorServer> buildWrapper(String name, String ip, String status) {
        LambdaQueryWrapper<MonitorServer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(MonitorServer::getName, name);
        }
        if (StringUtils.hasText(ip)) {
            wrapper.like(MonitorServer::getIp, ip);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorServer::getStatus, status);
        }
        wrapper.orderByDesc(MonitorServer::getCreateTime);
        return wrapper;
    }
}
