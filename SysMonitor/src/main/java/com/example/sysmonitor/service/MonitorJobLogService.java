package com.example.sysmonitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmonitor.entity.MonitorJobLog;

import java.util.List;

public interface MonitorJobLogService extends IService<MonitorJobLog> {

    Page<MonitorJobLog> page(int current, int size, Long jobId, String jobName, String status);

    List<MonitorJobLog> list(Long jobId, String jobName, String status);
}
