package com.example.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.salary.entity.EmployeeSalary;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工薪资 Mapper
 */
@Mapper
public interface EmployeeSalaryMapper extends BaseMapper<EmployeeSalary> {
}
