package com.example.culturemgt.behavior.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.behavior.entity.BehaviorReviewConfig;
import com.example.culturemgt.behavior.entity.BehaviorReviewRecord;
import com.example.culturemgt.behavior.mapper.BehaviorReviewConfigMapper;
import com.example.culturemgt.behavior.mapper.BehaviorReviewRecordMapper;
import com.example.culturemgt.common.cul.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReviewService {

    private final BehaviorReviewConfigMapper configMapper;
    private final BehaviorReviewRecordMapper recordMapper;
    private final ScoreService scoreService;
    private final AuditService auditService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ReviewService(BehaviorReviewConfigMapper configMapper,
                         BehaviorReviewRecordMapper recordMapper,
                         ScoreService scoreService,
                         AuditService auditService) {
        this.configMapper = configMapper;
        this.recordMapper = recordMapper;
        this.scoreService = scoreService;
        this.auditService = auditService;
    }

    /* ========== 配置 ========== */

    public List<BehaviorReviewConfig> configList(String status) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        LambdaQueryWrapper<BehaviorReviewConfig> w = new LambdaQueryWrapper<>();
        w.eq(BehaviorReviewConfig::getDeleted, 0);
        if (status != null && !status.isBlank()) w.eq(BehaviorReviewConfig::getStatus, status);
        w.orderByDesc(BehaviorReviewConfig::getCreateTime);
        return configMapper.selectList(w);
    }

    @Transactional
    public BehaviorReviewConfig saveConfig(BehaviorReviewConfig c) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        validateConfig(c);
        if (c.getId() != null) {
            BehaviorReviewConfig old = configMapper.selectById(c.getId());
            BehaviorFlow.require(old != null && old.getDeleted() == 0, "01", "配置不存在");
            BehaviorFlow.require("草稿".equals(old.getStatus()), "11", "已发布/已结束不可修改");
            c.setId(old.getId());
        } else {
            c.setStatus("草稿");
            c.setPublisher(CurrentHeader.operator());
        }
        c.setUpdateTime(LocalDateTime.now());
        if (c.getId() == null) {
            c.setCreateTime(LocalDateTime.now());
            configMapper.insert(c);
        } else {
            configMapper.updateById(c);
        }
        return c;
    }

    @Transactional
    public void publishConfig(Long id) {
        BehaviorReviewConfig c = configMapper.selectById(id);
        BehaviorFlow.require(c != null && c.getDeleted() == 0, "01", "配置不存在");
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        BehaviorFlow.require("草稿".equals(c.getStatus()), "02", "当前状态不可发布");
        BehaviorFlow.require(c.getStartTime() != null && c.getEndTime() != null && c.getEndTime().isAfter(c.getStartTime()), "07", "评议窗口起止非法");
        c.setStatus("已发布");
        c.setUpdateTime(LocalDateTime.now());
        configMapper.updateById(c);
        auditService.log("PUBLISH", "cul_behavior_review_config", id, "发布评议配置 " + c.getPeriodTag());
    }

    @Transactional
    public void finishConfig(Long id) {
        BehaviorReviewConfig c = configMapper.selectById(id);
        BehaviorFlow.require(c != null && c.getDeleted() == 0, "01", "配置不存在");
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        BehaviorFlow.require("已发布".equals(c.getStatus()), "02", "当前状态不可结束");
        c.setStatus("已结束");
        c.setUpdateTime(LocalDateTime.now());
        configMapper.updateById(c);
        auditService.log("FINISH", "cul_behavior_review_config", id, "结束评议配置 " + c.getPeriodTag());
    }

    /* ========== 评议 ========== */

    public List<BehaviorReviewRecord> myTodo() {
        // 简化：返回当前用户作为 evaluator 的评议（已提交）
        return recordMapper.selectList(
            new LambdaQueryWrapper<BehaviorReviewRecord>()
                .eq(BehaviorReviewRecord::getEvaluator, CurrentHeader.operator())
                .eq(BehaviorReviewRecord::getDeleted, 0)
                .orderByDesc(BehaviorReviewRecord::getCreateTime));
    }

    public List<BehaviorReviewRecord> myGiven() {
        return myTodo();
    }

    public List<BehaviorReviewRecord> myReceived() {
        String me = CurrentHeader.operator();
        Set<String> roles = CulRole.currentRoles();
        if (CulRole.hasAny(CulRole.CULTURE_STAFF) || roles.contains("C07")) {
            return recordMapper.selectList(
                new LambdaQueryWrapper<BehaviorReviewRecord>()
                    .eq(BehaviorReviewRecord::getDeleted, 0)
                    .orderByDesc(BehaviorReviewRecord::getCreateTime));
        }
        return recordMapper.selectList(
            new LambdaQueryWrapper<BehaviorReviewRecord>()
                .eq(BehaviorReviewRecord::getEvaluatee, me)
                .eq(BehaviorReviewRecord::getDeleted, 0)
                .orderByDesc(BehaviorReviewRecord::getCreateTime));
    }

    @Transactional
    public BehaviorReviewRecord submitReview(BehaviorReviewRecord r) {
        BehaviorReviewConfig c = configMapper.selectById(r.getConfigId());
        BehaviorFlow.require(c != null && c.getDeleted() == 0, "01", "评议配置不存在");
        BehaviorFlow.require("已发布".equals(c.getStatus()), "02", "评议未发布或已结束");
        BehaviorFlow.require(LocalDateTime.now().isAfter(c.getStartTime()) && LocalDateTime.now().isBefore(c.getEndTime()), "07", "评议窗口已关闭");
        String me = CurrentHeader.operator();
        r.setEvaluator(me);
        // 重复拦截
        long dup = recordMapper.selectCount(
            new LambdaQueryWrapper<BehaviorReviewRecord>()
                .eq(BehaviorReviewRecord::getConfigId, r.getConfigId())
                .eq(BehaviorReviewRecord::getEvaluator, me)
                .eq(BehaviorReviewRecord::getEvaluatee, r.getEvaluatee())
                .eq(BehaviorReviewRecord::getRelation, r.getRelation())
                .eq(BehaviorReviewRecord::getDeleted, 0));
        BehaviorFlow.require(dup == 0, "06", "同一配置下不可重复评议同一对象");
        validateReview(r);
        // 算法71
        double total = ScoreService.calcTotal(r.getScoreCustomer(), r.getScoreStrive(), r.getScoreHardship(), r.getScoreReflect());
        r.setTotalScore(total);
        r.setLevelTag(ScoreService.levelOf(total));
        r.setPushed(1);
        r.setPushTime(LocalDateTime.now());
        r.setSubmitTime(LocalDateTime.now());
        r.setCreateTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        recordMapper.insert(r);
        // 模拟推绩效：同时入账积分
        scoreService.bookFromReview(r);
        auditService.log("SUBMIT", "cul_behavior_review_record", r.getId(),
            me + " 评议 " + r.getEvaluatee() + " 总得分=" + total + " 等级=" + r.getLevelTag());
        return r;
    }

    private void validateConfig(BehaviorReviewConfig c) {
        BehaviorFlow.require(c.getPeriodTag() != null && c.getPeriodTag().trim().length() >= 4, "01", "绩效周期必填");
        BehaviorFlow.require(c.getWeightJson() != null && c.getWeightJson().trim().length() > 10, "10", "权重配置必填");
        try {
            Map<String, Object> w = MAPPER.readValue(c.getWeightJson(), Map.class);
            double sum = w.values().stream().mapToDouble(v -> Double.parseDouble(v.toString())).sum();
            BehaviorFlow.require(Math.abs(sum - 100.0) < 0.01, "10", "权重合计须=100");
        } catch (CulBizException be) {
            throw be;
        } catch (Exception e) {
            BehaviorFlow.require(false, "10", "权重格式错误:" + e.getMessage());
        }
        BehaviorFlow.require(c.getTargetScope() != null && c.getTargetScope().trim().length() >= 2, "01", "参评对象必填");
        BehaviorFlow.require(c.getStartTime() != null && c.getEndTime() != null && c.getEndTime().isAfter(c.getStartTime()), "07", "评议窗口起止非法");
    }

    private void validateReview(BehaviorReviewRecord r) {
        BehaviorFlow.require(r.getEvaluatee() != null && !r.getEvaluatee().isBlank(), "01", "被评人必填");
        BehaviorFlow.require("上级".equals(r.getRelation()) || "互评".equals(r.getRelation()), "01", "评议关系非法");
        BehaviorFlow.require(r.getScoreCustomer() != null && r.getScoreCustomer() >= 0 && r.getScoreCustomer() <= 100, "01", "客户导向得分非法");
        BehaviorFlow.require(r.getScoreStrive() != null && r.getScoreStrive() >= 0 && r.getScoreStrive() <= 100, "01", "奋斗担当得分非法");
        BehaviorFlow.require(r.getScoreHardship() != null && r.getScoreHardship() >= 0 && r.getScoreHardship() <= 100, "01", "艰苦奋斗得分非法");
        BehaviorFlow.require(r.getScoreReflect() != null && r.getScoreReflect() >= 0 && r.getScoreReflect() <= 100, "01", "自我批判得分非法");
        BehaviorFlow.require(r.getComment() != null && r.getComment().trim().length() >= 10, "01", "评语不少于10字");
    }
}
