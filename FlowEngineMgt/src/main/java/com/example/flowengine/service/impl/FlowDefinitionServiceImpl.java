package com.example.flowengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.flowengine.common.BizException;
import com.example.flowengine.entity.FlowDefinition;
import com.example.flowengine.mapper.FlowDefinitionMapper;
import com.example.flowengine.service.FlowDefinitionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlowDefinitionServiceImpl extends ServiceImpl<FlowDefinitionMapper, FlowDefinition> implements FlowDefinitionService {

    @Override
    @Transactional
    public void publish(Long id) {
        FlowDefinition def = getById(id);
        if (def == null) {
            throw new BizException("流程定义不存在");
        }
        List<FlowDefinition> others = list(new LambdaQueryWrapper<FlowDefinition>()
                .eq(FlowDefinition::getDefCode, def.getDefCode())
                .eq(FlowDefinition::getStatus, "published")
                .ne(FlowDefinition::getId, id));
        for (FlowDefinition o : others) {
            o.setStatus("suspended");
            o.setUpdateTime(LocalDateTime.now());
            updateById(o);
        }
        def.setStatus("published");
        def.setUpdateTime(LocalDateTime.now());
        updateById(def);
    }

    @Override
    public void suspend(Long id) {
        FlowDefinition def = getById(id);
        if (def == null) {
            throw new BizException("流程定义不存在");
        }
        if (!"published".equals(def.getStatus())) {
            throw new BizException("仅已发布的流程定义可停用");
        }
        def.setStatus("suspended");
        def.setUpdateTime(LocalDateTime.now());
        updateById(def);
    }
}
