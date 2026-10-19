package com.example.hrplanning.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.hrplanning.entity.KeyMetrics;

public interface KeyMetricsService extends IService<KeyMetrics> {

    IPage<KeyMetrics> page(Integer pageNum, Integer pageSize, String planNo, String metricsType);
}
