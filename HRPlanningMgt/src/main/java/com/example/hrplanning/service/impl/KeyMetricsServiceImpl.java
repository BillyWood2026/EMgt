package com.example.hrplanning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.hrplanning.entity.KeyMetrics;
import com.example.hrplanning.mapper.KeyMetricsMapper;
import com.example.hrplanning.service.KeyMetricsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KeyMetricsServiceImpl
        extends ServiceImpl<KeyMetricsMapper, KeyMetrics>
        implements KeyMetricsService {

    @Override
    public IPage<KeyMetrics> page(Integer pageNum, Integer pageSize, String planNo, String metricsType) {
        LambdaQueryWrapper<KeyMetrics> wrapper = new LambdaQueryWrapper<KeyMetrics>()
                .eq(StringUtils.hasText(planNo), KeyMetrics::getPlanNo, planNo)
                .eq(StringUtils.hasText(metricsType), KeyMetrics::getMetricsType, metricsType)
                .orderByDesc(KeyMetrics::getId);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
