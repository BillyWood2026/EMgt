package com.example.qualification.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.qualification.entity.QualificationReview;
import com.example.qualification.service.QualificationReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任职资格评审校准 Controller（base: /api/review-calibration）
 * 对进入校准环节（PENDING_CALIBRATION）的评审记录提供查询与校准更新，
 * 复用 qualification_review 表与 QualificationReviewService。
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/review-calibration")
public class ReviewCalibrationController {

    @Autowired
    private QualificationReviewService reviewService;

    /**
     * 校准列表：可按 reviewStatus / keyword（姓名/编号）过滤，默认按 id 倒序。
     */
    @GetMapping("/list")
    public ResponseResult<List<QualificationReview>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<QualificationReview> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            qw.eq(QualificationReview::getReviewStatus, status.trim());
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(QualificationReview::getEmployeeName, kw)
                    .or().like(QualificationReview::getReviewNo, kw));
        }
        qw.orderByDesc(QualificationReview::getId);
        return ResponseResult.success(reviewService.list(qw));
    }

    /**
     * 校准详情
     */
    @GetMapping("/{id}")
    public ResponseResult<QualificationReview> getById(@PathVariable Long id) {
        return ResponseResult.success(reviewService.getById(id));
    }

    /**
     * 校准更新：仅覆盖请求中显式提供的校准字段（各评分/总分/状态/评审人/意见/节点），
     * 基于库中最新版本合并，保证乐观锁与未提交字段不丢失。
     */
    @PutMapping("/{id}")
    public ResponseResult<QualificationReview> calibrate(@PathVariable Long id,
                                                         @RequestBody QualificationReview body) {
        QualificationReview existing = reviewService.getById(id);
        if (existing == null) {
            return ResponseResult.error(404, "评审记录不存在");
        }
        if (body.getTotalScore() != null) {
            existing.setTotalScore(body.getTotalScore());
        }
        if (body.getSelfScore() != null) {
            existing.setSelfScore(body.getSelfScore());
        }
        if (body.getDeptScore() != null) {
            existing.setDeptScore(body.getDeptScore());
        }
        if (body.getHrScore() != null) {
            existing.setHrScore(body.getHrScore());
        }
        if (body.getExpertScore() != null) {
            existing.setExpertScore(body.getExpertScore());
        }
        if (StringUtils.hasText(body.getReviewStatus())) {
            existing.setReviewStatus(body.getReviewStatus());
        }
        if (StringUtils.hasText(body.getReviewer())) {
            existing.setReviewer(body.getReviewer());
        }
        if (body.getRemark() != null) {
            existing.setRemark(body.getRemark());
        }
        if (StringUtils.hasText(body.getCurrentNode())) {
            existing.setCurrentNode(body.getCurrentNode());
        }
        existing.setUpdateTime(LocalDateTime.now());
        reviewService.updateById(existing);
        return ResponseResult.success(reviewService.getById(id));
    }

    /**
     * 校准通过：委托评审审批通过流程。
     */
    @PostMapping("/{id}/approve")
    public ResponseResult<Void> approve(@PathVariable Long id) {
        reviewService.approve(id);
        return ResponseResult.success();
    }
}
