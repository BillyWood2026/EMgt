package com.example.succession.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.succession.entity.SuccessionPlan;
import com.example.succession.mapper.SuccessionPlanMapper;
import com.example.succession.service.SuccessionPlanService;
import org.springframework.stereotype.Service;

/**
 * Succession Plan Service Implementation
 */
@Service
public class SuccessionPlanServiceImpl extends ServiceImpl<SuccessionPlanMapper, SuccessionPlan>
        implements SuccessionPlanService {
}
