package com.example.culturemgt.behavior.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.culturemgt.behavior.entity.BehaviorAudit;
import com.example.culturemgt.behavior.mapper.BehaviorAuditMapper;
import com.example.culturemgt.common.cul.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BehaviorAuditService {

    private final BehaviorAuditMapper auditMapper;
    private final ScoreService scoreService;
    private final AuditService auditService;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String[] DIMS = {"客户导向", "奋斗担当", "艰苦奋斗", "自我批判"};

    public BehaviorAuditService(BehaviorAuditMapper auditMapper,
                                ScoreService scoreService,
                                AuditService auditService) {
        this.auditMapper = auditMapper;
        this.scoreService = scoreService;
        this.auditService = auditService;
    }

    public List<BehaviorAudit> adminList(String status, String dept, String targetUser) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        LambdaQueryWrapper<BehaviorAudit> w = new LambdaQueryWrapper<>();
        w.eq(BehaviorAudit::getDeleted, 0);
        if (status != null && !status.isBlank()) w.eq(BehaviorAudit::getStatus, status);
        if (dept != null && !dept.isBlank()) w.eq(BehaviorAudit::getTargetDept, dept);
        if (targetUser != null && !targetUser.isBlank()) w.eq(BehaviorAudit::getTargetUser, targetUser);
        w.orderByDesc(BehaviorAudit::getCreateTime);
        return auditMapper.selectList(w);
    }

    public List<BehaviorAudit> myAudit() {
        return auditMapper.selectList(
            new LambdaQueryWrapper<BehaviorAudit>()
                .eq(BehaviorAudit::getTargetUser, CurrentHeader.operator())
                .eq(BehaviorAudit::getDeleted, 0)
                .orderByDesc(BehaviorAudit::getCreateTime));
    }

    public BehaviorAudit get(Long id) {
        BehaviorAudit a = auditMapper.selectById(id);
        BehaviorFlow.require(a != null && a.getDeleted() == 0, "01", "稽核记录不存在");
        String me = CurrentHeader.operator();
        if (me.equals(a.getTargetUser()) || me.equals(a.getRegistrar())) return a;
        Set<String> roles = CulRole.currentRoles();
        if (CulRole.hasAny(CulRole.CULTURE_STAFF) || roles.contains("C07")) return a;
        if (roles.contains("C04")) return a;
        BehaviorFlow.require(false, "12", "无权查看他人稽核记录");
        return a;
    }

    @Transactional
    public BehaviorAudit create(BehaviorAudit a) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        validate(a);
        a.setAuditNo(nextNo());
        a.setRegistrar(CurrentHeader.operator());
        a.setStatus("已告知");
        a.setApprovalLog("[]");
        appendLog(a, "登记", true, "登记稽核记录", a.getRegistrar(), roles());
        a.setCreateTime(LocalDateTime.now());
        a.setUpdateTime(LocalDateTime.now());
        auditMapper.insert(a);
        auditService.log("CREATE", "cul_behavior_audit", a.getId(),
            "登记稽核 " + a.getAuditNo() + " 针对 " + a.getTargetUser());
        return a;
    }

    @Transactional
    public void confirm(Long id) {
        BehaviorAudit a = auditMapper.selectById(id);
        BehaviorFlow.require(a != null && a.getDeleted() == 0, "01", "稽核记录不存在");
        String me = CurrentHeader.operator();
        BehaviorFlow.require(me.equals(a.getTargetUser()), "04", "仅本人可确认");
        BehaviorFlow.require("已告知".equals(a.getStatus()), "02", "当前状态不允许确认");
        a.setStatus("已确认");
        a.setConfirmTime(LocalDateTime.now());
        appendLog(a, "确认", true, null, me, roles());
        a.setUpdateTime(LocalDateTime.now());
        auditMapper.updateById(a);
        auditService.log("CONFIRM", "cul_behavior_audit", id, me + " 确认稽核 " + a.getAuditNo());
    }

    @Transactional
    public void appeal(Long id, String reason) {
        BehaviorAudit a = auditMapper.selectById(id);
        BehaviorFlow.require(a != null && a.getDeleted() == 0, "01", "稽核记录不存在");
        String me = CurrentHeader.operator();
        BehaviorFlow.require(me.equals(a.getTargetUser()), "04", "仅本人可申诉");
        BehaviorFlow.require("已告知".equals(a.getStatus()), "02", "当前状态不允许申诉");
        BehaviorFlow.require(reason != null && reason.trim().length() >= 10, "03", "申诉理由不少于10字");
        // 7天内申诉
        BehaviorFlow.require(a.getConfirmTime() == null ||
            LocalDateTime.now().isBefore(a.getConfirmTime().plusDays(7)), "15", "申诉须在告知后7天内");
        a.setStatus("申诉中");
        Map<String, Object> appeal = new LinkedHashMap<>();
        appeal.put("reason", reason);
        appeal.put("by", me);
        appeal.put("time", LocalDateTime.now().toString());
        appeal.put("verdict", null);
        appeal.put("verdictBy", null);
        appeal.put("verdictTime", null);
        try {
            a.setAppealLog(MAPPER.writeValueAsString(Arrays.asList(appeal)));
        } catch (Exception ex) { }
        appendLog(a, "申诉", true, reason, me, roles());
        a.setUpdateTime(LocalDateTime.now());
        auditMapper.updateById(a);
        auditService.log("APPEAL", "cul_behavior_audit", id, me + " 申诉稽核 " + a.getAuditNo());
    }

    @Transactional
    public void verdictAppeal(Long id, boolean uphold, String opinion) {
        BehaviorAudit a = auditMapper.selectById(id);
        BehaviorFlow.require(a != null && a.getDeleted() == 0, "01", "稽核记录不存在");
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        BehaviorFlow.require("申诉中".equals(a.getStatus()), "02", "当前状态不允许裁决");
        BehaviorFlow.require(opinion != null && opinion.trim().length() >= 5, "03", "裁决意见不少于5字");
        String me = CurrentHeader.operator();
        if (uphold) {
            a.setStatus("整改实施中");
        } else {
            a.setStatus("申诉驳回已归档");
        }
        // 更新 appeal_log
        try {
            List<Map<String, Object>> log = MAPPER.readValue(a.getAppealLog() != null ? a.getAppealLog() : "[]",
                MAPPER.getTypeFactory().constructCollectionType(List.class, Map.class));
            if (!log.isEmpty()) {
                Map<String, Object> last = log.get(log.size() - 1);
                last.put("verdict", uphold ? "维持" : "驳回");
                last.put("verdictBy", me);
                last.put("verdictTime", LocalDateTime.now().toString());
                last.put("verdictOpinion", opinion);
            }
            a.setAppealLog(MAPPER.writeValueAsString(log));
        } catch (Exception ex) { }
        appendLog(a, uphold ? "申诉驳回" : "申诉成立", uphold, opinion, me, roles());
        a.setUpdateTime(LocalDateTime.now());
        auditMapper.updateById(a);
        auditService.log(uphold ? "APPEAL_REJECT" : "APPEAL_OK", "cul_behavior_audit", id,
            "裁决稽核 " + a.getAuditNo() + "：" + (uphold ? "维持原判" : "申诉成立"));
    }

    @Transactional
    public void submitRectify(Long id, String note, String evidenceJson) {
        BehaviorAudit a = auditMapper.selectById(id);
        BehaviorFlow.require(a != null && a.getDeleted() == 0, "01", "稽核记录不存在");
        String me = CurrentHeader.operator();
        BehaviorFlow.require(me.equals(a.getTargetUser()), "04", "仅本人可提交整改材料");
        BehaviorFlow.require("已确认".equals(a.getStatus()) || "整改实施中".equals(a.getStatus()), "02", "当前状态不允许提交整改");
        BehaviorFlow.require(note != null && note.trim().length() >= 10, "01", "整改说明不少于10字");
        BehaviorFlow.require(evidenceJson != null && evidenceJson.trim().length() > 2, "19", "验收材料不能为空");
        a.setStatus("待验收");
        a.setRectifyNote(note);
        a.setRectifyEvidence(evidenceJson);
        appendLog(a, "提交整改", true, null, me, roles());
        a.setUpdateTime(LocalDateTime.now());
        auditMapper.updateById(a);
        auditService.log("RECTIFY", "cul_behavior_audit", id, me + " 提交整改 " + a.getAuditNo());
    }

    @Transactional
    public void accept(Long id, boolean pass, String opinion) {
        BehaviorAudit a = auditMapper.selectById(id);
        BehaviorFlow.require(a != null && a.getDeleted() == 0, "01", "稽核记录不存在");
        String me = CurrentHeader.operator();
        Set<String> roles = CulRole.currentRoles();
        BehaviorFlow.require(roles.contains("C04") || roles.contains("C99") || CulRole.hasAny(CulRole.CULTURE_STAFF), "04", "仅主管或文化岗可验收");
        BehaviorFlow.require("待验收".equals(a.getStatus()), "02", "当前状态不允许验收");
        BehaviorFlow.require(opinion != null && opinion.trim().length() >= 5, "03", "验收意见不少于5字");
        if (pass) {
            a.setStatus("已生效");
            a.setAcceptOpinion(opinion);
            appendLog(a, "验收通过", true, opinion, me, roles());
            // 扣分入账
            Long txnId = scoreService.bookFromAudit(a);
            a.setPointTxnId(txnId);
        } else {
            a.setStatus("整改实施中");
            a.setAcceptOpinion(opinion);
            appendLog(a, "验收驳回", false, opinion, me, roles());
        }
        a.setUpdateTime(LocalDateTime.now());
        auditMapper.updateById(a);
        auditService.log(pass ? "ACCEPT_OK" : "ACCEPT_REJECT", "cul_behavior_audit", id,
            "验收稽核 " + a.getAuditNo() + "：" + (pass ? "通过" : "驳回"));
    }

    private void validate(BehaviorAudit a) {
        BehaviorFlow.require(a.getTargetUser() != null && !a.getTargetUser().isBlank(), "01", "关联员工必填");
        BehaviorFlow.require(a.getViolateDimension() != null && Arrays.asList(DIMS).contains(a.getViolateDimension()), "01", "违规价值观维度非法");
        BehaviorFlow.require(a.getViolateScene() != null && a.getViolateScene().trim().length() >= 2, "01", "违规场景必填");
        BehaviorFlow.require(a.getViolateDetail() != null && a.getViolateDetail().trim().length() >= 10, "01", "违规详情不少于10字");
        BehaviorFlow.require(a.getOccurTime() != null && !a.getOccurTime().isAfter(LocalDateTime.now()), "07", "发生时间不能在未来");
        BehaviorFlow.require(a.getDeductScore() != null && a.getDeductScore() >= 0 && a.getDeductScore() <= 10, "01", "扣分须在0-10之间");
        BehaviorFlow.require(a.getRectifyRequirement() != null && a.getRectifyRequirement().trim().length() >= 5, "01", "整改要求必填");
        BehaviorFlow.require(a.getRectifyDeadline() != null, "09", "整改期限必填");
        BehaviorFlow.require(a.getAttachments() != null && a.getAttachments().trim().length() > 2, "01", "佐证附件不能为空");
    }

    private String nextNo() {
        String prefix = "JG" + LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        long cnt = auditMapper.selectCount(
            new LambdaQueryWrapper<BehaviorAudit>()
                .likeRight(BehaviorAudit::getAuditNo, prefix)
                .eq(BehaviorAudit::getDeleted, 0));
        return prefix + String.format("%04d", cnt + 1);
    }

    private void appendLog(BehaviorAudit a, String action, boolean pass, String opinion, String by, Set<String> roles) {
        try {
            List<Map<String, Object>> log = MAPPER.readValue(a.getApprovalLog() != null ? a.getApprovalLog() : "[]",
                MAPPER.getTypeFactory().constructCollectionType(List.class, Map.class));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("action", action);
            item.put("pass", pass);
            item.put("opinion", opinion);
            item.put("by", by);
            item.put("roles", String.join(",", roles));
            item.put("time", LocalDateTime.now().toString());
            log.add(item);
            a.setApprovalLog(MAPPER.writeValueAsString(log));
        } catch (Exception ex) { }
    }

    private Set<String> roles() {
        return CulRole.currentRoles();
    }
}
