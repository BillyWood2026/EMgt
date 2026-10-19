package com.example.salary.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.salary.entity.EmployeeSalary;

/**
 * 员工薪资 Service
 */
public interface EmployeeSalaryService extends IService<EmployeeSalary> {

    /**
     * 分页查询员工薪资，支持员工姓名 / 职级 / 部门模糊搜索
     */
    IPage<EmployeeSalary> pageList(long page, long size, String employeeName, String jobLevel, String department);
}
