package com.example.bimgt.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bimgt.bi.entity.BiChart;
import com.example.bimgt.bi.mapper.BiChartMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BiChartService extends ServiceImpl<BiChartMapper, BiChart> {

    public List<BiChart> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<BiChart>()
                .orderByDesc(BiChart::getCreateTime));
    }

    public BiChart upsert(BiChart c) {
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
