package com.example.sysmonitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmonitor.entity.MonitorServer;

import java.util.List;

public interface MonitorServerService extends IService<MonitorServer> {

    Page<MonitorServer> page(int current, int size, String name, String ip, String status);

    List<MonitorServer> list(String name, String ip, String status);
}
