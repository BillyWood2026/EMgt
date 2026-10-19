package com.example.flowengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.flowengine.entity.FlowDefinition;

public interface FlowDefinitionService extends IService<FlowDefinition> {
    void publish(Long id);

    void suspend(Long id);
}
