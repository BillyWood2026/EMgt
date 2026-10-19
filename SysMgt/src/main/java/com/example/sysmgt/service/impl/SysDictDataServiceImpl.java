package com.example.sysmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmgt.entity.SysDictData;
import com.example.sysmgt.mapper.SysDictDataMapper;
import com.example.sysmgt.service.SysDictDataService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    @Override
    public Page<SysDictData> page(int current, int size, String dictType, String dictLabel, String status) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dictType)) {
            wrapper.eq(SysDictData::getDictType, dictType);
        }
        if (StringUtils.hasText(dictLabel)) {
            wrapper.like(SysDictData::getDictLabel, dictLabel);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysDictData::getStatus, status);
        }
        wrapper.orderByAsc(SysDictData::getDictType).orderByAsc(SysDictData::getSortOrder);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<SysDictData> list(String dictType, String dictLabel, String status) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dictType)) {
            wrapper.eq(SysDictData::getDictType, dictType);
        }
        if (StringUtils.hasText(dictLabel)) {
            wrapper.like(SysDictData::getDictLabel, dictLabel);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysDictData::getStatus, status);
        }
        wrapper.orderByAsc(SysDictData::getDictType).orderByAsc(SysDictData::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<SysDictData> listByDictType(String dictType) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictType);
        wrapper.eq(SysDictData::getStatus, "ACTIVE");
        wrapper.orderByAsc(SysDictData::getSortOrder);
        return list(wrapper);
    }

    @Override
    public boolean deleteByDictType(String dictType) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictType);
        return remove(wrapper);
    }
}
