package com.example.salary.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.salary.entity.SalaryPolicy;
import com.example.salary.mapper.SalaryPolicyMapper;
import com.example.salary.service.SalaryPolicyService;
import org.springframework.stereotype.Service;

/**
 * 薪酬政策 Service 实现
 */
@Service
public class SalaryPolicyServiceImpl extends ServiceImpl<SalaryPolicyMapper, SalaryPolicy> implements SalaryPolicyService {
}
