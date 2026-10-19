package com.example.perf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.perf.entity.PerfAssessment;

import java.util.List;

/**
 * 绩效评估 Service
 */
public interface PerfAssessmentService extends IService<PerfAssessment> {

    /**
     * 按前端筛选条件查询（周期、状态、部门均为精确匹配）
     */
    List<PerfAssessment> search(String period, String status, String department);
}
