package com.example.sysmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmonitor.entity.MonitorJobLog;
import com.example.sysmonitor.mapper.MonitorJobLogMapper;
import com.example.sysmonitor.service.MonitorJobLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MonitorJobLogServiceImpl extends ServiceImpl<MonitorJobLogMapper, MonitorJobLog> implements MonitorJobLogService {

    @Override
    public Page<MonitorJobLog> page(int current, int size, Long jobId, String jobName, String status) {
        LambdaQueryWrapper<MonitorJobLog> wrapper = new LambdaQueryWrapper<>();
        if (jobId != null) {
            wrapper.eq(MonitorJobLog::getJobId, jobId);
        }
        if (StringUtils.hasText(jobName)) {
            wrapper.like(MonitorJobLog::getJobName, jobName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorJobLog::getStatus, status);
        }
        wrapper.orderByDesc(MonitorJobLog::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<MonitorJobLog> list(Long jobId, String jobName, String status) {
        LambdaQueryWrapper<MonitorJobLog> wrapper = new LambdaQueryWrapper<>();
        if (jobId != null) {
            wrapper.eq(MonitorJobLog::getJobId, jobId);
        }
        if (StringUtils.hasText(jobName)) {
            wrapper.like(MonitorJobLog::getJobName, jobName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorJobLog::getStatus, status);
        }
        wrapper.orderByDesc(MonitorJobLog::getCreateTime);
        return list(wrapper);
    }
}
