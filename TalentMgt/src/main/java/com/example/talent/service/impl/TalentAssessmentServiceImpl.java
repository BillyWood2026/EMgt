package com.example.talent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.talent.entity.TalentAssessment;
import com.example.talent.mapper.TalentAssessmentMapper;
import com.example.talent.service.TalentAssessmentService;
import org.springframework.stereotype.Service;

/**
 * Talent Assessment Service Implementation
 */
@Service
public class TalentAssessmentServiceImpl extends ServiceImpl<TalentAssessmentMapper, TalentAssessment>
        implements TalentAssessmentService {
}
