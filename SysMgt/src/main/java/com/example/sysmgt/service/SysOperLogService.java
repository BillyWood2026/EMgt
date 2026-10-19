package com.example.sysmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmgt.entity.SysOperLog;

import java.time.LocalDateTime;

public interface SysOperLogService extends IService<SysOperLog> {

    Page<SysOperLog> page(int current, int size, String title, String businessType, String operName,
                          Integer status, LocalDateTime beginTime, LocalDateTime endTime);

    void clean();
}
