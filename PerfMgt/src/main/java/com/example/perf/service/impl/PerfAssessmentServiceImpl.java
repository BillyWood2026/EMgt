package com.example.perf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.perf.entity.PerfAssessment;
import com.example.perf.mapper.PerfAssessmentMapper;
import com.example.perf.service.PerfAssessmentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 绩效评估 Service 实现
 */
@Service
public class PerfAssessmentServiceImpl extends ServiceImpl<PerfAssessmentMapper, PerfAssessment> implements PerfAssessmentService {

    @Override
    public List<PerfAssessment> search(String period, String status, String department) {
        LambdaQueryWrapper<PerfAssessment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(period)) {
            wrapper.eq(PerfAssessment::getPeriod, period.trim());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PerfAssessment::getStatus, status.trim());
        }
        if (StringUtils.hasText(department)) {
            wrapper.eq(PerfAssessment::getDepartment, department.trim());
        }
        wrapper.orderByDesc(PerfAssessment::getId);
        return list(wrapper);
    }
}
