package com.example.culturemgt.foundation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.foundation.entity.Redline;
import com.example.culturemgt.foundation.mapper.RedlineMapper;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文化红线：编号自动(HX+年份+3位)，草稿→提交→文化审核(C03)→人力合规复核(C05)→高管终审(C07)→生效；
 * 重大违规自动携带评优一票否决，否决锁定不可人工解除；禁物理删除仅归档。
 */
@Service
public class RedlineService {

    private static final List<String> LEVELS = List.of("一般", "较重", "严重", "重大");

    @Autowired private RedlineMapper mapper;
    @Autowired private AuditService audit;

    public List<Redline> list(String status, String level, String category) {
        LambdaQueryWrapper<Redline> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(Redline::getStatus, status);
        if (level != null && !level.isBlank()) w.eq(Redline::getViolationLevel, level);
        if (category != null && !category.isBlank()) w.eq(Redline::getCategory, category);
        w.orderByDesc(Redline::getId);
        return mapper.selectList(w);
    }

    public Redline get(Long id) {
        return mapper.selectById(id);
    }

    public Redline save(Redline r) {
        validate(r);
        CulRole.requireAny(CulRole.C03, CulRole.C07, CulRole.C99);
        if (r.getId() == null) {
            r.setCode(nextCode());
            r.setStatus("草稿");
            r.setStage(0);
            r.setRelatedCount(0);
            if (r.getVetoFlag() == null) r.setVetoFlag(0);
            if (r.getScopeText() == null || r.getScopeText().isBlank()) r.setScopeText("全员");
            r.setCreator(CurrentHeader.operator());
            r.setCreateTime(LocalDateTime.now());
            mapper.insert(r);
            audit.log("NEW", "cul_redline", r.getId(), "编制红线 " + r.getCode());
        } else {
            Redline old = mapper.selectById(r.getId());
            FoundationFlow.require(old != null, "01", "红线条款不存在");
            FoundationFlow.require("草稿".equals(old.getStatus()), "06", "仅草稿可编辑，当前状态：" + old.getStatus());
            // 重大等级的否决标记由系统自动携带，不允许人工取消
            if ("重大".equals(r.getViolationLevel())) {
                r.setVetoFlag(1);
            }
            r.setCode(old.getCode());
            r.setStatus(old.getStatus());
            r.setStage(old.getStage());
            r.setRelatedCount(old.getRelatedCount());
            r.setUpdateTime(LocalDateTime.now());
            mapper.updateById(r);
        }
        return r;
    }

    public void submit(Long id) {
        Redline r = must(id);
        FoundationFlow.require("草稿".equals(r.getStatus()), "03", "仅草稿可提交，当前状态：" + r.getStatus());
        r.setStatus("待审");
        r.setStage(1);
        r.setUpdateTime(LocalDateTime.now());
        mapper.updateById(r);
        audit.log("SUBMIT", "cul_redline", id, "提交红线审批");
    }

    public void approve(Long id, boolean pass, String opinion) {
        Redline r = must(id);
        FoundationFlow.require("待审".equals(r.getStatus()), "03", "仅待审单据可审批，当前状态：" + r.getStatus());
        int stage = r.getStage() == null ? 0 : r.getStage();
        switch (stage) {
            case 1 -> CulRole.requireAny(CulRole.C03, CulRole.C99);
            case 2 -> CulRole.requireAny(CulRole.C05, CulRole.C99);
            default -> CulRole.requireAny(CulRole.C07, CulRole.C99);
        }
        if (!pass) {
            FoundationFlow.require(FoundationFlow.len(opinion) >= 5, "04", "驳回必须填写不少于5字的驳回意见");
            r.setApprovalLog(logOf(r, stage, false, opinion));
            r.setStatus("草稿");
            r.setStage(0);
        } else if (stage < 3) {
            r.setApprovalLog(logOf(r, stage, true, opinion));
            r.setStage(stage + 1);
        } else {
            r.setApprovalLog(logOf(r, stage, true, opinion));
            r.setStatus("生效");
            r.setStage(3);
            if ("重大".equals(r.getViolationLevel())) {
                r.setVetoFlag(1);
            }
        }
        r.setUpdateTime(LocalDateTime.now());
        mapper.updateById(r);
        audit.log(pass ? "APPROVE" : "REJECT", "cul_redline", id,
                "节点" + stage + (pass ? "通过" : "驳回") + " " + (opinion == null ? "" : opinion));
    }

    public void archive(Long id, String reason) {
        Redline r = must(id);
        FoundationFlow.require("生效".equals(r.getStatus()), "03", "仅生效条款可归档，当前状态：" + r.getStatus());
        CulRole.requireAny(CulRole.C07, CulRole.C99);
        FoundationFlow.require(FoundationFlow.len(reason) >= 5, "01", "归档原因不少于5字");
        r.setStatus("归档");
        r.setUpdateTime(LocalDateTime.now());
        mapper.updateById(r);
        audit.log("ARCHIVE", "cul_redline", id, "红线归档：" + reason);
    }

    private synchronized String nextCode() {
        String prefix = "HX" + Year.now().getValue();
        // 取号必须覆盖逻辑删除行：cul_redline.code 有唯一索引，count+1 会与已删除编号撞号
        String maxCode = mapper.selectMaxCode(prefix);
        int seq = 0;
        if (maxCode != null && maxCode.length() >= prefix.length() + 1) {
            try {
                seq = Integer.parseInt(maxCode.substring(prefix.length()));
            } catch (NumberFormatException ignore) {
                seq = 0;
            }
        }
        return prefix + String.format("%03d", seq + 1);
    }

    private void validate(Redline r) {
        FoundationFlow.require(FoundationFlow.len(r.getCategory()) > 0, "01", "红线分类必选");
        FoundationFlow.require(LEVELS.contains(r.getViolationLevel()), "01", "违规等级非法（一般/较重/严重/重大）");
        FoundationFlow.requireText(r.getDescription(), 10, 1000, "违规详细描述", "01");
        FoundationFlow.requireText(r.getPunishStd(), 5, 1000, "对应处罚标准", "01");
        if ("重大".equals(r.getViolationLevel())) {
            r.setVetoFlag(1); // 系统自动携带一票否决
        }
    }

    private String logOf(Redline r, int stage, boolean pass, String opinion) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("stage", stage);
        e.put("pass", pass);
        e.put("opinion", opinion);
        e.put("by", CurrentHeader.operator());
        e.put("roles", CulRole.currentRoles());
        e.put("time", LocalDateTime.now().toString());
        return FoundationFlow.appendLog(r.getApprovalLog(), e);
    }

    private Redline must(Long id) {
        Redline r = mapper.selectById(id);
        FoundationFlow.require(r != null, "01", "红线条款不存在");
        return r;
    }
}
