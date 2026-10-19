package com.example.sysmonitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmonitor.entity.MonitorHealthLog;

import java.util.List;

public interface MonitorHealthLogService extends IService<MonitorHealthLog> {

    Page<MonitorHealthLog> page(int current, int size, String serviceName, String status);

    List<MonitorHealthLog> list(String serviceName, String status);
}
