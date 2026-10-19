package com.example.sysmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmgt.entity.SysFile;
import com.example.sysmgt.mapper.SysFileMapper;
import com.example.sysmgt.service.SysFileService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService {

    @Override
    public Page<SysFile> page(int current, int size, String fileName, String fileType, String storageType, String bizType) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(fileName)) {
            wrapper.like(SysFile::getFileName, fileName);
        }
        if (StringUtils.hasText(fileType)) {
            wrapper.eq(SysFile::getFileType, fileType);
        }
        if (StringUtils.hasText(storageType)) {
            wrapper.eq(SysFile::getStorageType, storageType);
        }
        if (StringUtils.hasText(bizType)) {
            wrapper.eq(SysFile::getBizType, bizType);
        }
        wrapper.orderByDesc(SysFile::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<SysFile> list(String fileName, String fileType, String storageType, String bizType) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(fileName)) {
            wrapper.like(SysFile::getFileName, fileName);
        }
        if (StringUtils.hasText(fileType)) {
            wrapper.eq(SysFile::getFileType, fileType);
        }
        if (StringUtils.hasText(storageType)) {
            wrapper.eq(SysFile::getStorageType, storageType);
        }
        if (StringUtils.hasText(bizType)) {
            wrapper.eq(SysFile::getBizType, bizType);
        }
        wrapper.orderByDesc(SysFile::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<SysFile> listByBiz(String bizType, String bizId) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFile::getBizType, bizType);
        wrapper.eq(SysFile::getBizId, bizId);
        wrapper.orderByDesc(SysFile::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean deleteByBiz(String bizType, String bizId) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFile::getBizType, bizType);
        wrapper.eq(SysFile::getBizId, bizId);
        return remove(wrapper);
    }
}
