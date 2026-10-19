package com.example.sysmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmonitor.entity.MonitorJob;
import com.example.sysmonitor.mapper.MonitorJobMapper;
import com.example.sysmonitor.service.MonitorJobService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MonitorJobServiceImpl extends ServiceImpl<MonitorJobMapper, MonitorJob> implements MonitorJobService {

    @Override
    public Page<MonitorJob> page(int current, int size, String jobName, String jobGroup, String status) {
        LambdaQueryWrapper<MonitorJob> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(jobName)) {
            wrapper.like(MonitorJob::getJobName, jobName);
        }
        if (StringUtils.hasText(jobGroup)) {
            wrapper.eq(MonitorJob::getJobGroup, jobGroup);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorJob::getStatus, status);
        }
        wrapper.orderByDesc(MonitorJob::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<MonitorJob> list(String jobName, String jobGroup, String status) {
        LambdaQueryWrapper<MonitorJob> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(jobName)) {
            wrapper.like(MonitorJob::getJobName, jobName);
        }
        if (StringUtils.hasText(jobGroup)) {
            wrapper.eq(MonitorJob::getJobGroup, jobGroup);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorJob::getStatus, status);
        }
        wrapper.orderByDesc(MonitorJob::getCreateTime);
        return list(wrapper);
    }
}
