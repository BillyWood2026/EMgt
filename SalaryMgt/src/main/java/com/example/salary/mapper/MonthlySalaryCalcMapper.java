package com.example.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.salary.entity.MonthlySalaryCalc;
import org.apache.ibatis.annotations.Mapper;

/**
 * 月度薪资核算 Mapper
 */
@Mapper
public interface MonthlySalaryCalcMapper extends BaseMapper<MonthlySalaryCalc> {
}
