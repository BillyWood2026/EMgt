package com.example.sysmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmgt.entity.SysConfig;
import com.example.sysmgt.mapper.SysConfigMapper;
import com.example.sysmgt.service.SysConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    @Override
    public Page<SysConfig> page(int current, int size, String configKey, String configName, String status) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(configKey)) {
            wrapper.like(SysConfig::getConfigKey, configKey);
        }
        if (StringUtils.hasText(configName)) {
            wrapper.like(SysConfig::getConfigName, configName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysConfig::getStatus, status);
        }
        wrapper.orderByAsc(SysConfig::getSortOrder);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<SysConfig> list(String configKey, String configName, String status) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(configKey)) {
            wrapper.like(SysConfig::getConfigKey, configKey);
        }
        if (StringUtils.hasText(configName)) {
            wrapper.like(SysConfig::getConfigName, configName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysConfig::getStatus, status);
        }
        wrapper.orderByAsc(SysConfig::getSortOrder);
        return list(wrapper);
    }

    @Override
    public SysConfig getByConfigKey(String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        return getOne(wrapper);
    }

    @Override
    public boolean updateByConfigKey(String configKey, String configValue) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        SysConfig config = new SysConfig();
        config.setConfigValue(configValue);
        return update(config, wrapper);
    }
}
