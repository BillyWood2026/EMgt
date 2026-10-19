package com.example.sysmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmonitor.entity.MonitorOnlineUser;
import com.example.sysmonitor.mapper.MonitorOnlineUserMapper;
import com.example.sysmonitor.service.MonitorOnlineUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MonitorOnlineUserServiceImpl extends ServiceImpl<MonitorOnlineUserMapper, MonitorOnlineUser> implements MonitorOnlineUserService {

    @Override
    public Page<MonitorOnlineUser> page(int current, int size, String username, String status) {
        LambdaQueryWrapper<MonitorOnlineUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(MonitorOnlineUser::getUsername, username);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorOnlineUser::getStatus, status);
        }
        wrapper.orderByDesc(MonitorOnlineUser::getLoginTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<MonitorOnlineUser> list(String username, String status) {
        LambdaQueryWrapper<MonitorOnlineUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(MonitorOnlineUser::getUsername, username);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorOnlineUser::getStatus, status);
        }
        wrapper.orderByDesc(MonitorOnlineUser::getLoginTime);
        return list(wrapper);
    }
}
