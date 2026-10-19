package com.example.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.training.entity.TrainingPlan;
import com.example.training.mapper.TrainingPlanMapper;
import com.example.training.service.TrainingPlanService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TrainingPlanServiceImpl extends ServiceImpl<TrainingPlanMapper, TrainingPlan>
        implements TrainingPlanService {

    @Override
    public List<TrainingPlan> listPlans(String trainingType, String planName) {
        LambdaQueryWrapper<TrainingPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(trainingType), TrainingPlan::getTrainingType, trainingType)
                .like(StringUtils.hasText(planName), TrainingPlan::getPlanName, planName)
                .orderByDesc(TrainingPlan::getId);
        return this.list(wrapper);
    }
}
