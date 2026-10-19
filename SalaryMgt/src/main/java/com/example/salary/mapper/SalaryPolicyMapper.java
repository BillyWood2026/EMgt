package com.example.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.salary.entity.SalaryPolicy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 薪酬政策 Mapper
 */
@Mapper
public interface SalaryPolicyMapper extends BaseMapper<SalaryPolicy> {
}
