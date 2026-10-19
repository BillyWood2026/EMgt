package com.example.culturemgt.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.activity.entity.CultureActivity;
import com.example.culturemgt.activity.mapper.CultureActivityMapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.foundation.service.FoundationFlow;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文化活动全流程：草稿→待审批(①部门负责人C04 ②文化专员C02/C03)→已发布(报名中)→进行中→待复盘(素材归档)→已闭环；
 * 已发布可取消。仅经办人本人/文化岗可改草稿；驳回回草稿意见≥5字。
 */
@Service
public class ActivityService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired private CultureActivityMapper mapper;
    @Autowired private AuditService audit;

    /** 前台活动广场：已发布报名中 + 进行中。 */
    public List<CultureActivity> square(String type) {
        LambdaQueryWrapper<CultureActivity> w = new LambdaQueryWrapper<CultureActivity>()
                .in(CultureActivity::getStatus, "已发布", "进行中");
        if (type != null && !type.isBlank()) w.eq(CultureActivity::getActType, type);
        w.orderByAsc(CultureActivity::getPlanStart);
        return mapper.selectList(w);
    }

    public List<CultureActivity> list(String status, String type, String keyword) {
        LambdaQueryWrapper<CultureActivity> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(CultureActivity::getStatus, status);
        if (type != null && !type.isBlank()) w.eq(CultureActivity::getActType, type);
        if (keyword != null && !keyword.isBlank()) w.like(CultureActivity::getActName, keyword);
        w.orderByDesc(CultureActivity::getId);
        return mapper.selectList(w);
    }

    public CultureActivity get(Long id) {
        return mapper.selectById(id);
    }

    public CultureActivity save(CultureActivity a) {
        validate(a);
        if (a.getId() == null) {
            a.setManager(CurrentHeader.operator());
            a.setStatus("草稿");
            a.setStage(0);
            a.setSignedCount(0);
            a.setCheckedCount(0);
            a.setCreateTime(LocalDateTime.now());
            mapper.insert(a);
            audit.log("NEW", "cul_activity", a.getId(), CurrentHeader.operator() + " 建活动 " + a.getActName());
        } else {
            CultureActivity old = mapper.selectById(a.getId());
            ActivityFlow.require(old != null, "01", "活动不存在");
            ActivityFlow.require("草稿".equals(old.getStatus()), "02", "仅草稿状态可编辑，当前状态：" + old.getStatus());
            boolean owner = CurrentHeader.operator().equals(old.getManager());
            ActivityFlow.require(owner || CulRole.hasAny(CulRole.C02, CulRole.C03, CulRole.C99),
                    "04", "仅经办人本人或文化岗可编辑该草稿");
            a.setManager(old.getManager());
            a.setStatus(old.getStatus());
            a.setStage(old.getStage());
            a.setSignedCount(old.getSignedCount());
            a.setCheckedCount(old.getCheckedCount());
            a.setApprovalLog(old.getApprovalLog());
            a.setUpdateTime(LocalDateTime.now());
            mapper.updateById(a);
        }
        return a;
    }

    public void submit(Long id) {
        CultureActivity a = must(id);
        boolean owner = CurrentHeader.operator().equals(a.getManager());
        ActivityFlow.require(owner || CulRole.hasAny(CulRole.C02, CulRole.C03, CulRole.C99), "04", "仅经办人或文化岗可提交审批");
        ActivityFlow.require("草稿".equals(a.getStatus()), "02", "仅草稿可提交，当前状态：" + a.getStatus());
        a.setStatus("待审批");
        a.setStage(1);
        a.setRejectReason(null);
        a.setUpdateTime(LocalDateTime.now());
        mapper.updateById(a);
        audit.log("SUBMIT", "cul_activity", id, CurrentHeader.operator() + " 提交活动审批");
    }

    public void approve(Long id, boolean pass, String opinion) {
        CultureActivity a = must(id);
        ActivityFlow.require("待审批".equals(a.getStatus()), "02", "仅待审批活动可审批，当前状态：" + a.getStatus());
        int stage = a.getStage() == null ? 0 : a.getStage();
        if (stage == 1) {
            CulRole.requireAny(CulRole.C04, CulRole.C99);
        } else {
            CulRole.requireAny(CulRole.C02, CulRole.C03, CulRole.C99);
        }
        if (!pass) {
            FoundationFlow.require(FoundationFlow.len(opinion) >= 5, "03", "驳回必须填写不少于5字的意见");
            a.setApprovalLog(logOf(a, stage, false, opinion));
            a.setRejectReason(opinion);
            a.setStatus("草稿");
            a.setStage(0);
        } else if (stage < 2) {
            a.setApprovalLog(logOf(a, stage, true, opinion));
            a.setStage(2);
        } else {
            a.setApprovalLog(logOf(a, stage, true, opinion));
            a.setStatus("已发布");
            a.setStage(2);
        }
        a.setUpdateTime(LocalDateTime.now());
        mapper.updateById(a);
        audit.log(pass ? "APPROVE" : "REJECT", "cul_activity", id,
                "活动节点" + stage + (pass ? "通过" : "驳回") + " " + (opinion == null ? "" : opinion));
    }

    /** 已发布 → 进行中（活动开始）。 */
    public void start(Long id) {
        CultureActivity a = must(id);
        CulRole.requireAny(CulRole.C02, CulRole.C03, CulRole.C04, CulRole.C99);
        ActivityFlow.require("已发布".equals(a.getStatus()), "02", "仅已发布活动可标记开始，当前状态：" + a.getStatus());
        a.setStatus("进行中");
        a.setUpdateTime(LocalDateTime.now());
        mapper.updateById(a);
        audit.log("START", "cul_activity", id, "活动开始进入进行中");
    }

    /** 进行中 → 待复盘（素材归档）。 */
    public void toReview(Long id) {
        CultureActivity a = must(id);
        CulRole.requireAny(CulRole.C02, CulRole.C03, CulRole.C04, CulRole.C99);
        ActivityFlow.require("进行中".equals(a.getStatus()), "02", "仅进行中活动可结束并归档素材，当前状态：" + a.getStatus());
        a.setStatus("待复盘");
        a.setUpdateTime(LocalDateTime.now());
        mapper.updateById(a);
        audit.log("FINISH", "cul_activity", id, "活动结束进入素材归档/待复盘");
    }

    /** 待复盘 → 已闭环：必须提交复盘总结与至少1个归档素材。 */
    public void close(Long id, String summary, String resultEval, String archiveFiles) {
        CultureActivity a = must(id);
        CulRole.requireAny(CulRole.C02, CulRole.C03, CulRole.C99);
        ActivityFlow.require("待复盘".equals(a.getStatus()), "02", "仅待复盘活动可闭环，当前状态：" + a.getStatus());
        FoundationFlow.require(FoundationFlow.len(summary) >= 10, "01", "复盘总结不少于10字");
        int files = attachmentCount(archiveFiles);
        ActivityFlow.require(files >= 1, "01", "闭环前须至少归档1个活动素材（照片/视频/纪要）");
        a.setSummary(summary);
        a.setResultEval(resultEval);
        a.setArchiveFiles(archiveFiles);
        a.setStatus("已闭环");
        a.setUpdateTime(LocalDateTime.now());
        mapper.updateById(a);
        audit.log("CLOSE", "cul_activity", id, "复盘闭环，素材" + files + "个");
    }

    /** 已发布 → 已取消（活动撤销）。 */
    public void cancel(Long id, String reason) {
        CultureActivity a = must(id);
        CulRole.requireAny(CulRole.C02, CulRole.C03, CulRole.C99);
        ActivityFlow.require("已发布".equals(a.getStatus()), "02", "仅已发布(报名中)活动可取消，当前状态：" + a.getStatus());
        FoundationFlow.require(FoundationFlow.len(reason) >= 5, "03", "取消活动须填写不少于5字的原因");
        a.setStatus("已取消");
        a.setRejectReason(reason);
        a.setUpdateTime(LocalDateTime.now());
        mapper.updateById(a);
        audit.log("CANCEL", "cul_activity", id, "活动取消：" + reason);
    }

    private int attachmentCount(String json) {
        if (json == null || json.isBlank()) return 0;
        try {
            return MAPPER.readValue(json, List.class).size();
        } catch (Exception e) {
            return 0;
        }
    }

    private void validate(CultureActivity a) {
        int n = ActivityFlow.len(a.getActName());
        ActivityFlow.require(n >= 10 && n <= 100, "01", "活动名称需10-100字（当前" + n + "字）");
        ActivityFlow.require(ActivityFlow.ACT_TYPES.contains(a.getActType()), "01", "活动类型非法，须为7类枚举之一");
        ActivityFlow.require(a.getPlanStart() != null && a.getPlanEnd() != null, "01", "计划开始/结束时间必填");
        ActivityFlow.require(!a.getPlanEnd().isBefore(a.getPlanStart()), "01", "计划结束时间不能早于开始时间");
        if (a.getSignDeadline() != null) {
            ActivityFlow.require(!a.getSignDeadline().isAfter(a.getPlanStart()), "01", "报名截止时间不能晚于活动开始时间");
        }
        ActivityFlow.require(a.getMaxSign() != null && a.getMaxSign() >= 0, "01", "报名上限须≥0（0表示不限）");
        ActivityFlow.require(a.getShouldCount() != null && a.getShouldCount() >= 0, "01", "应参与人数须≥0");
        ActivityFlow.require(ActivityFlow.len(a.getIntro()) <= 2000, "01", "活动简介不超过2000字");
    }

    private String logOf(CultureActivity a, int stage, boolean pass, String opinion) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("stage", stage);
        e.put("pass", pass);
        e.put("opinion", opinion);
        e.put("by", CurrentHeader.operator());
        e.put("roles", CulRole.currentRoles());
        e.put("time", LocalDateTime.now().toString());
        return FoundationFlow.appendLog(a.getApprovalLog(), e);
    }

    private CultureActivity must(Long id) {
        CultureActivity a = mapper.selectById(id);
        ActivityFlow.require(a != null, "01", "活动不存在");
        return a;
    }
}
