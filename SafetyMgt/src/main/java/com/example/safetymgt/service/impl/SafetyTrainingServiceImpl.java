package com.example.safetymgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.safetymgt.entity.SafetyTraining;
import com.example.safetymgt.mapper.SafetyTrainingMapper;
import com.example.safetymgt.service.SafetyTrainingService;
import org.springframework.stereotype.Service;

@Service
public class SafetyTrainingServiceImpl extends ServiceImpl<SafetyTrainingMapper, SafetyTraining> implements SafetyTrainingService {
}
