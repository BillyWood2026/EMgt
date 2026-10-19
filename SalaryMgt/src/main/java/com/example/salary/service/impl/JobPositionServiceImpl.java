package com.example.salary.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.salary.entity.JobPosition;
import com.example.salary.mapper.JobPositionMapper;
import com.example.salary.service.JobPositionService;
import org.springframework.stereotype.Service;

/**
 * 岗位评估 Service 实现
 */
@Service
public class JobPositionServiceImpl extends ServiceImpl<JobPositionMapper, JobPosition> implements JobPositionService {
}
