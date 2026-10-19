package com.example.salary.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.salary.entity.JobGrade;
import com.example.salary.mapper.JobGradeMapper;
import com.example.salary.service.JobGradeService;
import org.springframework.stereotype.Service;

/**
 * 岗位等级 Service 实现
 */
@Service
public class JobGradeServiceImpl extends ServiceImpl<JobGradeMapper, JobGrade> implements JobGradeService {
}
