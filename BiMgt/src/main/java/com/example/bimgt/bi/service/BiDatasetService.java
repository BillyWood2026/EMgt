package com.example.bimgt.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bimgt.bi.entity.BiDataset;
import com.example.bimgt.bi.mapper.BiDatasetMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BiDatasetService extends ServiceImpl<BiDatasetMapper, BiDataset> {

    public List<BiDataset> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<BiDataset>()
                .orderByDesc(BiDataset::getCreateTime));
    }

    public BiDataset upsert(BiDataset ds) {
        ds.setUpdateTime(LocalDateTime.now());
        if (ds.getId() == null) {
            if (ds.getDatasetCode() == null) {
                ds.setDatasetCode("DS-" + System.currentTimeMillis() % 100000);
            }
            ds.setStatus(ds.getStatus() == null ? "草稿" : ds.getStatus());
            ds.setCreateTime(ds.getUpdateTime());
            baseMapper.insert(ds);
        } else {
            baseMapper.updateById(ds);
        }
        return ds;
    }
}
