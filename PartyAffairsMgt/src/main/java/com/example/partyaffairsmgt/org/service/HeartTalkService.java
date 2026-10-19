package com.example.partyaffairsmgt.org.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.member.entity.PartyMember;
import com.example.partyaffairsmgt.member.mapper.PartyMemberMapper;
import com.example.partyaffairsmgt.org.entity.HeartTalk;
import com.example.partyaffairsmgt.org.entity.PartyOrg;
import com.example.partyaffairsmgt.org.entity.TalkCoverage;
import com.example.partyaffairsmgt.org.entity.TalkPlan;
import com.example.partyaffairsmgt.org.mapper.HeartTalkMapper;
import com.example.partyaffairsmgt.org.mapper.PartyOrgMapper;
import com.example.partyaffairsmgt.org.mapper.TalkCoverageMapper;
import com.example.partyaffairsmgt.org.mapper.TalkPlanMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 4.5 谈心谈话：计划规则、应谈名单生成、覆盖统计、重复对子预警、问题跟进。 */
@Service
public class HeartTalkService {

    @Autowired private HeartTalkMapper talkMapper;
    @Autowired private TalkPlanMapper planMapper;
    @Autowired private TalkCoverageMapper coverageMapper;
    @Autowired private PartyMemberMapper memberMapper;
    @Autowired private PartyOrgMapper orgMapper;
    @Autowired private AuditService auditService;

    public List<HeartTalk> list(Long orgId, Integer year, String followStatus, String keyword) {
        QueryWrapper<HeartTalk> qw = new QueryWrapper<>();
        if (orgId != null) {
            qw.eq("org_id", orgId);
        }
        if (year != null) {
            qw.apply("year(talk_date)={0}", year);
        }
        if (followStatus != null && !followStatus.isBlank()) {
            qw.eq("follow_status", followStatus);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("talker_name", keyword).or().like("interviewee_name", keyword)
                    .or().like("topic", keyword));
        }
        qw.orderByDesc("talk_date", "id");
        return talkMapper.selectList(qw);
    }

    @Transactional
    public HeartTalk save(HeartTalk t) {
        validate(t);
        boolean duplicate = t.getId() == null && t.getTalkDate() != null
                && talkMapper.selectCount(new QueryWrapper<HeartTalk>()
                .eq("org_id", t.getOrgId())
                .eq("talker_name", t.getTalkerName())
                .eq("interviewee_name", t.getIntervieweeName())
                .apply("year(talk_date)={0}", t.getTalkDate().getYear())) > 0;
        LocalDateTime now = LocalDateTime.now();
        if (t.getId() == null) {
            if (t.getStatus() == null) {
                t.setStatus("正常");
            }
            if (t.getFollowStatus() == null && t.getIssueToSolve() != null && !t.getIssueToSolve().isBlank()) {
                t.setFollowStatus("跟进中");
            }
            t.setCreateTime(now);
            t.setUpdateTime(now);
            talkMapper.insert(t);
        } else {
            t.setUpdateTime(now);
            talkMapper.updateById(t);
        }
        markCovered(t);
        if (duplicate) {
            // 重复谈话对子：预警不阻断
            auditService.log("TALK_DUPLICATE_WARN", "heart_talk", t.getId(),
                    t.getTalkerName() + " 与 " + t.getIntervieweeName() + " 本年重复谈话对子（预警）");
        }
        return t;
    }

    private void validate(HeartTalk t) {
        if (t.getTalkerName() == null || t.getTalkerName().isBlank()
                || t.getIntervieweeName() == null || t.getIntervieweeName().isBlank()) {
            throw PartyBizException.block("E-TALK-01", "谈话人和谈话对象必填");
        }
        if (t.getTalkDate() == null) {
            throw PartyBizException.block("E-TALK-01", "谈话日期必填");
        }
        if (t.getTalkMode() != null && !Arrays.asList("个别谈话", "集体谈话").contains(t.getTalkMode())) {
            throw PartyBizException.block("E-TALK-01", "谈话方式必须为个别谈话/集体谈话");
        }
        if (t.getFollowStatus() != null && t.getIssueToSolve() == null && "已解决".equals(t.getFollowStatus())) {
            throw PartyBizException.block("E-TALK-02", "标记问题已解决前必须登记问题与跟进情况");
        }
    }

    /** 谈话完成后自动回写应谈名单的覆盖状态。 */
    private void markCovered(HeartTalk t) {
        if (t.getOrgId() == null || t.getTalkDate() == null) {
            return;
        }
        TalkCoverage c = coverageMapper.selectOne(new QueryWrapper<TalkCoverage>()
                .eq("org_id", t.getOrgId())
                .eq("plan_year", t.getTalkDate().getYear())
                .eq("interviewee_name", t.getIntervieweeName())
                .last("limit 1"));
        if (c != null && (c.getCovered() == null || c.getCovered() == 0)) {
            c.setCovered(1);
            c.setCoverDate(t.getTalkDate());
            c.setHeartTalkId(t.getId());
            c.setUpdateTime(LocalDateTime.now());
            coverageMapper.updateById(c);
        }
    }

    // ---------- 计划 ----------

    public List<TalkPlan> plans(Long orgId, Integer year) {
        QueryWrapper<TalkPlan> qw = new QueryWrapper<>();
        if (orgId != null) {
            qw.eq("org_id", orgId);
        }
        if (year != null) {
            qw.eq("plan_year", year);
        }
        qw.orderByDesc("plan_year", "id");
        return planMapper.selectList(qw);
    }

    @Transactional
    public TalkPlan savePlan(TalkPlan p) {
        if (p.getOrgId() == null || p.getPlanYear() == null) {
            throw PartyBizException.block("E-TALK-01", "计划组织和年度必填");
        }
        if (p.getLeaderRule() == null) {
            p.setLeaderRule("党组织书记与班子成员每年至少谈心谈话1次");
        }
        if (p.getCommitteeRule() == null) {
            p.setCommitteeRule("班子成员之间、班子成员与党员每年至少谈心谈话1次");
        }
        if (p.getStatus() == null) {
            p.setStatus("生效中");
        }
        LocalDateTime now = LocalDateTime.now();
        if (p.getId() == null) {
            p.setCreateTime(now);
            p.setUpdateTime(now);
            planMapper.insert(p);
        } else {
            p.setUpdateTime(now);
            planMapper.updateById(p);
        }
        return p;
    }

    /**
     * 生成应谈名单：
     * 领导班子成员规则——书记与每位班子成员；
     * 支部委员规则——书记与每位普通党员（班子成员已含上行）。
     */
    @Transactional
    public List<TalkCoverage> generateCoverage(Long planId) {
        TalkPlan plan = mustPlan(planId);
        PartyOrg org = orgMapper.selectById(plan.getOrgId());
        if (org == null) {
            throw PartyBizException.block("E-TALK-01", "组织不存在");
        }
        List<PartyMember> members = memberMapper.selectList(new QueryWrapper<PartyMember>()
                .eq("org_id", plan.getOrgId()).orderByAsc("id"));
        List<String> committee = parseNames(org.getCommitteeMembers());
        String secretary = org.getSecretary() == null || org.getSecretary().isBlank()
                ? (committee.isEmpty() ? null : committee.get(0)) : org.getSecretary();

        coverageMapper.delete(new QueryWrapper<TalkCoverage>().eq("plan_id", planId));
        List<TalkCoverage> rows = new ArrayList<>();
        LocalDate due = LocalDate.of(plan.getPlanYear(), 12, 31);
        for (PartyMember m : members) {
            boolean isCommittee = committee.contains(m.getName());
            String talker = secretary == null ? "" : secretary;
            String rule = isCommittee ? "领导班子成员规则" : "支部委员规则";
            TalkCoverage c = new TalkCoverage();
            c.setPlanId(planId);
            c.setOrgId(plan.getOrgId());
            c.setPlanYear(plan.getPlanYear());
            c.setTalkerName(talker);
            c.setIntervieweeId(m.getId());
            c.setIntervieweeName(m.getName());
            c.setRequiredRule(rule);
            c.setDueDate(due);
            c.setCovered(0);
            c.setCreateTime(LocalDateTime.now());
            c.setUpdateTime(c.getCreateTime());
            coverageMapper.insert(c);
            rows.add(c);
        }
        auditService.log("TALK_COVERAGE_GEN", "talk_plan", planId,
                "生成应谈名单" + rows.size() + "人");
        return rows;
    }

    public List<TalkCoverage> coverage(Long planId) {
        List<TalkCoverage> list = coverageMapper.selectList(new QueryWrapper<TalkCoverage>()
                .eq("plan_id", planId).orderByAsc("covered", "id"));
        // 兜底：若谈话记录已登记则补覆盖
        list.forEach(c -> {
            if (c.getCovered() == null || c.getCovered() == 0) {
                HeartTalk hit = talkMapper.selectOne(new QueryWrapper<HeartTalk>()
                        .eq("org_id", c.getOrgId())
                        .eq("interviewee_name", c.getIntervieweeName())
                        .apply("year(talk_date)={0}", c.getPlanYear())
                        .last("limit 1"));
                if (hit != null) {
                    c.setCovered(1);
                    c.setCoverDate(hit.getTalkDate());
                    c.setHeartTalkId(hit.getId());
                    coverageMapper.updateById(c);
                }
            }
        });
        return list;
    }

    /** 覆盖率/未覆盖/逾期统计；年末未覆盖纳入考核预警。 */
    public Map<String, Object> coverageStats(Long planId) {
        List<TalkCoverage> all = coverage(planId);
        long covered = all.stream().filter(c -> c.getCovered() != null && c.getCovered() == 1).count();
        List<String> uncovered = all.stream()
                .filter(c -> c.getCovered() == null || c.getCovered() == 0)
                .map(TalkCoverage::getIntervieweeName).collect(Collectors.toList());
        LocalDate today = LocalDate.now();
        boolean yearEnd = today.getMonthValue() >= 11;
        List<String> overdue = all.stream()
                .filter(c -> (c.getCovered() == null || c.getCovered() == 0)
                        && c.getDueDate() != null && today.isAfter(c.getDueDate()))
                .map(TalkCoverage::getIntervieweeName).collect(Collectors.toList());
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("total", all.size());
        r.put("covered", covered);
        r.put("coverageRate", all.isEmpty() ? 100.0
                : Math.round(covered * 1000.0 / all.size()) / 10.0);
        r.put("uncovered", uncovered);
        r.put("overdue", overdue);
        r.put("assessmentWarn", yearEnd && !uncovered.isEmpty()
                ? "年末仍有" + uncovered.size() + "人未覆盖，纳入年度考核预警" : null);
        return r;
    }

    /** 问题未解决跟进清单。 */
    public List<HeartTalk> unresolvedIssues() {
        return talkMapper.selectList(new QueryWrapper<HeartTalk>()
                .isNotNull("issue_to_solve").ne("follow_status", "已解决")
                .orderByAsc("talk_date"));
    }

    private List<String> parseNames(String s) {
        if (s == null || s.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(s.split("[,，、;；/\\s]+"))
                .filter(x -> !x.isBlank()).map(String::trim).collect(Collectors.toList());
    }

    private TalkPlan mustPlan(Long id) {
        TalkPlan p = planMapper.selectById(id);
        if (p == null) {
            throw PartyBizException.block("E-TALK-01", "谈心谈话计划不存在：" + id);
        }
        return p;
    }
}
