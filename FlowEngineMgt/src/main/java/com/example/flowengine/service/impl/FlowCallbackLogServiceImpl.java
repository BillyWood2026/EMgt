package com.example.flowengine.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.flowengine.entity.FlowCallbackLog;
import com.example.flowengine.mapper.FlowCallbackLogMapper;
import com.example.flowengine.service.FlowCallbackLogService;
import org.springframework.stereotype.Service;

@Service
public class FlowCallbackLogServiceImpl extends ServiceImpl<FlowCallbackLogMapper, FlowCallbackLog> implements FlowCallbackLogService {
}
