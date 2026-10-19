package com.example.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.employee.entity.Employee;

import java.util.List;

public interface EmployeeService extends IService<Employee> {

    /**
     * 员工列表查询，支持按姓名(模糊)、部门、状态可选过滤。
     * status 同时接受前端小写码值(active/probation/resigned)与枚举名(ACTIVE/...)。
     */
    List<Employee> searchEmployees(String name, String department, String status);

    /** 新增员工：employeeId/employeeStatus 缺省时自动补全。 */
    Employee createEmployee(Employee employee);

    /** 更新员工：按 body 中的 id 更新，null 字段不参与更新(支持离职只改状态)。 */
    Employee updateEmployee(Employee employee);
}
