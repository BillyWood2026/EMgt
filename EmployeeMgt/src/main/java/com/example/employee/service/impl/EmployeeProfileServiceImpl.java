package com.example.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.employee.entity.EmployeeProfile;
import com.example.employee.mapper.EmployeeProfileMapper;
import com.example.employee.service.EmployeeProfileService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeProfileServiceImpl extends ServiceImpl<EmployeeProfileMapper, EmployeeProfile> implements EmployeeProfileService {
}
