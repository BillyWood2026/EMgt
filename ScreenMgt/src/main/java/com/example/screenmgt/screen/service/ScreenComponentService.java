package com.example.screenmgt.screen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.screenmgt.screen.entity.ScreenComponent;
import com.example.screenmgt.screen.mapper.ScreenComponentMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScreenComponentService extends ServiceImpl<ScreenComponentMapper, ScreenComponent> {

    public List<ScreenComponent> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<ScreenComponent>()
                .orderByDesc(ScreenComponent::getCreateTime));
    }

    public ScreenComponent upsert(ScreenComponent c) {
        c.setUpdateTime(LocalDateTime.now());
        if (c.getId() == null) {
            c.setStatus(c.getStatus() == null ? "启用" : c.getStatus());
            c.setCreateTime(c.getUpdateTime());
            baseMapper.insert(c);
        } else {
            baseMapper.updateById(c);
        }
        return c;
    }
}
