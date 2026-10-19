package com.example.sysmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmgt.entity.SysLoginLog;

import java.time.LocalDateTime;

public interface SysLoginLogService extends IService<SysLoginLog> {

    Page<SysLoginLog> page(int current, int size, String loginName, String status,
                           LocalDateTime beginTime, LocalDateTime endTime);

    void clean();
}
