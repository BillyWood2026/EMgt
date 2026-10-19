package com.example.salary.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.salary.entity.JobValueAssessment;

/**
 * 岗位价值评估 Service
 */
public interface JobValueAssessmentService extends IService<JobValueAssessment> {

    /**
     * 分页查询岗位价值评估，支持岗位名称 / 价值等级（传 "1" 可匹配 "一级"）搜索
     */
    IPage<JobValueAssessment> pageList(long page, long size, String positionName, String valueLevel);
}
