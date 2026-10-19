package com.example.culturemgt.foundation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.foundation.entity.Creed;
import com.example.culturemgt.foundation.mapper.CreedMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 文化纲领：编制→提交→人力初审(C05)→法务审核(C06)→高管终审(C07)→生效，旧版自动归档。 */
@Service
public class CreedService {

    @Autowired private CreedMapper mapper;
    @Autowired private AuditService audit;

    public List<Creed> list(String status, String applyOrg) {
        LambdaQueryWrapper<Creed> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(Creed::getStatus, status);
        if (applyOrg != null && !applyOrg.isBlank()) w.like(Creed::getApplyOrg, applyOrg);
        w.orderByDesc(Creed::getId);
        return mapper.selectList(w);
    }

    public Creed get(Long id) {
        return mapper.selectById(id);
    }

    public Creed active() {
        return mapper.selectOne(new LambdaQueryWrapper<Creed>()
                .eq(Creed::getStatus, "生效").orderByDesc(Creed::getEffectDate).orderByDesc(Creed::getId).last("limit 1"));
    }

    public Creed save(Creed c) {
        validate(c);
        if (c.getId() == null) {
            c.setVersion(c.getVersion() == null || c.getVersion().isBlank() ? "V1.0" : c.getVersion());
            c.setStatus("草稿");
            c.setStage(0);
            c.setCreator(CurrentHeader.operator());
            c.setCreateTime(LocalDateTime.now());
            mapper.insert(c);
            audit.log("NEW", "cul_creed", c.getId(), "编制纲领 " + c.getCreedName() + " " + c.getVersion());
        } else {
            Creed old = mapper.selectById(c.getId());
            FoundationFlow.require(old != null, "01", "纲领版本不存在");
            FoundationFlow.require("草稿".equals(old.getStatus()), "06", "仅草稿状态可编辑，当前状态：" + old.getStatus());
            c.setStatus(old.getStatus());
            c.setStage(old.getStage());
            c.setUpdateTime(LocalDateTime.now());
            mapper.updateById(c);
            audit.log("EDIT", "cul_creed", c.getId(), "编辑纲领 " + c.getCreedName());
        }
        return c;
    }

    public void submit(Long id) {
        Creed c = must(id);
        FoundationFlow.require("草稿".equals(c.getStatus()), "03", "仅草稿可提交审批，当前状态：" + c.getStatus());
        c.setStatus("待审");
        c.setStage(1);
        c.setUpdateTime(LocalDateTime.now());
        mapper.updateById(c);
        audit.log("SUBMIT", "cul_creed", id, CurrentHeader.operator() + " 提交纲领审批");
    }

    /**
     * 节点审批：stage 1 人力负责人C05 / 2 法务C06 / 3 高管C07。
     */
    public void approve(Long id, boolean pass, String opinion) {
        Creed c = must(id);
        FoundationFlow.require("待审".equals(c.getStatus()), "03", "仅待审单据可审批，当前状态：" + c.getStatus());
        int stage = c.getStage() == null ? 0 : c.getStage();
        String[] nodeRoles = switch (stage) {
            case 1 -> new String[]{CulRole.C05, CulRole.C99};
            case 2 -> new String[]{CulRole.C06, CulRole.C99};
            case 3 -> new String[]{CulRole.C07, CulRole.C99};
            default -> new String[]{CulRole.C07, CulRole.C99};
        };
        CulRole.requireAny(nodeRoles);
        if (!pass) {
            FoundationFlow.require(FoundationFlow.len(opinion) >= 5, "04", "驳回必须填写不少于5字的驳回意见");
            c.setApprovalLog(logOf(c, stage, false, opinion));
            c.setStatus("草稿");
            c.setStage(0);
        } else if (stage < 3) {
            c.setApprovalLog(logOf(c, stage, true, opinion));
            c.setStage(stage + 1);
        } else {
            c.setApprovalLog(logOf(c, stage, true, opinion));
            c.setStatus("生效");
            c.setStage(3);
            archiveOthers(c);
        }
        c.setUpdateTime(LocalDateTime.now());
        mapper.updateById(c);
        audit.log(pass ? "APPROVE" : "REJECT", "cul_creed", id,
                "节点" + stage + (pass ? "通过" : "驳回") + " " + (opinion == null ? "" : opinion));
    }

    public void archive(Long id, String reason) {
        Creed c = must(id);
        FoundationFlow.require("生效".equals(c.getStatus()), "03", "仅生效版本可作废归档，当前状态：" + c.getStatus());
        FoundationFlow.require(FoundationFlow.len(reason) >= 5, "01", "作废原因不少于5字");
        CulRole.requireAny(CulRole.C07, CulRole.C99);
        c.setStatus("作废归档");
        c.setArchiveReason(reason);
        c.setUpdateTime(LocalDateTime.now());
        mapper.updateById(c);
        audit.log("ARCHIVE", "cul_creed", id, "纲领作废归档：" + reason);
    }

    private void archiveOthers(Creed c) {
        List<Creed> actives = mapper.selectList(new LambdaQueryWrapper<Creed>().eq(Creed::getStatus, "生效"));
        for (Creed a : actives) {
            if (a.getId().equals(c.getId())) continue;
            if (a.getApplyOrg() == null || a.getApplyOrg().equals(c.getApplyOrg())) {
                a.setStatus("作废归档");
                a.setArchiveReason("新版本 " + c.getVersion() + " 终审通过自动归档");
                a.setUpdateTime(LocalDateTime.now());
                mapper.updateById(a);
            }
        }
    }

    private String logOf(Creed c, int stage, boolean pass, String opinion) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("stage", stage);
        e.put("pass", pass);
        e.put("opinion", opinion);
        e.put("by", CurrentHeader.operator());
        e.put("roles", CulRole.currentRoles());
        e.put("time", LocalDateTime.now().toString());
        return FoundationFlow.appendLog(c.getApprovalLog(), e);
    }

    private void validate(Creed c) {
        FoundationFlow.requireText(c.getMission(), 10, 500, "企业使命", "01");
        FoundationFlow.requireText(c.getVision(), 10, 500, "企业愿景", "01");
        FoundationFlow.require(FoundationFlow.len(c.getValueGroups()) > 2, "01", "核心价值观分组必填");
        FoundationFlow.require(FoundationFlow.len(c.getApplyOrg()) > 0, "01", "适用组织范围必选");
        FoundationFlow.require(c.getEffectDate() != null, "01", "生效时间必填");
        FoundationFlow.requireDateRange(c.getEffectDate(), c.getExpireDate(), "01");
        FoundationFlow.requireText(c.getCreedName(), 2, 120, "文化体系名称", "01");
    }

    private Creed must(Long id) {
        Creed c = mapper.selectById(id);
        FoundationFlow.require(c != null, "01", "纲领版本不存在");
        return c;
    }
}
