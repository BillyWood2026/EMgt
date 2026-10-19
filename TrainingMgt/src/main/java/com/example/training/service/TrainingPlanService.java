package com.example.training.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.training.entity.TrainingPlan;

import java.util.List;

public interface TrainingPlanService extends IService<TrainingPlan> {

    List<TrainingPlan> listPlans(String trainingType, String planName);
}
