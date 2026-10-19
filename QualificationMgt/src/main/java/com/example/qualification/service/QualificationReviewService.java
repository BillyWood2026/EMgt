package com.example.qualification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.qualification.entity.QualificationReview;

public interface QualificationReviewService extends IService<QualificationReview> {

    /**
     * 审批通过: reviewStatus -> APPROVED
     */
    boolean approve(Long id);

    /**
     * 审批拒绝: reviewStatus -> REJECTED
     */
    boolean reject(Long id);
}
