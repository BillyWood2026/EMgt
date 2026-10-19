package com.example.sysmonitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmonitor.entity.MonitorOnlineUser;

import java.util.List;

public interface MonitorOnlineUserService extends IService<MonitorOnlineUser> {

    Page<MonitorOnlineUser> page(int current, int size, String username, String status);

    List<MonitorOnlineUser> list(String username, String status);
}
