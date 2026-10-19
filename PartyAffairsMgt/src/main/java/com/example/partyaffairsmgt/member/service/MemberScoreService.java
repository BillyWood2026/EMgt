package com.example.partyaffairsmgt.member.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.member.entity.ScoreFlow;
import com.example.partyaffairsmgt.member.entity.ScoreRule;
import com.example.partyaffairsmgt.member.mapper.ScoreFlowMapper;
import com.example.partyaffairsmgt.member.mapper.ScoreRuleMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 党员积分服务（需求 6.3）。
 * 规则配置、流水管理、排名、年度重置。
 */
@Service
public class MemberScoreService {

    @Autowired
    private ScoreRuleMapper ruleMapper;
    @Autowired
    private ScoreFlowMapper flowMapper;
    @Autowired
    private AuditService auditService;

    /** 积分规则列表 */
    public List<ScoreRule> rules() {
        return ruleMapper.selectList(new QueryWrapper<ScoreRule>().eq("deleted", 0).orderByAsc("id"));
    }

    /** 保存积分规则 */
    @Transactional
    public ScoreRule saveRule(ScoreRule rule) {
        if (rule.getId() == null) {
            rule.setCreator(CurrentHeader.operator());
            rule.setCreateTime(LocalDateTime.now());
            ruleMapper.insert(rule);
        } else {
            rule.setUpdateTime(LocalDateTime.now());
            ruleMapper.updateById(rule);
        }
        auditService.log("积分规则", rule.getId() == null ? "新增" : "编辑", rule.getId(), rule.getRuleName());
        return rule;
    }

    /** 积分流水列表 */
    public List<ScoreFlow> flows(Long memberId, Long orgId, Integer year, Integer month, String ruleKey) {
        QueryWrapper<ScoreFlow> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (memberId != null) qw.eq("member_id", memberId);
        if (orgId != null) qw.eq("org_id", orgId);
        if (year != null) qw.eq("score_year", year);
        if (month != null) qw.eq("score_month", month);
        if (ruleKey != null && !ruleKey.isBlank()) qw.eq("rule_key", ruleKey);
        return flowMapper.selectList(qw.orderByDesc("create_time"));
    }

    /** 手工登记积分 */
    @Transactional
    public ScoreFlow addFlow(ScoreFlow flow) {
        // 同一事项重复加分去重
        if (flow.getSourceType() != null && flow.getSourceId() != null) {
            Long count = flowMapper.selectCount(
                    new QueryWrapper<ScoreFlow>()
                            .eq("source_type", flow.getSourceType())
                            .eq("source_id", flow.getSourceId())
                            .eq("deleted", 0));
            if (count > 0) {
                throw PartyBizException.block("E-MB-10", "同一事项重复加分");
            }
        }
        // 手工加分需审批
        if ("手工".equals(flow.getSourceType())) {
            flow.setAuditStatus("待审批");
        } else {
            flow.setAuditStatus("已批准");
        }
        if (flow.getScoreYear() == null) {
            flow.setScoreYear(LocalDate.now().getYear());
        }
        if (flow.getScoreMonth() == null) {
            flow.setScoreMonth(LocalDate.now().getMonthValue());
        }
        flow.setCreateTime(LocalDateTime.now());
        flowMapper.insert(flow);
        auditService.log("积分流水", "登记", flow.getId(), "党员:" + flow.getMemberName() + ", 分值:" + flow.getScore());
        return flow;
    }

    /** 手工加分审批 */
    @Transactional
    public void auditFlow(Long flowId, boolean pass, String opinion) {
        ScoreFlow flow = flowMapper.selectById(flowId);
        if (flow == null || flow.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-10", "积分流水不存在");
        }
        if (!"待审批".equals(flow.getAuditStatus())) {
            throw PartyBizException.block("E-MB-10", "该流水已审批");
        }
        flow.setAuditStatus(pass ? "已批准" : "已驳回");
        flow.setApprover(CurrentHeader.operator());
        flow.setAuditTime(LocalDateTime.now());
        flow.setUpdateTime(LocalDateTime.now());
        flowMapper.updateById(flow);
        auditService.log("积分流水", pass ? "批准" : "驳回", flowId, opinion);
    }

    /** 积分排名 */
    public Map<String, Object> rank(Long orgId, Integer year, String scope) {
        QueryWrapper<ScoreFlow> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        qw.eq("audit_status", "已批准");
        if (year != null) qw.eq("score_year", year);
        if ("branch".equals(scope) && orgId != null) {
            qw.eq("org_id", orgId);
        }
        List<ScoreFlow> flows = flowMapper.selectList(qw);
        // 按党员汇总
        Map<Long, Integer> memberScores = new HashMap<>();
        Map<Long, String> memberNames = new HashMap<>();
        Map<Long, String> orgNames = new HashMap<>();
        for (ScoreFlow f : flows) {
            memberScores.merge(f.getMemberId(), f.getScore(), Integer::sum);
            memberNames.put(f.getMemberId(), f.getMemberName());
            orgNames.put(f.getMemberId(), f.getOrgName());
        }
        // 排序
        List<Map.Entry<Long, Integer>> sorted = memberScores.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());
        List<Map<String, Object>> rankList = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Long, Integer> e : sorted) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", rank++);
            item.put("memberId", e.getKey());
            item.put("memberName", memberNames.get(e.getKey()));
            item.put("orgName", orgNames.get(e.getKey()));
            item.put("score", e.getValue());
            rankList.add(item);
        }
        // 负分预警
        List<Map<String, Object>> negativeAlerts = rankList.stream()
                .filter(r -> (Integer) r.get("score") < 0)
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("rankList", rankList);
        result.put("negativeAlerts", negativeAlerts);
        result.put("totalMembers", rankList.size());
        return result;
    }

    /** 个人积分明细与汇总 */
    public Map<String, Object> myScore(Long memberId, Integer year) {
        QueryWrapper<ScoreFlow> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        qw.eq("audit_status", "已批准");
        qw.eq("member_id", memberId);
        if (year != null) qw.eq("score_year", year);
        List<ScoreFlow> flows = flowMapper.selectList(qw.orderByDesc("create_time"));
        int total = flows.stream().mapToInt(ScoreFlow::getScore).sum();
        Map<String, Object> result = new HashMap<>();
        result.put("flows", flows);
        result.put("totalScore", total);
        result.put("year", year);
        return result;
    }

    /** 年度重置/结转 */
    @Transactional
    public void resetYear(Integer year, String mode) {
        // mode: reset 清零 / carryover 结转
        QueryWrapper<ScoreFlow> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        qw.eq("score_year", year);
        List<ScoreFlow> flows = flowMapper.selectList(qw);
        if ("reset".equals(mode)) {
            // 逻辑删除旧年度流水（deleted 带 @TableLogic，须走 deleteById）
            for (ScoreFlow f : flows) {
                flowMapper.deleteById(f.getId());
            }
        }
        // 结转模式保留流水，仅记录日志
        auditService.log("积分管理", "年度重置", year, "模式:" + mode + ", 影响流水:" + flows.size());
    }

    /** 自动关联党费缴纳加分（供党费域调用） */
    @Transactional
    public void autoAddDuesScore(Long memberId, String memberName, Long orgId, String orgName, Long paymentId, Integer year, Integer month) {
        ScoreFlow flow = new ScoreFlow();
        flow.setMemberId(memberId);
        flow.setMemberName(memberName);
        flow.setOrgId(orgId);
        flow.setOrgName(orgName);
        flow.setRuleKey("按时交党费");
        flow.setRuleName("按时交纳党费");
        flow.setScore(2);
        flow.setSourceType("缴费");
        flow.setSourceId(paymentId);
        flow.setAuditStatus("已批准");
        flow.setScoreYear(year);
        flow.setScoreMonth(month);
        flow.setRemark("自动关联党费账单");
        flow.setCreateTime(LocalDateTime.now());
        // 检查是否已存在
        Long count = flowMapper.selectCount(
                new QueryWrapper<ScoreFlow>()
                        .eq("source_type", "缴费")
                        .eq("source_id", paymentId)
                        .eq("deleted", 0));
        if (count == 0) {
            flowMapper.insert(flow);
        }
    }
}
