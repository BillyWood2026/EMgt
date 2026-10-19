package com.example.flowengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.flowengine.entity.FlowTask;

public interface FlowTaskService extends IService<FlowTask> {
    void complete(Long id, String action, String comment);
}
