package com.example.sysmonitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmonitor.entity.MonitorMetrics;

import java.util.List;

public interface MonitorMetricsService extends IService<MonitorMetrics> {

    Page<MonitorMetrics> page(int current, int size, String metricsType, String instanceId);

    List<MonitorMetrics> list(String metricsType, String instanceId);
}
