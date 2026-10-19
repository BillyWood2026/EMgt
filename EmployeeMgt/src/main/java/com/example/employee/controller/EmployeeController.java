package com.example.employee.controller;

import com.example.common.dto.ResponseResult;
import com.example.employee.entity.Employee;
import com.example.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 员工资源简版入口(/api/employee)：返回与 /api/hr-archive/employee 相同的员工列表，
 * 供健康检查/服务探活使用。前端页面统一走 /api/hr-archive/employee。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseResult<List<Employee>> list() {
        return ResponseResult.success(employeeService.list());
    }
}
