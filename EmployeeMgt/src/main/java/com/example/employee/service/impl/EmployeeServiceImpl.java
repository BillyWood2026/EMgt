package com.example.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.employee.entity.Employee;
import com.example.employee.mapper.EmployeeMapper;
import com.example.employee.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public List<Employee> searchEmployees(String name, String department, String status) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Employee::getEmployeeName, name.trim());
        }
        if (StringUtils.hasText(department)) {
            wrapper.eq(Employee::getDeptName, department.trim());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Employee::getEmployeeStatus, normalizeStatus(status.trim()));
        }
        wrapper.orderByAsc(Employee::getId);
        return list(wrapper);
    }

    @Override
    public Employee createEmployee(Employee employee) {
        if (!StringUtils.hasText(employee.getEmployeeId())) {
            employee.setEmployeeId(generateEmployeeId());
        }
        if (!StringUtils.hasText(employee.getEmployeeStatus())) {
            employee.setEmployeeStatus("ACTIVE");
        }
        if (employee.getDeleted() == null) {
            employee.setDeleted(0);
        }
        if (employee.getVersion() == null) {
            employee.setVersion(1);
        }
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        save(employee);
        return getById(employee.getId());
    }

    @Override
    public Employee updateEmployee(Employee employee) {
        if (employee.getId() == null) {
            throw new IllegalArgumentException("员工ID(id)不能为空");
        }
        // updateById 默认 NOT_NULL 策略：离职 PUT 仅携带 id + employeeStatus 等非空字段,
        // 其余字段保持原值, 不会被置空。
        employee.setUpdateTime(LocalDateTime.now());
        updateById(employee);
        return getById(employee.getId());
    }

    /** 前端列表状态码(active/probation/resigned) -> 后端枚举名。 */
    private String normalizeStatus(String status) {
        switch (status.toLowerCase()) {
            case "active":
                return "ACTIVE";
            case "probation":
                return "PROBATION";
            case "resigned":
                return "RESIGNED";
            default:
                return status.toUpperCase();
        }
    }

    private String generateEmployeeId() {
        return "EMP" + LocalDateTime.now().format(NO_FMT)
                + String.format("%02d", ThreadLocalRandom.current().nextInt(100));
    }
}
