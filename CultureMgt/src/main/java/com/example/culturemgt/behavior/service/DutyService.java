package com.example.culturemgt.behavior.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.behavior.entity.BehaviorDuty;
import com.example.culturemgt.behavior.mapper.BehaviorDutyMapper;
import com.example.culturemgt.common.cul.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DutyService {

    private final BehaviorDutyMapper dutyMapper;
    private final AuditService auditService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public DutyService(BehaviorDutyMapper dutyMapper, AuditService auditService) {
        this.dutyMapper = dutyMapper;
        this.auditService = auditService;
    }

    public List<BehaviorDuty> mine() {
        return dutyMapper.selectList(
            new LambdaQueryWrapper<BehaviorDuty>()
                .eq(BehaviorDuty::getOfficer, CurrentHeader.operator())
                .eq(BehaviorDuty::getDeleted, 0)
                .orderByDesc(BehaviorDuty::getCreateTime));
    }

    public List<BehaviorDuty> todoList() {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        return dutyMapper.selectList(
            new LambdaQueryWrapper<BehaviorDuty>()
                .eq(BehaviorDuty::getStatus, "待审核")
                .eq(BehaviorDuty::getDeleted, 0)
                .orderByAsc(BehaviorDuty::getCreateTime));
    }

    public List<BehaviorDuty> adminList(String status, String period, String dept) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        LambdaQueryWrapper<BehaviorDuty> w = new LambdaQueryWrapper<>();
        w.eq(BehaviorDuty::getDeleted, 0);
        if (status != null && !status.isBlank()) w.eq(BehaviorDuty::getStatus, status);
        if (period != null && !period.isBlank()) w.eq(BehaviorDuty::getPeriodTag, period);
        if (dept != null && !dept.isBlank()) w.eq(BehaviorDuty::getDept, dept);
        w.orderByDesc(BehaviorDuty::getCreateTime);
        return dutyMapper.selectList(w);
    }

    public BehaviorDuty get(Long id) {
        BehaviorDuty d = dutyMapper.selectById(id);
        BehaviorFlow.require(d != null && d.getDeleted() == 0, "01", "履职记录不存在");
        return d;
    }

    @Transactional
    public BehaviorDuty save(BehaviorDuty d) {
        String me = CurrentHeader.operator();
        Set<String> roles = CulRole.currentRoles();
        BehaviorFlow.require(roles.contains("C04") || roles.contains("C99") || CulRole.hasAny(CulRole.CULTURE_STAFF), "04", "仅部门负责人可填报");
        if (d.getId() != null) {
            BehaviorDuty old = dutyMapper.selectById(d.getId());
            BehaviorFlow.require(old != null && old.getDeleted() == 0, "01", "记录不存在");
            BehaviorFlow.require(me.equals(old.getOfficer()), "04", "仅本人可编辑");
            BehaviorFlow.require("草稿".equals(old.getStatus()) || "已驳回".equals(old.getStatus()), "02", "当前状态不可编辑");
            d.setId(old.getId());
        } else {
            d.setOfficer(me);
            d.setStatus("草稿");
            d.setApprovalLog("[]");
        }
        validate(d);
        // 在途拦截
        if (d.getId() == null) {
            long inFlight = dutyMapper.selectCount(
                new LambdaQueryWrapper<BehaviorDuty>()
                    .eq(BehaviorDuty::getOfficer, me)
                    .eq(BehaviorDuty::getPeriodTag, d.getPeriodTag())
                    .notIn(BehaviorDuty::getStatus, Arrays.asList("已归档", "已驳回"))
                    .eq(BehaviorDuty::getDeleted, 0));
            BehaviorFlow.require(inFlight == 0, "06", "同周期在途记录已存在");
        }
        d.setUpdateTime(LocalDateTime.now());
        if (d.getId() == null) {
            d.setCreateTime(LocalDateTime.now());
            dutyMapper.insert(d);
        } else {
            dutyMapper.updateById(d);
        }
        return d;
    }

    @Transactional
    public void submit(Long id) {
        BehaviorDuty d = dutyMapper.selectById(id);
        BehaviorFlow.require(d != null && d.getDeleted() == 0, "01", "记录不存在");
        String me = CurrentHeader.operator();
        BehaviorFlow.require(me.equals(d.getOfficer()), "04", "仅本人可提交");
        BehaviorFlow.require("草稿".equals(d.getStatus()) || "已驳回".equals(d.getStatus()), "02", "当前状态不可提交");
        d.setStatus("待审核");
        appendLog(d, "提交", true, null, me, roles());
        d.setUpdateTime(LocalDateTime.now());
        dutyMapper.updateById(d);
        auditService.log("SUBMIT", "cul_behavior_duty", id, me + " 提交履职 " + d.getPeriodTag());
    }

    @Transactional
    public void review(Long id, boolean pass, String opinion) {
        BehaviorDuty d = dutyMapper.selectById(id);
        BehaviorFlow.require(d != null && d.getDeleted() == 0, "01", "记录不存在");
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        BehaviorFlow.require("待审核".equals(d.getStatus()), "02", "当前状态不可审核");
        String me = CurrentHeader.operator();
        if (!pass) {
            BehaviorFlow.require(opinion != null && opinion.trim().length() >= 5, "03", "驳回意见不少于5字");
            d.setStatus("已驳回");
            d.setReviewOpinion(opinion);
        } else {
            d.setStatus("已归档");
            d.setReviewOpinion(opinion);
        }
        d.setReviewer(me);
        d.setReviewTime(LocalDateTime.now());
        appendLog(d, pass ? "审核通过" : "审核驳回", pass, opinion, me, roles());
        d.setUpdateTime(LocalDateTime.now());
        dutyMapper.updateById(d);
        auditService.log(pass ? "REVIEW_OK" : "REVIEW_REJECT", "cul_behavior_duty", id,
            "审核履职 " + d.getPeriodTag() + "：" + (pass ? "通过" : "驳回"));
    }

    private void validate(BehaviorDuty d) {
        BehaviorFlow.require(d.getPeriodTag() != null && d.getPeriodTag().trim().length() >= 4, "14", "填报周期必填");
        BehaviorFlow.require(d.getDutyItems() != null && d.getDutyItems().trim().length() > 10, "14", "履职明细必填");
        try {
            List<Map<String, Object>> items = MAPPER.readValue(d.getDutyItems(),
                MAPPER.getTypeFactory().constructCollectionType(List.class, Map.class));
            BehaviorFlow.require(items.size() == 5, "14", "履职明细须包含5项");
        } catch (Exception e) {
            BehaviorFlow.require(false, "14", "履职明细格式错误");
        }
    }

    private void appendLog(BehaviorDuty d, String action, boolean pass, String opinion, String by, Set<String> roles) {
        try {
            List<Map<String, Object>> log = MAPPER.readValue(d.getApprovalLog() != null ? d.getApprovalLog() : "[]",
                MAPPER.getTypeFactory().constructCollectionType(List.class, Map.class));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("action", action);
            item.put("pass", pass);
            item.put("opinion", opinion);
            item.put("by", by);
            item.put("roles", String.join(",", roles));
            item.put("time", LocalDateTime.now().toString());
            log.add(item);
            d.setApprovalLog(MAPPER.writeValueAsString(log));
        } catch (Exception ex) { }
    }

    private Set<String> roles() {
        return CulRole.currentRoles();
    }
}
