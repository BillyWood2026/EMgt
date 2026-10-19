package com.example.culturemgt.foundation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.foundation.entity.Institution;
import com.example.culturemgt.foundation.entity.InstitutionLearn;
import com.example.culturemgt.foundation.mapper.InstitutionLearnMapper;
import com.example.culturemgt.foundation.mapper.InstitutionMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文化制度台账：草稿→提交→文化部门初审(C02/C03)→人力复核(C05)→法务(C06)→高管终审(C07)→生效；
 * 修订复制新版本(Vx.y+0.1)，终审通过旧版自动归档；合规覆盖率=已学人数/应学人数。
 */
@Service
public class InstitutionService {

    @Autowired private InstitutionMapper mapper;
    @Autowired private InstitutionLearnMapper learnMapper;
    @Autowired private AuditService audit;

    public List<Institution> list(String status, String docType, String docName) {
        LambdaQueryWrapper<Institution> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(Institution::getStatus, status);
        if (docType != null && !docType.isBlank()) w.eq(Institution::getDocType, docType);
        if (docName != null && !docName.isBlank()) w.like(Institution::getDocName, docName);
        w.orderByDesc(Institution::getId);
        return mapper.selectList(w);
    }

    public Institution get(Long id) {
        return mapper.selectById(id);
    }

    public Institution save(Institution d) {
        validate(d);
        checkNameUnique(d);
        if (d.getId() == null) {
            d.setVersion(d.getVersion() == null || d.getVersion().isBlank() ? "V1.0" : d.getVersion());
            d.setStatus("草稿");
            d.setStage(0);
            if (d.getShouldCount() == null) d.setShouldCount(0);
            d.setLearnedCount(0);
            d.setCreator(CurrentHeader.operator());
            d.setCreateTime(LocalDateTime.now());
            mapper.insert(d);
            audit.log("NEW", "cul_institution", d.getId(), "编制制度 " + d.getDocName());
        } else {
            Institution old = mapper.selectById(d.getId());
            FoundationFlow.require(old != null, "01", "制度不存在");
            FoundationFlow.require("草稿".equals(old.getStatus()), "06", "仅草稿可编辑，当前状态：" + old.getStatus());
            d.setStatus(old.getStatus());
            d.setStage(old.getStage());
            d.setLearnedCount(old.getLearnedCount());
            d.setUpdateTime(LocalDateTime.now());
            mapper.updateById(d);
        }
        return d;
    }

    public void submit(Long id) {
        Institution d = must(id);
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        FoundationFlow.require("草稿".equals(d.getStatus()), "03", "仅草稿可提交，当前状态：" + d.getStatus());
        d.setStatus("待审");
        d.setStage(1);
        d.setUpdateTime(LocalDateTime.now());
        mapper.updateById(d);
        audit.log("SUBMIT", "cul_institution", id, "提交制度审批");
    }

    public void approve(Long id, boolean pass, String opinion) {
        Institution d = must(id);
        FoundationFlow.require("待审".equals(d.getStatus()), "03", "仅待审单据可审批，当前状态：" + d.getStatus());
        int stage = d.getStage() == null ? 0 : d.getStage();
        switch (stage) {
            case 1 -> CulRole.requireAny(CulRole.C02, CulRole.C03, CulRole.C99);
            case 2 -> CulRole.requireAny(CulRole.C05, CulRole.C99);
            case 3 -> CulRole.requireAny(CulRole.C06, CulRole.C99);
            default -> CulRole.requireAny(CulRole.C07, CulRole.C99);
        }
        if (!pass) {
            FoundationFlow.require(FoundationFlow.len(opinion) >= 5, "04", "驳回必须填写不少于5字的驳回意见");
            d.setApprovalLog(logOf(d, stage, false, opinion));
            d.setStatus("草稿");
            d.setStage(0);
        } else if (stage < 4) {
            d.setApprovalLog(logOf(d, stage, true, opinion));
            d.setStage(stage + 1);
        } else {
            d.setApprovalLog(logOf(d, stage, true, opinion));
            d.setStatus("生效");
            d.setStage(4);
            archiveSameName(d);
        }
        d.setUpdateTime(LocalDateTime.now());
        mapper.updateById(d);
        audit.log(pass ? "APPROVE" : "REJECT", "cul_institution", id,
                "节点" + stage + (pass ? "通过" : "驳回") + " " + (opinion == null ? "" : opinion));
    }

    /** 对生效制度发起修订：复制为新草稿，版本号+0.1，原版本保持生效。 */
    public Institution revise(Long id, String reason) {
        Institution src = must(id);
        FoundationFlow.require("生效".equals(src.getStatus()), "03", "仅生效制度可发起修订，当前状态：" + src.getStatus());
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        FoundationFlow.require(FoundationFlow.len(reason) >= 5, "01", "修订原因不少于5字");
        Institution n = new Institution();
        n.setDocName(src.getDocName());
        n.setDocType(src.getDocType());
        n.setApplyOrg(src.getApplyOrg());
        n.setApplyPost(src.getApplyPost());
        n.setEffectDate(src.getEffectDate());
        n.setVersion(FoundationFlow.bumpVersion(src.getVersion()));
        n.setDept(src.getDept());
        n.setSummary(src.getSummary());
        n.setKeyClause(src.getKeyClause());
        n.setFileUrl(src.getFileUrl());
        n.setShouldCount(src.getShouldCount());
        n.setLearnedCount(0);
        n.setStatus("草稿");
        n.setStage(0);
        n.setReviseReason(reason);
        n.setParentId(src.getId());
        n.setCreator(CurrentHeader.operator());
        n.setCreateTime(LocalDateTime.now());
        mapper.insert(n);
        audit.log("REVISE", "cul_institution", n.getId(),
                "基于#" + id + " " + src.getVersion() + " 发起修订：" + reason);
        return n;
    }

    /** 员工完成制度学习（幂等）。 */
    public void learn(Long id, String userName, String orgName) {
        Institution d = must(id);
        FoundationFlow.require("生效".equals(d.getStatus()), "03", "仅生效制度可学习确认");
        String user = (userName == null || userName.isBlank()) ? CurrentHeader.operator() : userName;
        Long exists = learnMapper.selectCount(new LambdaQueryWrapper<InstitutionLearn>()
                .eq(InstitutionLearn::getInstitutionId, id).eq(InstitutionLearn::getUserName, user));
        if (exists == 0) {
            InstitutionLearn l = new InstitutionLearn();
            l.setInstitutionId(id);
            l.setUserName(user);
            l.setOrgName(orgName);
            l.setLearnTime(LocalDateTime.now());
            learnMapper.insert(l);
        }
        Long cnt = learnMapper.selectCount(new LambdaQueryWrapper<InstitutionLearn>()
                .eq(InstitutionLearn::getInstitutionId, id));
        d.setLearnedCount(cnt.intValue());
        d.setUpdateTime(LocalDateTime.now());
        mapper.updateById(d);
        audit.log("LEARN", "cul_institution", id, user + " 完成制度学习");
    }

    public List<InstitutionLearn> learners(Long id) {
        return learnMapper.selectList(new LambdaQueryWrapper<InstitutionLearn>()
                .eq(InstitutionLearn::getInstitutionId, id).orderByDesc(InstitutionLearn::getLearnTime));
    }

    public double complianceRate(Long id) {
        Institution d = must(id);
        if (d.getShouldCount() == null || d.getShouldCount() <= 0) return 0d;
        return Math.round(d.getLearnedCount() * 10000.0 / d.getShouldCount()) / 100.0;
    }

    private void archiveSameName(Institution d) {
        List<Institution> actives = mapper.selectList(new LambdaQueryWrapper<Institution>()
                .eq(Institution::getStatus, "生效").eq(Institution::getDocName, d.getDocName()));
        for (Institution a : actives) {
            if (a.getId().equals(d.getId())) continue;
            a.setStatus("归档");
            a.setUpdateTime(LocalDateTime.now());
            mapper.updateById(a);
        }
    }

    private void checkNameUnique(Institution d) {
        LambdaQueryWrapper<Institution> w = new LambdaQueryWrapper<Institution>()
                .eq(Institution::getDocName, d.getDocName())
                .in(Institution::getStatus, java.util.List.of("草稿", "待审", "生效", "修订中"));
        if (d.getId() != null) w.ne(Institution::getId, d.getId());
        Long cnt = mapper.selectCount(w);
        FoundationFlow.require(cnt == 0, "05", "制度名称「" + d.getDocName() + "」在未归档版本中已存在，同分类下名称须唯一");
    }

    private void validate(Institution d) {
        FoundationFlow.requireText(d.getDocName(), 2, 160, "制度名称", "01");
        FoundationFlow.require(FoundationFlow.len(d.getDocType()) > 0, "01", "制度分类必选");
        FoundationFlow.requireText(d.getSummary(), 50, 800, "制度摘要", "01");
        FoundationFlow.require(FoundationFlow.len(d.getKeyClause()) > 0, "01", "关键约束条款必填");
        FoundationFlow.require(d.getEffectDate() != null, "01", "生效日期必填");
        FoundationFlow.requireDateRange(d.getEffectDate(), d.getExpireDate(), "01");
        FoundationFlow.require(d.getFileUrl() != null && !d.getFileUrl().isBlank(), "01", "制度原文附件必传(PDF/Word)");
    }

    private String logOf(Institution d, int stage, boolean pass, String opinion) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("stage", stage);
        e.put("pass", pass);
        e.put("opinion", opinion);
        e.put("by", CurrentHeader.operator());
        e.put("roles", CulRole.currentRoles());
        e.put("time", LocalDateTime.now().toString());
        return FoundationFlow.appendLog(d.getApprovalLog(), e);
    }

    private Institution must(Long id) {
        Institution d = mapper.selectById(id);
        FoundationFlow.require(d != null, "01", "制度不存在");
        return d;
    }
}
