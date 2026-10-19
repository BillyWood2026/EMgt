package com.example.sysmonitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmonitor.entity.MonitorService;

import java.util.List;

public interface MonitorServiceService extends IService<MonitorService> {

    Page<MonitorService> page(int current, int size, String serviceName, String serviceType, String status);

    List<MonitorService> list(String serviceName, String serviceType, String status);
}
