package com.example.environmgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.environmgt.entity.EnvMonitor;
import com.example.environmgt.mapper.EnvMonitorMapper;
import com.example.environmgt.service.EnvMonitorService;
import org.springframework.stereotype.Service;

@Service
public class EnvMonitorServiceImpl extends ServiceImpl<EnvMonitorMapper, EnvMonitor> implements EnvMonitorService {
}
