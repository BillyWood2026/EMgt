package com.example.sysmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmgt.entity.SysConfig;

import java.util.List;

public interface SysConfigService extends IService<SysConfig> {

    Page<SysConfig> page(int current, int size, String configKey, String configName, String status);

    List<SysConfig> list(String configKey, String configName, String status);

    SysConfig getByConfigKey(String configKey);

    boolean updateByConfigKey(String configKey, String configValue);
}
