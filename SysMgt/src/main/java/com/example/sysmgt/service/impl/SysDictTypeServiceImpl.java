package com.example.sysmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmgt.entity.SysDictType;
import com.example.sysmgt.mapper.SysDictTypeMapper;
import com.example.sysmgt.service.SysDictTypeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    @Override
    public Page<SysDictType> page(int current, int size, String dictName, String dictType, String status) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dictName)) {
            wrapper.like(SysDictType::getDictName, dictName);
        }
        if (StringUtils.hasText(dictType)) {
            wrapper.like(SysDictType::getDictType, dictType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysDictType::getStatus, status);
        }
        wrapper.orderByAsc(SysDictType::getId);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<SysDictType> list(String dictName, String dictType, String status) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dictName)) {
            wrapper.like(SysDictType::getDictName, dictName);
        }
        if (StringUtils.hasText(dictType)) {
            wrapper.like(SysDictType::getDictType, dictType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysDictType::getStatus, status);
        }
        wrapper.orderByAsc(SysDictType::getId);
        return list(wrapper);
    }

    @Override
    public SysDictType getByDictType(String dictType) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictType::getDictType, dictType);
        return getOne(wrapper);
    }

    @Override
    public boolean checkDictTypeUnique(Long id, String dictType) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictType::getDictType, dictType);
        if (id != null) {
            wrapper.ne(SysDictType::getId, id);
        }
        return count(wrapper) == 0;
    }
}
