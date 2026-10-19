package com.example.sysmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmgt.entity.SysLoginLog;
import com.example.sysmgt.mapper.SysLoginLogMapper;
import com.example.sysmgt.service.SysLoginLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Override
    public Page<SysLoginLog> page(int current, int size, String loginName, String status,
                                  LocalDateTime beginTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(loginName)) {
            wrapper.like(SysLoginLog::getLoginName, loginName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysLoginLog::getStatus, status);
        }
        if (beginTime != null) {
            wrapper.ge(SysLoginLog::getLoginTime, beginTime);
        }
        if (endTime != null) {
            wrapper.le(SysLoginLog::getLoginTime, endTime);
        }
        wrapper.orderByDesc(SysLoginLog::getLoginTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public void clean() {
        remove(null);
    }
}
