package com.example.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.salary.entity.SalaryItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 薪资项目 / 薪资政策 Mapper
 */
@Mapper
public interface SalaryItemMapper extends BaseMapper<SalaryItem> {
}
