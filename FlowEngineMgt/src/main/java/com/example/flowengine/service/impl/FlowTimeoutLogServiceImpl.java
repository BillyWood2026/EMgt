package com.example.flowengine.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.flowengine.entity.FlowTimeoutLog;
import com.example.flowengine.mapper.FlowTimeoutLogMapper;
import com.example.flowengine.service.FlowTimeoutLogService;
import org.springframework.stereotype.Service;

@Service
public class FlowTimeoutLogServiceImpl extends ServiceImpl<FlowTimeoutLogMapper, FlowTimeoutLog> implements FlowTimeoutLogService {
}
