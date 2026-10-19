package com.example.salary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.salary.entity.EmployeeSalary;
import com.example.salary.mapper.EmployeeSalaryMapper;
import com.example.salary.service.EmployeeSalaryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 员工薪资 Service 实现
 */
@Service
public class EmployeeSalaryServiceImpl
        extends ServiceImpl<EmployeeSalaryMapper, EmployeeSalary>
        implements EmployeeSalaryService {

    @Override
    public IPage<EmployeeSalary> pageList(long page, long size, String employeeName, String jobLevel, String department) {
        LambdaQueryWrapper<EmployeeSalary> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(employeeName)) {
            wrapper.like(EmployeeSalary::getEmployeeName, employeeName);
        }
        if (StringUtils.hasText(jobLevel)) {
            wrapper.like(EmployeeSalary::getJobLevel, jobLevel);
        }
        if (StringUtils.hasText(department)) {
            wrapper.like(EmployeeSalary::getDepartment, department);
        }
        wrapper.orderByAsc(EmployeeSalary::getId);
        return this.page(new Page<>(page, Math.min(size, 500L)), wrapper);
    }
}
