package com.example.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.employee.entity.EmployeeProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeProfileMapper extends BaseMapper<EmployeeProfile> {
}
