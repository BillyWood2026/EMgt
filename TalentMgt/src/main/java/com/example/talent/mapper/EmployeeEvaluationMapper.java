package com.example.talent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.talent.entity.EmployeeEvaluation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工测评 Mapper
 */
@Mapper
public interface EmployeeEvaluationMapper extends BaseMapper<EmployeeEvaluation> {
}
