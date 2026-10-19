package com.example.hrplanning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.hrplanning.entity.HRPlan;
import com.example.hrplanning.mapper.HRPlanMapper;
import com.example.hrplanning.service.HRPlanService;
import org.springframework.stereotype.Service;

/**
 * HR Plan Service Implementation
 */
@Service
public class HRPlanServiceImpl extends ServiceImpl<HRPlanMapper, HRPlan>
        implements HRPlanService {
}
