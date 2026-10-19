package com.example.salary.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.salary.entity.SalaryCalculation;
import com.example.salary.mapper.SalaryCalculationMapper;
import com.example.salary.service.SalaryCalculationService;
import org.springframework.stereotype.Service;

/**
 * 薪资核算 Service 实现
 */
@Service
public class SalaryCalculationServiceImpl extends ServiceImpl<SalaryCalculationMapper, SalaryCalculation> implements SalaryCalculationService {
}
