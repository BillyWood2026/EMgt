package com.example.recruitment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.recruitment.entity.RecruitmentRequirement;
import com.example.recruitment.mapper.RecruitmentRequirementMapper;
import com.example.recruitment.service.RecruitmentRequirementService;
import org.springframework.stereotype.Service;

@Service
public class RecruitmentRequirementServiceImpl extends ServiceImpl<RecruitmentRequirementMapper, RecruitmentRequirement> implements RecruitmentRequirementService {
}
