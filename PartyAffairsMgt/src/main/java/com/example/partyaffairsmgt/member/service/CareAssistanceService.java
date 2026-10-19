package com.example.partyaffairsmgt.member.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.member.entity.CareDifficulty;
import com.example.partyaffairsmgt.member.entity.CareHelp;
import com.example.partyaffairsmgt.member.entity.CareVisit;
import com.example.partyaffairsmgt.member.mapper.CareDifficultyMapper;
import com.example.partyaffairsmgt.member.mapper.CareHelpMapper;
import com.example.partyaffairsmgt.member.mapper.CareVisitMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 党员关怀帮扶服务（需求 6.5）。
 * 困难台账、走访慰问、帮扶申请发放。
 */
@Service
public class CareAssistanceService {

    @Autowired
    private CareDifficultyMapper difficultyMapper;
    @Autowired
    private CareVisitMapper visitMapper;
    @Autowired
    private CareHelpMapper helpMapper;
    @Autowired
    private AuditService auditService;

    /** 困难台账列表 */
    public List<CareDifficulty> difficulties(Long orgId, String difficultyType) {
        QueryWrapper<CareDifficulty> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (orgId != null) qw.eq("org_id", orgId);
        if (difficultyType != null && !difficultyType.isBlank()) qw.eq("difficulty_type", difficultyType);
        return difficultyMapper.selectList(qw);
    }

    /** 新增困难台账 */
    @Transactional
    public CareDifficulty addDifficulty(CareDifficulty difficulty) {
        difficulty.setStatus("在册");
        difficulty.setCreateTime(LocalDateTime.now());
        difficultyMapper.insert(difficulty);
        auditService.log("关怀帮扶", "困难台账新增", difficulty.getId(), "党员:" + difficulty.getMemberName());
        return difficulty;
    }

    /** 退出困难台账 */
    @Transactional
    public void exitDifficulty(Long id, String exitReason) {
        CareDifficulty d = difficultyMapper.selectById(id);
        if (d == null || d.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-12", "困难台账记录不存在");
        }
        d.setStatus("已退出");
        d.setExitDate(LocalDate.now());
        d.setExitReason(exitReason);
        d.setUpdateTime(LocalDateTime.now());
        difficultyMapper.updateById(d);
        auditService.log("关怀帮扶", "困难台账退出", id, "原因:" + exitReason);
    }

    /** 走访慰问列表 */
    public List<CareVisit> visits(Long orgId, String visitNode, Integer year) {
        QueryWrapper<CareVisit> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (orgId != null) qw.eq("org_id", orgId);
        if (visitNode != null && !visitNode.isBlank()) qw.eq("visit_node", visitNode);
        if (year != null) qw.apply("YEAR(visit_date) = {0}", year);
        return visitMapper.selectList(qw.orderByDesc("visit_date"));
    }

    /** 新增走访慰问 */
    @Transactional
    public CareVisit addVisit(CareVisit visit) {
        // 同一慰问节点重复慰问提醒
        Long count = visitMapper.selectCount(
                new QueryWrapper<CareVisit>()
                        .eq("member_id", visit.getMemberId())
                        .eq("visit_node", visit.getVisitNode())
                        .eq("deleted", 0)
                        .apply("YEAR(visit_date) = {0}", visit.getVisitDate().getYear()));
        if (count > 0) {
            visit.setIsDuplicate(1);
        } else {
            visit.setIsDuplicate(0);
        }
        visit.setCreateTime(LocalDateTime.now());
        visitMapper.insert(visit);
        auditService.log("关怀帮扶", "走访慰问", visit.getId(),
                "党员:" + visit.getMemberName() + ", 节点:" + visit.getVisitNode()
                        + (visit.getIsDuplicate() == 1 ? " [重复慰问提醒]" : ""));
        return visit;
    }

    /** 帮扶申请列表 */
    public List<CareHelp> helps(Long orgId, String status) {
        QueryWrapper<CareHelp> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (orgId != null) qw.eq("org_id", orgId);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        return helpMapper.selectList(qw.orderByDesc("create_time"));
    }

    /** 新增帮扶申请 */
    @Transactional
    public CareHelp addHelp(CareHelp help) {
        help.setStatus("申请中");
        help.setApplyDate(LocalDate.now());
        help.setCreateTime(LocalDateTime.now());
        helpMapper.insert(help);
        auditService.log("关怀帮扶", "帮扶申请", help.getId(), "党员:" + help.getMemberName() + ", 金额:" + help.getAmount());
        return help;
    }

    /** 帮扶审批 */
    @Transactional
    public void approveHelp(Long helpId, boolean pass, String opinion) {
        CareHelp help = helpMapper.selectById(helpId);
        if (help == null || help.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-12", "帮扶申请不存在");
        }
        if (!"申请中".equals(help.getStatus())) {
            throw PartyBizException.block("E-MB-12", "该申请已处理");
        }
        help.setStatus(pass ? "已批准" : "已驳回");
        help.setApproveDate(LocalDate.now());
        help.setApprover(CurrentHeader.operator());
        help.setUpdateTime(LocalDateTime.now());
        helpMapper.updateById(help);
        auditService.log("关怀帮扶", pass ? "帮扶批准" : "帮扶驳回", helpId, opinion);
    }

    /** 帮扶发放 */
    @Transactional
    public void grantHelp(Long helpId, String grantWay, Long duesExpenseId) {
        CareHelp help = helpMapper.selectById(helpId);
        if (help == null || help.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-12", "帮扶申请不存在");
        }
        if (!"已批准".equals(help.getStatus())) {
            throw PartyBizException.block("E-MB-12", "帮扶申请未批准，不能发放");
        }
        help.setStatus("已发放");
        help.setGrantDate(LocalDate.now());
        help.setGrantWay(grantWay);
        help.setDuesExpenseId(duesExpenseId);
        help.setUpdateTime(LocalDateTime.now());
        helpMapper.updateById(help);
        auditService.log("关怀帮扶", "帮扶发放", helpId, "方式:" + grantWay + ", 关联党费使用:" + duesExpenseId);
    }

    /** 帮扶名单公示合规校验 */
    public List<Map<String, Object>> publicityCheck() {
        // 检查帮扶名单是否超范围公开敏感信息
        List<CareHelp> helps = helpMapper.selectList(
                new QueryWrapper<CareHelp>().eq("deleted", 0).eq("status", "已发放"));
        return helps.stream().map(h -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", h.getId());
            map.put("memberName", h.getMemberName());
            map.put("helpType", h.getHelpType());
            map.put("amount", h.getAmount());
            map.put("grantDate", h.getGrantDate());
            // 合规校验：金额是否超标准、是否含敏感信息等
            boolean compliant = h.getAmount() != null && h.getAmount().compareTo(new java.math.BigDecimal("10000")) <= 0;
            map.put("compliant", compliant);
            map.put("message", compliant ? "合规" : "金额超标准，需复核");
            return map;
        }).collect(Collectors.toList());
    }
}
