package com.example.salary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.salary.entity.JobValueAssessment;
import com.example.salary.mapper.JobValueAssessmentMapper;
import com.example.salary.service.JobValueAssessmentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 岗位价值评估 Service 实现
 */
@Service
public class JobValueAssessmentServiceImpl
        extends ServiceImpl<JobValueAssessmentMapper, JobValueAssessment>
        implements JobValueAssessmentService {

    @Override
    public IPage<JobValueAssessment> pageList(long page, long size, String positionName, String valueLevel) {
        LambdaQueryWrapper<JobValueAssessment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(positionName)) {
            wrapper.like(JobValueAssessment::getPositionName, positionName);
        }
        if (StringUtils.hasText(valueLevel)) {
            // 页面下拉传 "1".."5"，库里存 "一级".."五级"，用 LIKE 兼容两种入参
            wrapper.like(JobValueAssessment::getValueLevel, valueLevel);
        }
        wrapper.orderByAsc(JobValueAssessment::getId);
        return this.page(new Page<>(page, Math.min(size, 500L)), wrapper);
    }
}
