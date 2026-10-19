package com.example.culturemgt.foundation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.foundation.entity.Glossary;
import com.example.culturemgt.foundation.mapper.GlossaryMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 文化释义：文化专员编制→提交→集团文化管理员 C03 审核生效。 */
@Service
public class GlossaryService {

    @Autowired private GlossaryMapper mapper;
    @Autowired private AuditService audit;

    public List<Glossary> list(String status, String valueTag) {
        LambdaQueryWrapper<Glossary> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(Glossary::getStatus, status);
        if (valueTag != null && !valueTag.isBlank()) w.like(Glossary::getValueTag, valueTag);
        w.orderByDesc(Glossary::getId);
        return mapper.selectList(w);
    }

    public Glossary save(Glossary g) {
        FoundationFlow.requireText(g.getValueTag(), 2, 64, "价值观标签", "01");
        FoundationFlow.requireText(g.getPositiveText(), 10, 2000, "正向行为释义", "01");
        FoundationFlow.require(FoundationFlow.len(g.getApplyOrg()) > 0, "01", "适用组织必填");
        if (g.getId() == null) {
            g.setStatus("草稿");
            g.setStage(0);
            g.setCreator(CurrentHeader.operator());
            g.setCreateTime(LocalDateTime.now());
            mapper.insert(g);
            audit.log("NEW", "cul_glossary", g.getId(), "编制释义 " + g.getValueTag());
        } else {
            Glossary old = mapper.selectById(g.getId());
            FoundationFlow.require(old != null, "01", "释义不存在");
            FoundationFlow.require("草稿".equals(old.getStatus()) || "归档".equals(old.getStatus()),
                    "06", "仅草稿可编辑，当前状态：" + old.getStatus());
            g.setStatus(old.getStatus());
            g.setUpdateTime(LocalDateTime.now());
            mapper.updateById(g);
        }
        return g;
    }

    public void submit(Long id) {
        Glossary g = must(id);
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        FoundationFlow.require("草稿".equals(g.getStatus()) || "归档".equals(g.getStatus()),
                "03", "仅草稿可提交审核，当前状态：" + g.getStatus());
        g.setStatus("待审");
        g.setStage(1);
        g.setUpdateTime(LocalDateTime.now());
        mapper.updateById(g);
        audit.log("SUBMIT", "cul_glossary", id, "提交释义审核");
    }

    public void approve(Long id, boolean pass, String opinion) {
        Glossary g = must(id);
        FoundationFlow.require("待审".equals(g.getStatus()), "03", "仅待审单据可审核，当前状态：" + g.getStatus());
        CulRole.requireAny(CulRole.C03, CulRole.C99);
        if (!pass) {
            FoundationFlow.require(FoundationFlow.len(opinion) >= 5, "04", "驳回必须填写不少于5字的驳回意见");
            g.setStatus("草稿");
        } else {
            g.setStatus("生效");
        }
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("pass", pass);
        e.put("opinion", opinion);
        e.put("by", CurrentHeader.operator());
        e.put("time", LocalDateTime.now().toString());
        g.setApprovalLog(FoundationFlow.appendLog(g.getApprovalLog(), e));
        g.setStage(0);
        g.setUpdateTime(LocalDateTime.now());
        mapper.updateById(g);
        audit.log(pass ? "APPROVE" : "REJECT", "cul_glossary", id, (pass ? "审核通过" : "驳回") + " " + nullToEmpty(opinion));
    }

    public void archive(Long id) {
        Glossary g = must(id);
        FoundationFlow.require("生效".equals(g.getStatus()), "03", "仅生效释义可归档，当前状态：" + g.getStatus());
        CulRole.requireAny(CulRole.C03, CulRole.C99);
        g.setStatus("归档");
        g.setUpdateTime(LocalDateTime.now());
        mapper.updateById(g);
        audit.log("ARCHIVE", "cul_glossary", id, "释义归档");
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private Glossary must(Long id) {
        Glossary g = mapper.selectById(id);
        FoundationFlow.require(g != null, "01", "释义不存在");
        return g;
    }
}
