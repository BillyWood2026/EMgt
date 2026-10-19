package com.example.partyaffairsmgt.org.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.member.entity.PartyMember;
import com.example.partyaffairsmgt.member.mapper.PartyMemberMapper;
import com.example.partyaffairsmgt.org.entity.AppraisalItem;
import com.example.partyaffairsmgt.org.entity.AppraisalPlan;
import com.example.partyaffairsmgt.org.entity.DemocraticAppraisal;
import com.example.partyaffairsmgt.org.mapper.AppraisalItemMapper;
import com.example.partyaffairsmgt.org.mapper.AppraisalPlanMapper;
import com.example.partyaffairsmgt.org.mapper.DemocraticAppraisalMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 4.4 民主评议党员：计划/自评/互评/支评/审定/公示，加权得分、优秀比例、预备党员与调整审批。 */
@Service
public class DemocraticAppraisalService {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired private AppraisalPlanMapper planMapper;
    @Autowired private AppraisalItemMapper itemMapper;
    @Autowired private PartyMemberMapper memberMapper;
    @Autowired private DemocraticAppraisalMapper legacyMapper;
    @Autowired private AuditService auditService;

    // ---------- 计划 ----------

    public List<AppraisalPlan> plans(Long orgId, Integer year) {
        QueryWrapper<AppraisalPlan> qw = new QueryWrapper<>();
        if (orgId != null) {
            qw.eq("org_id", orgId);
        }
        if (year != null) {
            qw.eq("assess_year", year);
        }
        qw.orderByDesc("assess_year", "id");
        return planMapper.selectList(qw);
    }

    @Transactional
    public AppraisalPlan savePlan(AppraisalPlan plan) {
        if (plan.getOrgId() == null || plan.getAssessYear() == null) {
            throw PartyBizException.block("E-APP-01", "评议组织与年度必填");
        }
        if (plan.getEvalMode() == null) {
            plan.setEvalMode("实名");
        }
        if (plan.getExcellentRatio() == null) {
            plan.setExcellentRatio(new BigDecimal("0.3333"));
        }
        if (plan.getWeightsJson() == null) {
            plan.setWeightsJson("{\"politics\":0.3,\"discipline\":0.2,\"role\":0.3,\"morality\":0.2}");
        }
        LocalDateTime now = LocalDateTime.now();
        if (plan.getId() == null) {
            if (plan.getStatus() == null) {
                plan.setStatus("草稿");
            }
            plan.setCreateTime(now);
            plan.setUpdateTime(now);
            planMapper.insert(plan);
        } else {
            plan.setUpdateTime(now);
            planMapper.updateById(plan);
        }
        return plan;
    }

    /** 启动计划：按在册党员（含预备党员）生成评议明细。 */
    @Transactional
    public AppraisalPlan start(Long planId) {
        AppraisalPlan plan = mustPlan(planId);
        List<PartyMember> members = memberMapper.selectList(new QueryWrapper<PartyMember>()
                .eq("org_id", plan.getOrgId()).orderByAsc("id"));
        if (members.isEmpty()) {
            throw PartyBizException.block("E-APP-02", "该组织无在册党员，不能发起评议");
        }
        itemMapper.delete(new QueryWrapper<AppraisalItem>().eq("plan_id", planId));
        for (PartyMember m : members) {
            AppraisalItem item = new AppraisalItem();
            item.setPlanId(planId);
            item.setOrgId(plan.getOrgId());
            item.setMemberId(m.getId());
            item.setMemberName(m.getName());
            boolean probationary = "预备党员".equals(m.getMemberStatus());
            item.setIsProbationary(probationary ? 1 : 0);
            item.setParticipated(1);
            item.setResult(probationary ? "不定等次" : null);
            item.setStage("待自评");
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            itemMapper.insert(item);
        }
        plan.setStatus("进行中");
        plan.setPublishTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.updateById(plan);
        auditService.log("APPRAISAL_START", "appraisal_plan", planId,
                plan.getAssessYear() + "年度评议启动，对象" + members.size() + "人");
        return plan;
    }

    public List<AppraisalItem> items(Long planId) {
        return itemMapper.selectList(new QueryWrapper<AppraisalItem>()
                .eq("plan_id", planId).orderByAsc("id"));
    }

    // ---------- 自评 ----------

    @Transactional
    public AppraisalItem saveSelf(Long itemId, String selfContent, Integer participated) {
        AppraisalItem item = mustItem(itemId);
        if (participated != null && participated == 0) {
            item.setParticipated(0);
            item.setStage("未参加");
            item.setUpdateTime(LocalDateTime.now());
            itemMapper.updateById(item);
            return item;
        }
        if (selfContent == null || selfContent.isBlank()) {
            throw PartyBizException.block("E-APP-03", "自评内容不能为空");
        }
        item.setSelfContent(selfContent);
        if ("待自评".equals(item.getStage()) || "未参加".equals(item.getStage())) {
            item.setStage("已自评");
        }
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);
        return item;
    }

    // ---------- 互评/民主测评打分（四维加权） ----------

    @Transactional
    public AppraisalItem saveScores(Long itemId, BigDecimal politics, BigDecimal discipline,
                                    BigDecimal role, BigDecimal morality) {
        AppraisalItem item = mustItem(itemId);
        if (item.getParticipated() != null && item.getParticipated() == 0) {
            throw PartyBizException.block("E-APP-02", "该党员标记为未参加评议，不能录入得分");
        }
        item.setScorePolitics(politics);
        item.setScoreDiscipline(discipline);
        item.setScoreRole(role);
        item.setScoreMorality(morality);
        AppraisalPlan plan = mustPlan(item.getPlanId());
        double wP = 0.3, wD = 0.2, wR = 0.3, wM = 0.2;
        try {
            JsonNode w = JSON.readTree(plan.getWeightsJson());
            wP = w.path("politics").asDouble(0.3);
            wD = w.path("discipline").asDouble(0.2);
            wR = w.path("role").asDouble(0.3);
            wM = w.path("morality").asDouble(0.2);
        } catch (Exception ignored) {
            // 使用默认权重
        }
        double weighted = nz(politics) * wP + nz(discipline) * wD + nz(role) * wR + nz(morality) * wM;
        item.setWeightedScore(BigDecimal.valueOf(weighted).setScale(2, RoundingMode.HALF_UP));
        item.setMutualScore(BigDecimal.valueOf(weighted).setScale(2, RoundingMode.HALF_UP));
        if (!"已支评".equals(item.getStage()) && !"已审定".equals(item.getStage())) {
            item.setStage("已互评");
        }
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);
        return item;
    }

    private double nz(BigDecimal v) {
        return v == null ? 0 : v.doubleValue();
    }

    // ---------- 支部评定 ----------

    @Transactional
    public AppraisalItem saveBranch(Long itemId, String branchOpinion, String result) {
        AppraisalItem item = mustItem(itemId);
        if (item.getIsProbationary() != null && item.getIsProbationary() == 1) {
            // 预备党员不定等次
            if (result != null && !"不定等次".equals(result)) {
                throw PartyBizException.block("E-APP-04", "预备党员不定等次，不能评定为" + result);
            }
            item.setResult("不定等次");
        } else {
            if (result == null || !List.of("优秀", "合格", "基本合格", "不合格").contains(result)) {
                throw PartyBizException.block("E-APP-03", "评定等次非法：" + result);
            }
            item.setResult(result);
        }
        item.setBranchOpinion(branchOpinion);
        item.setStage("已支评");
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);
        return item;
    }

    /** 支评整体提交：优秀比例校验（阻断，可附上级同意说明 overrideNote 放行）。 */
    @Transactional
    public Map<String, Object> submitBranch(Long planId, String overrideNote) {
        AppraisalPlan plan = mustPlan(planId);
        List<AppraisalItem> items = items(planId);
        List<AppraisalItem> graded = items.stream()
                .filter(i -> i.getParticipated() == null || i.getParticipated() == 1)
                .filter(i -> i.getIsProbationary() == null || i.getIsProbationary() == 0)
                .collect(Collectors.toList());
        List<String> missing = graded.stream()
                .filter(i -> i.getResult() == null).map(AppraisalItem::getMemberName).collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw PartyBizException.block("E-APP-03", "以下党员尚未评定等次：" + String.join("、", missing));
        }
        long excellent = graded.stream().filter(i -> "优秀".equals(i.getResult())).count();
        BigDecimal ratio = BigDecimal.valueOf(excellent)
                .divide(BigDecimal.valueOf(Math.max(graded.size(), 1)), 4, RoundingMode.HALF_UP);
        boolean over = ratio.compareTo(plan.getExcellentRatio()) > 0;
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("excellent", excellent);
        r.put("graded", graded.size());
        r.put("ratio", ratio);
        r.put("ratioLimit", plan.getExcellentRatio());
        if (over && (overrideNote == null || overrideNote.isBlank())) {
            throw PartyBizException.block("E-APP-05",
                    "优秀比例" + ratio + "超过上限" + plan.getExcellentRatio()
                            + "（优秀" + excellent + "人/参评" + graded.size() + "人），须调整或上传上级同意说明");
        }
        if (over) {
            auditService.log("APPRAISAL_RATIO_OVERRIDE", "appraisal_plan", planId,
                    "优秀比例超限，附上级说明放行：" + overrideNote);
        }
        graded.forEach(i -> {
            i.setStage("已支评");
            i.setUpdateTime(LocalDateTime.now());
            itemMapper.updateById(i);
        });
        return r;
    }

    // ---------- 等次调整审批 ----------

    @Transactional
    public AppraisalItem requestAdjust(Long itemId, String result, String reason) {
        AppraisalItem item = mustItem(itemId);
        if (reason == null || reason.isBlank()) {
            throw PartyBizException.block("E-APP-06", "等次调整必须填写理由");
        }
        if (item.getIsProbationary() != null && item.getIsProbationary() == 1) {
            throw PartyBizException.block("E-APP-04", "预备党员不定等次");
        }
        item.setResult(result);
        item.setAdjustReason(reason);
        item.setAdjustStatus("待审批");
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);
        return item;
    }

    @Transactional
    public AppraisalItem auditAdjust(Long itemId, boolean pass, String opinion) {
        AppraisalItem item = mustItem(itemId);
        if (!"待审批".equals(item.getAdjustStatus())) {
            throw PartyBizException.block("E-APP-06", "该记录没有待审批的等次调整申请");
        }
        item.setAdjustStatus(pass ? "已批准" : "已驳回");
        item.setPartyOpinion(opinion);
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);
        auditService.log("APPRAISAL_ADJUST_AUDIT", "appraisal_item", itemId,
                (pass ? "批准调整" : "驳回调整") + " 等次=" + item.getResult());
        return item;
    }

    // ---------- 党委审定 / 公示 / 反馈 ----------

    @Transactional
    public AppraisalItem savePartyOpinion(Long itemId, String opinion) {
        AppraisalItem item = mustItem(itemId);
        item.setPartyOpinion(opinion);
        item.setStage("已审定");
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);
        return item;
    }

    @Transactional
    public AppraisalPlan publish(Long planId) {
        AppraisalPlan plan = mustPlan(planId);
        submitBranchSilentGuard(planId);
        plan.setStatus("已公示");
        plan.setPublishTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.updateById(plan);
        auditService.log("APPRAISAL_PUBLISH", "appraisal_plan", planId, "评议结果公示");
        return plan;
    }

    private void submitBranchSilentGuard(Long planId) {
        List<AppraisalItem> graded = items(planId).stream()
                .filter(i -> i.getIsProbationary() == null || i.getIsProbationary() == 0)
                .collect(Collectors.toList());
        boolean anyUnconfirmed = graded.stream().anyMatch(i -> i.getResult() == null);
        if (anyUnconfirmed) {
            throw PartyBizException.block("E-APP-03", "仍有党员未完成支部评定，不能公示");
        }
    }

    @Transactional
    public AppraisalPlan feedback(Long planId) {
        AppraisalPlan plan = mustPlan(planId);
        List<AppraisalItem> items = items(planId);
        for (AppraisalItem i : items) {
            if (i.getParticipated() != null && i.getParticipated() == 0) {
                continue;
            }
            i.setStage("已反馈");
            i.setFeedbackDate(LocalDate.now());
            i.setUpdateTime(LocalDateTime.now());
            itemMapper.updateById(i);
        }
        plan.setStatus("已反馈");
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.updateById(plan);
        auditService.log("APPRAISAL_FEEDBACK", "appraisal_plan", planId, "评议结果反馈完成");
        return plan;
    }

    // ---------- 进度 / 未参加 / 连续预警 ----------

    public Map<String, Object> progress(Long planId) {
        List<AppraisalItem> items = items(planId);
        int total = items.size();
        long selfDone = items.stream().filter(i -> i.getSelfContent() != null && !i.getSelfContent().isBlank()).count();
        long scored = items.stream().filter(i -> i.getWeightedScore() != null).count();
        List<String> notParticipated = items.stream()
                .filter(i -> i.getParticipated() != null && i.getParticipated() == 0)
                .map(AppraisalItem::getMemberName).collect(Collectors.toList());
        // 连续基本合格/不合格：查上年度 legacy 表
        List<String> continuous = new ArrayList<>();
        for (AppraisalItem i : items) {
            if ("基本合格".equals(i.getResult()) || "不合格".equals(i.getResult())) {
                Long prior = legacyMapper.selectCount(new QueryWrapper<DemocraticAppraisal>()
                        .eq("member_name", i.getMemberName())
                        .in("result", "基本合格", "不合格")
                        .apply("appraisal_year < {0}", mustPlan(planId).getAssessYear()));
                if (prior != null && prior > 0) {
                    continuous.add(i.getMemberName());
                }
            }
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("total", total);
        r.put("selfRate", rate(selfDone, total));
        r.put("mutualRate", rate(scored, total));
        r.put("selfDone", selfDone);
        r.put("scored", scored);
        r.put("notParticipated", notParticipated);
        r.put("continuousWarnings", continuous);
        return r;
    }

    private BigDecimal rate(long part, int total) {
        return total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(part * 100).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private AppraisalPlan mustPlan(Long id) {
        AppraisalPlan p = planMapper.selectById(id);
        if (p == null) {
            throw PartyBizException.block("E-APP-01", "评议计划不存在：" + id);
        }
        return p;
    }

    private AppraisalItem mustItem(Long id) {
        AppraisalItem i = itemMapper.selectById(id);
        if (i == null) {
            throw PartyBizException.block("E-APP-01", "评议明细不存在：" + id);
        }
        return i;
    }
}
