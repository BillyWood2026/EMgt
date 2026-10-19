package com.example.sysmonitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmonitor.entity.MonitorJob;

import java.util.List;

public interface MonitorJobService extends IService<MonitorJob> {

    Page<MonitorJob> page(int current, int size, String jobName, String jobGroup, String status);

    List<MonitorJob> list(String jobName, String jobGroup, String status);
}
