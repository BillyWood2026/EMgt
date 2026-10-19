package com.example.screenmgt.screen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.screenmgt.screen.entity.ScreenDatasource;
import com.example.screenmgt.screen.mapper.ScreenDatasourceMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScreenDatasourceService extends ServiceImpl<ScreenDatasourceMapper, ScreenDatasource> {

    public List<ScreenDatasource> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<ScreenDatasource>()
                .orderByDesc(ScreenDatasource::getCreateTime));
    }

    public ScreenDatasource upsert(ScreenDatasource d) {
        d.setUpdateTime(LocalDateTime.now());
        if (d.getId() == null) {
            d.setStatus(d.getStatus() == null ? "启用" : d.getStatus());
            d.setRefreshInterval(d.getRefreshInterval() == null ? 60 : d.getRefreshInterval());
            d.setCreateTime(d.getUpdateTime());
            baseMapper.insert(d);
        } else {
            baseMapper.updateById(d);
        }
        return d;
    }
}
