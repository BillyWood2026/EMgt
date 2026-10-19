package com.example.talentreview.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.talentreview.entity.TalentReview;
import com.example.talentreview.mapper.TalentReviewMapper;
import com.example.talentreview.service.TalentReviewService;
import org.springframework.stereotype.Service;

/**
 * Talent Review Service Implementation
 */
@Service
public class TalentReviewServiceImpl extends ServiceImpl<TalentReviewMapper, TalentReview>
        implements TalentReviewService {
}
