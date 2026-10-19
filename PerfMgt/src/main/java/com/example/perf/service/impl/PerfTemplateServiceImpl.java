package com.example.perf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.perf.entity.PerfTemplate;
import com.example.perf.mapper.PerfTemplateMapper;
import com.example.perf.service.PerfTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * KPI指标模板 Service 实现
 */
@Service
public class PerfTemplateServiceImpl extends ServiceImpl<PerfTemplateMapper, PerfTemplate> implements PerfTemplateService {

    @Override
    public List<PerfTemplate> search(String name, String dimension) {
        LambdaQueryWrapper<PerfTemplate> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(PerfTemplate::getName, name.trim());
        }
        if (StringUtils.hasText(dimension)) {
            wrapper.eq(PerfTemplate::getDimension, dimension.trim());
        }
        wrapper.orderByDesc(PerfTemplate::getId);
        return list(wrapper);
    }
}
