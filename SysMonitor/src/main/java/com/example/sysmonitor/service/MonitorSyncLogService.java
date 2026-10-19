package com.example.sysmonitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmonitor.entity.MonitorSyncLog;

import java.util.List;

public interface MonitorSyncLogService extends IService<MonitorSyncLog> {

    Page<MonitorSyncLog> page(int current, int size, String syncType, String status);

    List<MonitorSyncLog> list(String syncType, String status);
}
