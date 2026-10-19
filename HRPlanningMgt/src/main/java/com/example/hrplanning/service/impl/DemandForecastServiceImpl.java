package com.example.hrplanning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.hrplanning.entity.DemandForecast;
import com.example.hrplanning.mapper.DemandForecastMapper;
import com.example.hrplanning.service.DemandForecastService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DemandForecastServiceImpl
        extends ServiceImpl<DemandForecastMapper, DemandForecast>
        implements DemandForecastService {

    @Override
    public IPage<DemandForecast> page(Integer pageNum, Integer pageSize, String orgCode, String planNo) {
        LambdaQueryWrapper<DemandForecast> wrapper = new LambdaQueryWrapper<DemandForecast>()
                .eq(StringUtils.hasText(orgCode), DemandForecast::getOrgCode, orgCode)
                .eq(StringUtils.hasText(planNo), DemandForecast::getPlanNo, planNo)
                .orderByDesc(DemandForecast::getId);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
