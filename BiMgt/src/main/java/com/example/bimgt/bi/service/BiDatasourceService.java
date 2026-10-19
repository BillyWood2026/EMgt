package com.example.bimgt.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bimgt.bi.entity.BiDatasource;
import com.example.bimgt.bi.mapper.BiDatasourceMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BiDatasourceService extends ServiceImpl<BiDatasourceMapper, BiDatasource> {

    public List<BiDatasource> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<BiDatasource>()
                .orderByDesc(BiDatasource::getCreateTime));
    }

    public BiDatasource upsert(BiDatasource ds) {
        ds.setUpdateTime(LocalDateTime.now());
        if (ds.getId() == null) {
            ds.setStatus(ds.getStatus() == null ? "启用" : ds.getStatus());
            ds.setCreateTime(ds.getUpdateTime());
            baseMapper.insert(ds);
        } else {
            baseMapper.updateById(ds);
        }
        return ds;
    }
}
