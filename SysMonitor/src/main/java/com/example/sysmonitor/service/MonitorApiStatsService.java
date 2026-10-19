package com.example.sysmonitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmonitor.entity.MonitorApiStats;

import java.util.List;

public interface MonitorApiStatsService extends IService<MonitorApiStats> {

    Page<MonitorApiStats> page(int current, int size, String apiPath, String serviceName, String statsDate);

    List<MonitorApiStats> list(String apiPath, String serviceName, String statsDate);
}
