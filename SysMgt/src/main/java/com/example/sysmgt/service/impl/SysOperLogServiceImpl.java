package com.example.sysmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmgt.entity.SysOperLog;
import com.example.sysmgt.mapper.SysOperLogMapper;
import com.example.sysmgt.service.SysOperLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    @Override
    public Page<SysOperLog> page(int current, int size, String title, String businessType, String operName,
                                 Integer status, LocalDateTime beginTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(SysOperLog::getTitle, title);
        }
        if (StringUtils.hasText(businessType)) {
            wrapper.eq(SysOperLog::getBusinessType, businessType);
        }
        if (StringUtils.hasText(operName)) {
            wrapper.like(SysOperLog::getOperName, operName);
        }
        if (status != null) {
            wrapper.eq(SysOperLog::getStatus, status);
        }
        if (beginTime != null) {
            wrapper.ge(SysOperLog::getOperTime, beginTime);
        }
        if (endTime != null) {
            wrapper.le(SysOperLog::getOperTime, endTime);
        }
        wrapper.orderByDesc(SysOperLog::getOperTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public void clean() {
        remove(null);
    }
}
