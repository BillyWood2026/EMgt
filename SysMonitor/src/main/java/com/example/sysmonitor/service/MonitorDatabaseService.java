package com.example.sysmonitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmonitor.entity.MonitorDatabase;

import java.util.List;

public interface MonitorDatabaseService extends IService<MonitorDatabase> {

    Page<MonitorDatabase> page(int current, int size, String name, String type, String status);

    List<MonitorDatabase> list(String name, String type, String status);
}
