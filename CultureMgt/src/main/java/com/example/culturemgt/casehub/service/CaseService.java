package com.example.culturemgt.casehub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.foundation.service.FoundationFlow;
import com.example.culturemgt.casehub.entity.CaseItem;
import com.example.culturemgt.casehub.entity.CaseTag;
import com.example.culturemgt.casehub.mapper.CaseItemMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文化案例：员工投稿 → 部门初审(C04) → 子公司文化专员复审(C02) → 集团文化岗终审(C03) → 发布；
 * 终审按标签激励配置计积分；新闻/培训引用追加加分；禁物理删除仅归档。
 */
@Service
public class CaseService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired private CaseItemMapper mapper;
    @Autowired private CaseTagService tagService;
    @Autowired private AuditService audit;

    /** 案例广场（已发布；publicOnly=true 仅公开案例）。 */
    public List<CaseItem> square(String type, String tag, boolean publicOnly) {
        LambdaQueryWrapper<CaseItem> w = new LambdaQueryWrapper<CaseItem>().eq(CaseItem::getStatus, "已发布");
        if (publicOnly) w.eq(CaseItem::getIsPublic, 1);
        if (type != null && !type.isBlank()) w.eq(CaseItem::getCaseType, type);
        if (tag != null && !tag.isBlank()) w.like(CaseItem::getValueTags, tag);
        w.orderByDesc(CaseItem::getPublishTime).orderByDesc(CaseItem::getId);
        return mapper.selectList(w);
    }

    /** 我的投稿（按 X-User-Id）。 */
    public List<CaseItem> mine() {
        return mapper.selectList(new LambdaQueryWrapper<CaseItem>()
                .eq(CaseItem::getContributor, CurrentHeader.operator())
                .orderByDesc(CaseItem::getId));
    }

    /** 评审/管理台账。 */
    public List<CaseItem> adminList(String status, String type, String keyword) {
        LambdaQueryWrapper<CaseItem> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(CaseItem::getStatus, status);
        if (type != null && !type.isBlank()) w.eq(CaseItem::getCaseType, type);
        if (keyword != null && !keyword.isBlank()) w.like(CaseItem::getCaseTitle, keyword);
        w.orderByDesc(CaseItem::getId);
        return mapper.selectList(w);
    }

    public CaseItem get(Long id) {
        return mapper.selectById(id);
    }

    public CaseItem save(CaseItem c) {
        validateDraft(c);
        boolean negative = "反面警示".equals(c.getCaseType());
        if (c.getIsPublic() == null) {
            c.setIsPublic(negative ? 0 : 1);
        }
        if (negative) {
            c.setIsPublic(0); // 反面案例强制内部可见
        }
        if (c.getId() == null) {
            c.setContributor(CurrentHeader.operator());
            c.setStatus("草稿");
            c.setStage(0);
            c.setPointBase(0);
            c.setPointNews(0);
            c.setPointTraining(0);
            c.setPointTotal(0);
            c.setRefNewsCount(0);
            c.setRefTrainingCount(0);
            c.setCreateTime(LocalDateTime.now());
            mapper.insert(c);
            audit.log("NEW", "cul_case", c.getId(), CurrentHeader.operator() + " 投稿案例 " + c.getCaseTitle());
        } else {
            CaseItem old = mapper.selectById(c.getId());
            CaseFlow.require(old != null, "01", "案例不存在");
            CaseFlow.require("草稿".equals(old.getStatus()), "02", "仅草稿状态可编辑，当前状态：" + old.getStatus());
            boolean owner = CurrentHeader.operator().equals(old.getContributor());
            CaseFlow.require(owner || CulRole.hasAny(CulRole.C02, CulRole.C03, CulRole.C99),
                    "04", "仅投稿本人或文化管理岗可编辑该草稿");
            c.setContributor(old.getContributor());
            c.setStatus(old.getStatus());
            c.setStage(old.getStage());
            c.setPointBase(old.getPointBase());
            c.setPointNews(old.getPointNews());
            c.setPointTraining(old.getPointTraining());
            c.setPointTotal(old.getPointTotal());
            c.setRefNewsCount(old.getRefNewsCount());
            c.setRefTrainingCount(old.getRefTrainingCount());
            c.setUpdateTime(LocalDateTime.now());
            mapper.updateById(c);
        }
        return c;
    }

    public void submit(Long id) {
        CaseItem c = must(id);
        boolean owner = CurrentHeader.operator().equals(c.getContributor());
        CaseFlow.require(owner || CulRole.hasAny(CulRole.C02, CulRole.C03, CulRole.C99), "04", "仅投稿本人或文化岗可提交");
        CaseFlow.require("草稿".equals(c.getStatus()), "02", "仅草稿可提交评审，当前状态：" + c.getStatus());
        // 提交完整性：标签≥1、附件≥1
        CaseFlow.require(c.getValueTags() != null && !c.getValueTags().isBlank(), "05", "请至少选择一个价值观标签");
        CaseFlow.require(attachmentCount(c.getAttachments()) >= 1, "05", "请至少上传1个佐证附件");
        c.setStatus("待评审");
        c.setStage(1);
        c.setUpdateTime(LocalDateTime.now());
        mapper.updateById(c);
        audit.log("SUBMIT", "cul_case", id, CurrentHeader.operator() + " 提交案例评审");
    }

    public void approve(Long id, boolean pass, String opinion) {
        CaseItem c = must(id);
        CaseFlow.require("待评审".equals(c.getStatus()), "02", "仅待评审案例可审批，当前状态：" + c.getStatus());
        int stage = c.getStage() == null ? 0 : c.getStage();
        switch (stage) {
            case 1 -> CulRole.requireAny(CulRole.C04, CulRole.C99);
            case 2 -> CulRole.requireAny(CulRole.C02, CulRole.C99);
            default -> CulRole.requireAny(CulRole.C03, CulRole.C99);
        }
        if (!pass) {
            FoundationFlow.require(FoundationFlow.len(opinion) >= 5, "03", "驳回必须填写不少于5字的驳回意见");
            c.setApprovalLog(logOf(c, stage, false, opinion));
            c.setRejectReason(opinion);
            c.setStatus("草稿");
            c.setStage(0);
        } else if (stage < 3) {
            c.setApprovalLog(logOf(c, stage, true, opinion));
            c.setStage(stage + 1);
        } else {
            c.setApprovalLog(logOf(c, stage, true, opinion));
            publishAndScore(c);
        }
        c.setUpdateTime(LocalDateTime.now());
        mapper.updateById(c);
        audit.log(pass ? "APPROVE" : "REJECT", "cul_case", id,
                "案例节点" + stage + (pass ? "通过" : "驳回") + " " + (opinion == null ? "" : opinion));
    }

    public void toggleArchive(Long id) {
        CaseItem c = must(id);
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        if ("已发布".equals(c.getStatus())) {
            c.setStatus("已归档");
        } else if ("已归档".equals(c.getStatus())) {
            c.setStatus("已发布");
            if (c.getPublishTime() == null) c.setPublishTime(LocalDateTime.now());
        } else {
            CaseFlow.require(false, "02", "仅已发布/已归档案例可切换，当前状态：" + c.getStatus());
        }
        c.setUpdateTime(LocalDateTime.now());
        mapper.updateById(c);
        audit.log("ARCHIVE", "cul_case", id, "案例归档状态切换：" + c.getStatus());
    }

    /**
     * 引用加分（新闻宣传/培训模块后续调用）：type=news / training。
     */
    public void cite(Long id, String type) {
        CaseItem c = must(id);
        CulRole.requireAny(CulRole.C03, CulRole.C99);
        CaseFlow.require("已发布".equals(c.getStatus()), "06", "仅已发布案例可被引用，当前状态：" + c.getStatus());
        CaseTag tag = tagService.matchByFirstTag(c.getValueTags());
        int add;
        if ("news".equals(type)) {
            add = tag != null ? tag.getScoreNews() : 5;
            c.setPointNews(safe(c.getPointNews()) + add);
            c.setRefNewsCount(safe(c.getRefNewsCount()) + 1);
        } else if ("training".equals(type)) {
            add = tag != null ? tag.getScoreTraining() : 3;
            c.setPointTraining(safe(c.getPointTraining()) + add);
            c.setRefTrainingCount(safe(c.getRefTrainingCount()) + 1);
        } else {
            CaseFlow.require(false, "01", "引用类型非法（news/training）");
            return;
        }
        c.setPointTotal(safe(c.getPointBase()) + safe(c.getPointNews()) + safe(c.getPointTraining()));
        c.setPointDetail(buildDetail(c));
        c.setUpdateTime(LocalDateTime.now());
        mapper.updateById(c);
        audit.log("CITE", "cul_case", id, "被" + ("news".equals(type) ? "新闻" : "培训") + "引用，积分+" + add);
    }

    private void publishAndScore(CaseItem c) {
        boolean positive = "正向标杆".equals(c.getCaseType());
        CaseTag tag = tagService.matchByFirstTag(c.getValueTags());
        int base;
        if (tag != null) {
            base = positive ? tag.getScorePositive() : tag.getScoreNegative();
        } else {
            base = positive ? 10 : 5;
        }
        c.setPointBase(base);
        c.setPointTotal(base + safe(c.getPointNews()) + safe(c.getPointTraining()));
        c.setPointDetail(buildDetail(c));
        c.setStatus("已发布");
        c.setStage(3);
        c.setPublishTime(LocalDateTime.now());
    }

    private String buildDetail(CaseItem c) {
        StringBuilder sb = new StringBuilder();
        sb.append("基础").append(safe(c.getPointBase()));
        if (safe(c.getPointNews()) > 0) sb.append(" + 新闻引用").append(c.getPointNews());
        if (safe(c.getPointTraining()) > 0) sb.append(" + 培训引用").append(c.getPointTraining());
        sb.append(" = 合计").append(c.getPointTotal());
        return sb.toString();
    }

    private int safe(Integer n) {
        return n == null ? 0 : n;
    }

    private int attachmentCount(String json) {
        if (json == null || json.isBlank()) return 0;
        try {
            List<?> list = MAPPER.readValue(json, List.class);
            return list.size();
        } catch (Exception e) {
            return 0;
        }
    }

    private void validateDraft(CaseItem c) {
        int titleLen = CaseFlow.len(c.getCaseTitle());
        CaseFlow.require(titleLen >= 20 && titleLen <= 120, "01", "案例标题需20-120字（当前" + titleLen + "字）");
        CaseFlow.require("正向标杆".equals(c.getCaseType()) || "反面警示".equals(c.getCaseType()),
                "01", "案例类型须为正向标杆或反面警示");
        CaseFlow.require(c.getOccurTime() != null, "01", "事件发生时间必填");
        CaseFlow.require(!c.getOccurTime().isAfter(LocalDateTime.now()), "01", "事件发生时间不能晚于当前时间");
        CaseFlow.require(CaseFlow.len(c.getContent()) >= 80, "01", "案例正文不少于80字（当前" + CaseFlow.len(c.getContent()) + "字）");
    }

    private String logOf(CaseItem c, int stage, boolean pass, String opinion) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("stage", stage);
        e.put("pass", pass);
        e.put("opinion", opinion);
        e.put("by", CurrentHeader.operator());
        e.put("roles", CulRole.currentRoles());
        e.put("time", LocalDateTime.now().toString());
        return FoundationFlow.appendLog(c.getApprovalLog(), e);
    }

    private CaseItem must(Long id) {
        CaseItem c = mapper.selectById(id);
        CaseFlow.require(c != null, "01", "案例不存在");
        return c;
    }
}
