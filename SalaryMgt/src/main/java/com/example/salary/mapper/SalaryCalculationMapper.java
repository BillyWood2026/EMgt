package com.example.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.salary.entity.SalaryCalculation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 薪资核算 Mapper
 */
@Mapper
public interface SalaryCalculationMapper extends BaseMapper<SalaryCalculation> {
}
