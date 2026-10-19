package com.example.perf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.perf.entity.PerfTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * KPI指标模板 Mapper
 */
@Mapper
public interface PerfTemplateMapper extends BaseMapper<PerfTemplate> {
}
