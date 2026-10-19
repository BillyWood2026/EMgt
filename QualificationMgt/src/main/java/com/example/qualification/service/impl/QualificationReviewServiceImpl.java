package com.example.qualification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.qualification.entity.QualificationReview;
import com.example.qualification.mapper.QualificationReviewMapper;
import com.example.qualification.service.QualificationReviewService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class QualificationReviewServiceImpl
        extends ServiceImpl<QualificationReviewMapper, QualificationReview>
        implements QualificationReviewService {

    @Override
    public boolean approve(Long id) {
        return changeStatus(id, "APPROVED");
    }

    @Override
    public boolean reject(Long id) {
        return changeStatus(id, "REJECTED");
    }

    private boolean changeStatus(Long id, String status) {
        QualificationReview review = getById(id);
        if (review == null) {
            return false;
        }
        review.setReviewStatus(status);
        review.setReviewTime(LocalDateTime.now());
        if (review.getReviewer() == null || review.getReviewer().isBlank()) {
            review.setReviewer("admin");
        }
        review.setCurrentNode("审批完成");
        return updateById(review);
    }
}
