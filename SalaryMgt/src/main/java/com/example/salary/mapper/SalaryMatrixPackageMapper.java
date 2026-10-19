package com.example.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.salary.entity.SalaryMatrixPackage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 矩阵包 / 岗位薪资等级 Mapper
 */
@Mapper
public interface SalaryMatrixPackageMapper extends BaseMapper<SalaryMatrixPackage> {
}
