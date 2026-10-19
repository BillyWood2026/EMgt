package com.example.perf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.perf.entity.PerfTemplate;

import java.util.List;

/**
 * KPI指标模板 Service
 */
public interface PerfTemplateService extends IService<PerfTemplate> {

    /**
     * 按前端搜索条件查询（指标名称模糊、所属维度精确）
     */
    List<PerfTemplate> search(String name, String dimension);
}
