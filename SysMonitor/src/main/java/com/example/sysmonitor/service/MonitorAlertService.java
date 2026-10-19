package com.example.sysmonitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmonitor.entity.MonitorAlert;

import java.util.List;

public interface MonitorAlertService extends IService<MonitorAlert> {

    Page<MonitorAlert> page(int current, int size, String level, String sourceName, String status);

    List<MonitorAlert> list(String level, String sourceName, String status);
}
