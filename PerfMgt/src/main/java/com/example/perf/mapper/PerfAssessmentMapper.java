package com.example.perf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.perf.entity.PerfAssessment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 绩效评估 Mapper
 */
@Mapper
public interface PerfAssessmentMapper extends BaseMapper<PerfAssessment> {
}
