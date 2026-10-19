package com.example.qualification.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.qualification.entity.QualificationReview;
import com.example.qualification.service.QualificationReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 等级评定/评审 (对应前端 qual-rank/Rank.vue)
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private QualificationReviewService reviewService;

    /**
     * 评审列表(分页 + 员工姓名/工号 + 状态过滤), 直接返回数组。
     * 前端 GET /api/review/list?page=&size=&employeeName=&reviewStatus=
     */
    @GetMapping("/list")
    public ResponseResult<List<QualificationReview>> list(
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String reviewStatus,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "50") long size) {

        QueryWrapper<QualificationReview> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(employeeName)) {
            String kw = employeeName.trim();
            wrapper.and(w -> w.like("employee_name", kw).or().like("employee_no", kw));
        }
        if (StringUtils.hasText(reviewStatus)) {
            wrapper.eq("review_status", reviewStatus.trim());
        }
        wrapper.orderByDesc("create_time");

        Page<QualificationReview> result = reviewService.page(new Page<>(page, size), wrapper);
        return ResponseResult.success(result.getRecords());
    }

    /**
     * 评审详情
     */
    @GetMapping("/{id}")
    public ResponseResult<QualificationReview> getById(@PathVariable Long id) {
        return ResponseResult.success(reviewService.getById(id));
    }

    /**
     * 新建评审
     */
    @PostMapping
    public ResponseResult<QualificationReview> create(@RequestBody QualificationReview review) {
        reviewService.save(review);
        return ResponseResult.success(review);
    }

    /**
     * 删除评审(逻辑删除)
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(reviewService.removeById(id));
    }

    /**
     * 审批通过
     */
    @PostMapping("/{id}/approve")
    public ResponseResult<Void> approve(@PathVariable Long id) {
        reviewService.approve(id);
        return ResponseResult.success();
    }

    /**
     * 审批拒绝
     */
    @PostMapping("/{id}/reject")
    public ResponseResult<Void> reject(@PathVariable Long id) {
        reviewService.reject(id);
        return ResponseResult.success();
    }

    /**
     * 更新评审(body 可只包含 reviewStatus)
     */
    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody QualificationReview review) {
        review.setId(id);
        return ResponseResult.success(reviewService.updateById(review));
    }

    /**
     * 审批流程详情: { processInstanceId, currentNode, status }
     */
    @GetMapping("/{id}/process")
    public ResponseResult<Map<String, Object>> process(@PathVariable Long id) {
        QualificationReview review = reviewService.getById(id);
        Map<String, Object> data = new LinkedHashMap<>();
        if (review != null) {
            data.put("processInstanceId", review.getProcessInstanceId());
            data.put("currentNode", review.getCurrentNode());
            data.put("status", review.getReviewStatus());
        } else {
            data.put("processInstanceId", null);
            data.put("currentNode", null);
            data.put("status", null);
        }
        return ResponseResult.success(data);
    }

    /**
     * 审批历史节点列表
     */
    @GetMapping("/{id}/process/history")
    public ResponseResult<List<Map<String, Object>>> processHistory(@PathVariable Long id) {
        QualificationReview review = reviewService.getById(id);
        List<Map<String, Object>> history = new ArrayList<>();
        if (review == null) {
            return ResponseResult.success(history);
        }

        history.add(node("提交申请", "SUBMIT",
                review.getCreateTime() == null ? null : review.getCreateTime().format(TIME_FMT),
                review.getApplyReason()));

        if ("APPROVED".equals(review.getReviewStatus())) {
            history.add(node((review.getReviewer() == null ? "审批人" : review.getReviewer()) + "审批",
                    "APPROVE",
                    review.getReviewTime() == null ? null : review.getReviewTime().format(TIME_FMT),
                    StringUtils.hasText(review.getRemark()) ? review.getRemark() : "同意通过"));
        } else if ("REJECTED".equals(review.getReviewStatus())) {
            history.add(node((review.getReviewer() == null ? "审批人" : review.getReviewer()) + "审批",
                    "REJECT",
                    review.getReviewTime() == null ? null : review.getReviewTime().format(TIME_FMT),
                    StringUtils.hasText(review.getRemark()) ? review.getRemark() : "审批拒绝"));
        }
        return ResponseResult.success(history);
    }

    private Map<String, Object> node(String nodeName, String action, String operateTime, String opinion) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("nodeName", nodeName);
        node.put("action", action);
        node.put("operateTime", operateTime);
        node.put("opinion", opinion);
        return node;
    }
}
