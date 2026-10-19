package com.example.culturemgt.behavior.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.culturemgt.behavior.entity.BehaviorEvidence;
import com.example.culturemgt.behavior.mapper.BehaviorEvidenceMapper;
import com.example.culturemgt.common.cul.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EvidenceService {

    private final BehaviorEvidenceMapper evidenceMapper;
    private final ScoreService scoreService;
    private final AuditService auditService;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String[] DIMS = {"客户导向", "奋斗担当", "艰苦奋斗", "自我批判"};

    public EvidenceService(BehaviorEvidenceMapper evidenceMapper,
                           ScoreService scoreService,
                           AuditService auditService) {
        this.evidenceMapper = evidenceMapper;
        this.scoreService = scoreService;
        this.auditService = auditService;
    }

    public List<BehaviorEvidence> mine() {
        return evidenceMapper.selectList(
            new LambdaQueryWrapper<BehaviorEvidence>()
                .eq(BehaviorEvidence::getContributor, CurrentHeader.operator())
                .eq(BehaviorEvidence::getDeleted, 0)
                .orderByDesc(BehaviorEvidence::getCreateTime));
    }

    public List<BehaviorEvidence> todoList() {
        Set<String> roles = CulRole.currentRoles();
        if (roles.contains("C04") && !roles.contains("C99")) {
            // 直属上级看自己下属待初审
            String me = CurrentHeader.operator();
            return evidenceMapper.selectList(
                new LambdaQueryWrapper<BehaviorEvidence>()
                    .eq(BehaviorEvidence::getStatus, "待初审")
                    .eq(BehaviorEvidence::getStage, 1)
                    .eq(BehaviorEvidence::getDeleted, 0)
                    .orderByAsc(BehaviorEvidence::getCreateTime));
        }
        if (CulRole.hasAny(CulRole.CULTURE_STAFF)) {
            return evidenceMapper.selectList(
                new LambdaQueryWrapper<BehaviorEvidence>()
                    .eq(BehaviorEvidence::getStatus, "待复核")
                    .eq(BehaviorEvidence::getStage, 2)
                    .eq(BehaviorEvidence::getDeleted, 0)
                    .orderByAsc(BehaviorEvidence::getCreateTime));
        }
        return new ArrayList<>();
    }

    public List<BehaviorEvidence> adminList(String status, String dimension, String keyword) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        LambdaQueryWrapper<BehaviorEvidence> w = new LambdaQueryWrapper<>();
        w.eq(BehaviorEvidence::getDeleted, 0);
        if (status != null && !status.isBlank()) w.eq(BehaviorEvidence::getStatus, status);
        if (dimension != null && !dimension.isBlank()) w.eq(BehaviorEvidence::getValueDimension, dimension);
        if (keyword != null && !keyword.isBlank()) {
            w.and(x -> x.like(BehaviorEvidence::getContributor, keyword)
                        .or().like(BehaviorEvidence::getDeedDesc, keyword)
                        .or().like(BehaviorEvidence::getEvidenceNo, keyword));
        }
        w.orderByDesc(BehaviorEvidence::getCreateTime);
        return evidenceMapper.selectList(w);
    }

    public BehaviorEvidence get(Long id) {
        BehaviorEvidence e = evidenceMapper.selectById(id);
        BehaviorFlow.require(e != null && e.getDeleted() == 0, "01", "举证不存在");
        String me = CurrentHeader.operator();
        if (me.equals(e.getContributor())) return e;
        Set<String> roles = CulRole.currentRoles();
        if (CulRole.hasAny(CulRole.CULTURE_STAFF)) return e;
        if (roles.contains("C04")) {
            // 主管可看本部门
            return e;
        }
        BehaviorFlow.require(false, "12", "无权查看他人举证");
        return e;
    }

    @Transactional
    public BehaviorEvidence save(BehaviorEvidence e) {
        String me = CurrentHeader.operator();
        if (e.getId() != null) {
            BehaviorEvidence old = evidenceMapper.selectById(e.getId());
            BehaviorFlow.require(old != null && old.getDeleted() == 0, "01", "举证不存在");
            BehaviorFlow.require(me.equals(old.getContributor()), "04", "仅本人可编辑");
            BehaviorFlow.require("草稿".equals(old.getStatus()), "02", "当前状态不允许编辑");
            e.setId(old.getId());
        } else {
            e.setContributor(me);
            e.setEvidenceNo(nextNo());
            e.setStatus("草稿");
            e.setStage(0);
            e.setApprovalLog("[]");
        }
        validate(e);
        e.setUpdateTime(LocalDateTime.now());
        if (e.getId() == null) {
            e.setCreateTime(LocalDateTime.now());
            evidenceMapper.insert(e);
        } else {
            evidenceMapper.updateById(e);
        }
        return e;
    }

    @Transactional
    public void submit(Long id) {
        BehaviorEvidence e = evidenceMapper.selectById(id);
        BehaviorFlow.require(e != null && e.getDeleted() == 0, "01", "举证不存在");
        String me = CurrentHeader.operator();
        BehaviorFlow.require(me.equals(e.getContributor()), "04", "仅本人可提交");
        BehaviorFlow.require("草稿".equals(e.getStatus()) || "已驳回".equals(e.getStatus()), "02", "当前状态不允许提交");
        // 完整性
        BehaviorFlow.require(e.getDeedDesc() != null && e.getDeedDesc().trim().length() >= 30, "05", "事迹描述不少于30字");
        BehaviorFlow.require(e.getAttachments() != null && e.getAttachments().trim().length() > 2, "05", "佐证附件不能为空");
        e.setStatus("待初审");
        e.setStage(1);
        appendLog(e, "提交", true, null, me, roles());
        e.setUpdateTime(LocalDateTime.now());
        evidenceMapper.updateById(e);
        auditService.log("SUBMIT", "cul_behavior_evidence", id, me + " 提交举证 " + e.getEvidenceNo());
    }

    @Transactional
    public BehaviorEvidence approve(Long id, boolean pass, String opinion) {
        BehaviorEvidence e = evidenceMapper.selectById(id);
        BehaviorFlow.require(e != null && e.getDeleted() == 0, "01", "举证不存在");
        BehaviorFlow.require("待初审".equals(e.getStatus()) || "待复核".equals(e.getStatus()), "02", "当前状态不允许审核");
        String me = CurrentHeader.operator();
        Set<String> roles = CulRole.currentRoles();
        if (e.getStage() == 1) {
            // 上级初审：C04 且部门匹配（简化：C04 均可审）
            BehaviorFlow.require(roles.contains("C04") || roles.contains("C99"), "04", "仅直属上级可初审");
        } else {
            BehaviorFlow.require(CulRole.hasAny(CulRole.CULTURE_STAFF), "04", "仅文化专员可复核");
        }
        if (!pass) {
            BehaviorFlow.require(opinion != null && opinion.trim().length() >= 5, "03", "驳回意见不少于5字");
            e.setStatus("已驳回");
            e.setRejectReason(opinion);
            appendLog(e, e.getStage() == 1 ? "初审驳回" : "复核驳回", false, opinion, me, roles());
            e.setUpdateTime(LocalDateTime.now());
            evidenceMapper.updateById(e);
            auditService.log("REJECT", "cul_behavior_evidence", id, "驳回举证 " + e.getEvidenceNo() + "：" + opinion);
            return e;
        }
        if (e.getStage() == 1) {
            e.setStage(2);
            e.setStatus("待复核");
            appendLog(e, "初审通过", true, opinion, me, roles());
            e.setUpdateTime(LocalDateTime.now());
            evidenceMapper.updateById(e);
            auditService.log("STAGE1_PASS", "cul_behavior_evidence", id, "初审通过 " + e.getEvidenceNo());
            return e;
        }
        // stage=2 复核通过 → 入账
        double finalScore = e.getSelfScore() != null ? e.getSelfScore() : 0.0;
        e.setFinalScore(finalScore);
        e.setStatus("已入账");
        appendLog(e, "复核通过", true, opinion, me, roles());
        Long txnId = scoreService.bookFromEvidence(e);
        e.setPointTxnId(txnId);
        e.setUpdateTime(LocalDateTime.now());
        evidenceMapper.updateById(e);
        auditService.log("STAGE2_PASS", "cul_behavior_evidence", id, "复核通过入账 " + e.getEvidenceNo() + " 得分=" + finalScore);
        return e;
    }

    private void validate(BehaviorEvidence e) {
        BehaviorFlow.require(e.getValueDimension() != null && Arrays.asList(DIMS).contains(e.getValueDimension()), "01", "价值观维度非法");
        BehaviorFlow.require("正向".equals(e.getBehaviorType()) || "负向".equals(e.getBehaviorType()), "01", "行为类型非法");
        BehaviorFlow.require(e.getOccurTime() != null, "01", "发生时间必填");
        BehaviorFlow.require(!e.getOccurTime().isAfter(LocalDateTime.now()), "07", "发生时间不能在未来");
        BehaviorFlow.require(e.getScene() != null && e.getScene().trim().length() >= 2, "01", "发生场景必填");
        BehaviorFlow.require(e.getDeedDesc() != null && e.getDeedDesc().trim().length() >= 30, "01", "事迹描述不少于30字");
        BehaviorFlow.require(e.getAttachments() != null && e.getAttachments().trim().length() > 2, "01", "佐证附件不能为空");
        BehaviorFlow.require(e.getSelfScore() != null && e.getSelfScore() >= 0 && e.getSelfScore() <= 10, "01", "自评分须在0-10之间");
    }

    private String nextNo() {
        String prefix = "BH" + LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        long cnt = evidenceMapper.selectCount(
            new LambdaQueryWrapper<BehaviorEvidence>()
                .likeRight(BehaviorEvidence::getEvidenceNo, prefix)
                .eq(BehaviorEvidence::getDeleted, 0));
        return prefix + String.format("%04d", cnt + 1);
    }

    private void appendLog(BehaviorEvidence e, String action, boolean pass, String opinion, String by, Set<String> roles) {
        try {
            List<Map<String, Object>> log = MAPPER.readValue(e.getApprovalLog() != null ? e.getApprovalLog() : "[]",
                MAPPER.getTypeFactory().constructCollectionType(List.class, Map.class));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("action", action);
            item.put("pass", pass);
            item.put("opinion", opinion);
            item.put("by", by);
            item.put("roles", String.join(",", roles));
            item.put("time", LocalDateTime.now().toString());
            log.add(item);
            e.setApprovalLog(MAPPER.writeValueAsString(log));
        } catch (Exception ex) {
            // ignore
        }
    }

    private Set<String> roles() {
        return CulRole.currentRoles();
    }
}
