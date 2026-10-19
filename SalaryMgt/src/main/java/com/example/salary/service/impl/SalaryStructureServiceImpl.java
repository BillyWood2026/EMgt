package com.example.salary.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.salary.entity.SalaryStructure;
import com.example.salary.mapper.SalaryStructureMapper;
import com.example.salary.service.SalaryStructureService;
import org.springframework.stereotype.Service;

/**
 * 薪酬体系 Service 实现
 */
@Service
public class SalaryStructureServiceImpl extends ServiceImpl<SalaryStructureMapper, SalaryStructure> implements SalaryStructureService {
}
