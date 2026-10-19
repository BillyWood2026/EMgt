package com.example.screenmgt.screen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.screenmgt.screen.entity.ScreenTemplate;
import com.example.screenmgt.screen.mapper.ScreenTemplateMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScreenTemplateService extends ServiceImpl<ScreenTemplateMapper, ScreenTemplate> {

    public List<ScreenTemplate> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<ScreenTemplate>()
                .orderByDesc(ScreenTemplate::getCreateTime));
    }

    public ScreenTemplate upsert(ScreenTemplate t) {
        t.setUpdateTime(LocalDateTime.now());
        if (t.getId() == null) {
            t.setStatus(t.getStatus() == null ? "启用" : t.getStatus());
            t.setResolution(t.getResolution() == null ? "1920x1080" : t.getResolution());
            t.setCreateTime(t.getUpdateTime());
            baseMapper.insert(t);
        } else {
            baseMapper.updateById(t);
        }
        return t;
    }
}
