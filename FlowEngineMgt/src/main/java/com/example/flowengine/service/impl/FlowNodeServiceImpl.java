package com.example.flowengine.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.flowengine.entity.FlowNode;
import com.example.flowengine.mapper.FlowNodeMapper;
import com.example.flowengine.service.FlowNodeService;
import org.springframework.stereotype.Service;

@Service
public class FlowNodeServiceImpl extends ServiceImpl<FlowNodeMapper, FlowNode> implements FlowNodeService {
}
