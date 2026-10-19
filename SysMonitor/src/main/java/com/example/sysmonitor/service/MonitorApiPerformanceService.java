package com.example.sysmonitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmonitor.entity.MonitorApiPerformance;

import java.util.List;

public interface MonitorApiPerformanceService extends IService<MonitorApiPerformance> {

    Page<MonitorApiPerformance> page(int current, int size, String api, String method, String monitorDate);

    List<MonitorApiPerformance> list(String api, String method, String monitorDate);
}
