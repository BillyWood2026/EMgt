package com.example.bimgt.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bimgt.bi.entity.BiMetric;
import com.example.bimgt.bi.mapper.BiMetricMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BiMetricService extends ServiceImpl<BiMetricMapper, BiMetric> {

    public List<BiMetric> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<BiMetric>()
                .orderByDesc(BiMetric::getCreateTime));
    }

    public BiMetric upsert(BiMetric m) {
        m.setUpdateTime(LocalDateTime.now());
        if (m.getId() == null) {
            if (m.getMetricCode() == null) {
                m.setMetricCode("M-" + System.currentTimeMillis() % 100000);
            }
            m.setStatus(m.getStatus() == null ? "启用" : m.getStatus());
            m.setCreateTime(m.getUpdateTime());
            baseMapper.insert(m);
        } else {
            baseMapper.updateById(m);
        }
        return m;
    }
}
