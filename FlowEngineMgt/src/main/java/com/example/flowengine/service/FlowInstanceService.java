package com.example.flowengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.flowengine.entity.FlowInstance;

public interface FlowInstanceService extends IService<FlowInstance> {
    FlowInstance start(FlowInstance req);

    void withdraw(Long id, String operator);
}
