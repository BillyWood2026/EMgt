package com.example.partyaffairsmgt.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.activity.entity.ActivityPlan;
import com.example.partyaffairsmgt.activity.mapper.ActivityPlanMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 活动计划服务（需求 9.1）。
 * 频次合规：党员大会季度≥1、支委会/党小组会月≥1、党课季度≥1、主题党日月≥1（参数化）；
 * 同组织同日三会冲突提示；上级组织部门审批；审批通过后发布。
 * 错误码：E-AC-01 计划频次不足；E-AC-02 同日冲突；E-AC-03 非待审批不可审批。
 */
@Service
public class ActivityPlanService {

    /** 三会类型（纳入四本台账与频次校验） */
    public static final List<String> LEDGER_TYPES =
            List.of("支部党员大会", "支部委员会", "党小组会", "党课");

    @Autowired
    private ActivityPlanMapper planMapper;
    @Autowired
    private AuditService auditService;

    public List<ActivityPlan> list(Long orgId, String activityType, String approveStatus,
                                   String publishStatus, Integer year) {
        QueryWrapper<ActivityPlan> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (orgId != null) qw.eq("org_id", orgId);
        if (activityType != null && !activityType.isBlank()) qw.eq("activity_type", activityType);
        if (approveStatus != null && !approveStatus.isBlank()) qw.eq("approve_status", approveStatus);
        if (publishStatus != null && !publishStatus.isBlank()) qw.eq("publish_status", publishStatus);
        if (year != null) qw.eq("period_year", year);
        qw.orderByAsc("plan_date");
        return planMapper.selectList(qw);
    }

    public ActivityPlan detail(Long id) {
        ActivityPlan p = planMapper.selectById(id);
        if (p == null || p.getDeleted() == 1) {
            throw PartyBizException.block("E-AC-03", "活动计划不存在");
        }
        return p;
    }

    /** 编制计划（支部书记/党务经办）。同组织同日三会自动打冲突标记（提示，不阻断）。 */
    @Transactional
    public ActivityPlan save(ActivityPlan p) {
        if (p.getTitle() == null || p.getTitle().isBlank()) {
            throw PartyBizException.block("E-AC-03", "活动主题必填");
        }
        if (p.getActivityType() == null || p.getPlanDate() == null) {
            throw PartyBizException.block("E-AC-03", "活动类型与计划时间必填");
        }
        if (p.getHost() == null || p.getHost().isBlank()) {
            throw PartyBizException.block("E-AC-03", "主持人必填");
        }
        if (p.getParticipantScope() == null || p.getParticipantScope().isBlank()) {
            throw PartyBizException.block("E-AC-03", "参加范围必填");
        }
        LocalDate d = p.getPlanDate();
        if (p.getPeriodYear() == null) p.setPeriodYear(d.getYear());
        if (p.getPeriodQuarter() == null) p.setPeriodQuarter((d.getMonthValue() - 1) / 3 + 1);
        if (p.getPeriodMonth() == null) p.setPeriodMonth(d.getMonthValue());
        if (p.getInLedger() == null) p.setInLedger(1);
        if (p.getApproveStatus() == null) p.setApproveStatus("待审批");
        if (p.getStatus() == null) p.setStatus("草稿");
        if (p.getPublishStatus() == null) p.setPublishStatus("未发布");
        if (p.getPlanner() == null) p.setPlanner(CurrentHeader.operator());

        // 同组织同一天重复安排三会 → 冲突标记
        Long conflict = planMapper.selectCount(new QueryWrapper<ActivityPlan>()
                .eq("deleted", 0).eq("org_id", p.getOrgId())
                .in("activity_type", LEDGER_TYPES).eq("plan_date", d)
                .ne(p.getId() == null ? "id" : "id", p.getId() == null ? -1 : p.getId()));
        p.setConflictFlag(conflict != null && conflict > 0 ? 1 : 0);

        p.setUpdateTime(LocalDateTime.now());
        if (p.getId() == null) {
            p.setCreateTime(LocalDateTime.now());
            planMapper.insert(p);
        } else {
            planMapper.updateById(p);
        }
        auditService.log("三会一课", "编制计划", p.getId(), p.getTitle());
        return p;
    }

    /** 时间冲突检查（同组织同日重复安排三会），有冲突抛 WARN E-AC-02。 */
    public List<ActivityPlan> checkConflict(Long orgId, LocalDate date) {
        List<ActivityPlan> dup = planMapper.selectList(new QueryWrapper<ActivityPlan>()
                .eq("deleted", 0).eq("org_id", orgId).eq("plan_date", date)
                .in("activity_type", LEDGER_TYPES));
        if (dup.size() > 1) {
            throw new PartyBizException("E-AC-02", PartyBizException.WARN,
                    orgId + "在" + date + "重复安排" + dup.size() + "个三会活动，请核实合并或调整时间");
        }
        return dup;
    }

    /**
     * 频次合规校验（按组织×季度统计，低于下限返回缺失明细并预警 E-AC-01）。
     * 年度应召开：党员大会4、支委会12、党小组会12、党课4；主题党日12。
     */
    public Map<String, Object> checkFrequency(Long orgId, Integer year, Integer quarter) {
        List<Map<String, Object>> missing = new ArrayList<>();
        List<ActivityPlan> plans = planMapper.selectList(new QueryWrapper<ActivityPlan>()
                .eq("deleted", 0).eq("org_id", orgId).eq("period_year", year)
                .ne("approve_status", "退回"));
        for (int m = (quarter - 1) * 3 + 1; m <= quarter * 3; m++) {
            checkOne(plans, "支部委员会", m, "month", "支委会每月至少1次", missing);
            checkOne(plans, "党小组会", m, "month", "党小组会每月至少1次", missing);
            checkOne(plans, "主题党日", m, "month", "主题党日每月相对固定1天", missing);
        }
        long assembly = plans.stream().filter(x -> "支部党员大会".equals(x.getActivityType())
                && quarter.equals(x.getPeriodQuarter())).count();
        if (assembly < 1) {
            missing.add(item("支部党员大会", quarter + "季度", 1, (int) assembly, "支部党员大会每季度至少1次"));
        }
        long lecture = plans.stream().filter(x -> "党课".equals(x.getActivityType())
                && quarter.equals(x.getPeriodQuarter())).count();
        if (lecture < 1) {
            missing.add(item("党课", quarter + "季度", 1, (int) lecture, "党课每季度至少安排1次"));
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("orgId", orgId);
        r.put("year", year);
        r.put("quarter", quarter);
        r.put("missing", missing);
        r.put("passed", missing.isEmpty());
        if (!missing.isEmpty()) {
            throw new PartyBizException("E-AC-01", PartyBizException.WARN,
                    year + "年第" + quarter + "季度三会一课计划不足，缺失" + missing.size() + "项，请补齐");
        }
        return r;
    }

    private void checkOne(List<ActivityPlan> plans, String type, int month, String period,
                          String rule, List<Map<String, Object>> missing) {
        long n = plans.stream().filter(x -> type.equals(x.getActivityType())
                && month == (x.getPeriodMonth() == null ? 0 : x.getPeriodMonth())).count();
        if (n < 1) {
            missing.add(item(type, month + "月", 1, (int) n, rule));
        }
    }

    private Map<String, Object> item(String type, String period, int required, int actual, String rule) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("activityType", type);
        m.put("period", period);
        m.put("required", required);
        m.put("actual", actual);
        m.put("rule", rule);
        return m;
    }

    /** 上级党委组织部门审批/备案（E-AC-03） */
    @Transactional
    public ActivityPlan approve(Long id, boolean passed, String opinion) {
        ActivityPlan p = detail(id);
        if (!"待审批".equals(p.getApproveStatus())) {
            throw PartyBizException.block("E-AC-03", "仅待审批计划可审批，当前为[" + p.getApproveStatus() + "]");
        }
        p.setApproveStatus(passed ? "通过" : "退回");
        p.setApprover(CurrentHeader.operator());
        p.setApproveTime(LocalDateTime.now());
        p.setApproveOpinion(opinion);
        p.setStatus(passed ? "已审批" : "草稿");
        p.setUpdateTime(LocalDateTime.now());
        planMapper.updateById(p);
        auditService.log("三会一课", passed ? "审批通过" : "审批退回", id, p.getTitle());
        return p;
    }

    /** 发布（审批通过后；支持定时发布） */
    @Transactional
    public ActivityPlan publish(Long id, LocalDateTime scheduled) {
        ActivityPlan p = detail(id);
        if (!"通过".equals(p.getApproveStatus())) {
            throw PartyBizException.block("E-AC-03", "计划未通过审批，不可发布");
        }
        if (p.getExpectCount() == null) {
            throw PartyBizException.block("E-AC-03", "发布前须填写应参加人数");
        }
        p.setPublishStatus("已发布");
        p.setScheduledPublish(scheduled);
        p.setPublishTime(LocalDateTime.now());
        if (p.getEnrollDeadline() == null && p.getPlanDate() != null) {
            p.setEnrollDeadline(p.getPlanDate().atStartOfDay().minusHours(6));
        }
        p.setUpdateTime(LocalDateTime.now());
        planMapper.updateById(p);
        auditService.log("三会一课", "发布活动", id, p.getTitle());
        return p;
    }

    @Transactional
    public void delete(Long id) {
        planMapper.deleteById(id);
    }
}
