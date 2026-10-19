package com.example.partyaffairsmgt.membership.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.membership.entity.DuesExpense;
import com.example.partyaffairsmgt.membership.entity.DuesPayment;
import com.example.partyaffairsmgt.membership.entity.DuesPublication;
import com.example.partyaffairsmgt.membership.entity.DuesRemittance;
import com.example.partyaffairsmgt.membership.entity.DuesUsePlan;
import com.example.partyaffairsmgt.membership.mapper.DuesExpenseMapper;
import com.example.partyaffairsmgt.membership.mapper.DuesPaymentMapper;
import com.example.partyaffairsmgt.membership.mapper.DuesPublicationMapper;
import com.example.partyaffairsmgt.membership.mapper.DuesRemittanceMapper;
import com.example.partyaffairsmgt.membership.mapper.DuesUsePlanMapper;
import java.math.BigDecimal;
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
 * 党费使用管理（需求 5.4）。
 * 范围仅限五类；必须集体讨论（纪要必传）；审批后方可支出登记；收支平衡（使用不得超收）。
 */
@Service
public class DuesExpenseService {

    public static final List<String> USAGE_CATEGORIES = List.of(
            "培训党员",
            "订阅购买党员教育报刊资料音像制品和设备",
            "表彰先进基层党组织优秀党员优秀党务工作者",
            "补助生活困难党员",
            "补助遭受严重自然灾害党员及修缮因灾受损教育设施");

    @Autowired private DuesExpenseMapper expenseMapper;
    @Autowired private DuesUsePlanMapper planMapper;
    @Autowired private DuesPaymentMapper paymentMapper;
    @Autowired private DuesRemittanceMapper remittanceMapper;
    @Autowired private DuesPublicationMapper publicationMapper;
    @Autowired private AuditService auditService;

    // ==================== 年度计划 ====================

    public List<DuesUsePlan> planList(Long orgId, Integer year) {
        QueryWrapper<DuesUsePlan> qw = new QueryWrapper<>();
        if (orgId != null) qw.eq("org_id", orgId);
        if (year != null) qw.eq("plan_year", year);
        qw.orderByDesc("plan_year");
        return planMapper.selectList(qw);
    }

    @Transactional
    public DuesUsePlan savePlan(DuesUsePlan body) {
        if (body.getOrgId() == null || body.getPlanYear() == null) {
            throw PartyBizException.block("E-UE-05", "组织与计划年度必填");
        }
        DuesUsePlan existing = planMapper.selectOne(new QueryWrapper<DuesUsePlan>()
                .eq("org_id", body.getOrgId()).eq("plan_year", body.getPlanYear()).last("limit 1"));
        if (body.getId() == null && existing != null) {
            body.setId(existing.getId());
        }
        body.setCreator(CurrentHeader.operator());
        body.setUpdateTime(LocalDateTime.now());
        if (body.getId() == null) {
            body.setCreateTime(LocalDateTime.now());
            planMapper.insert(body);
        } else {
            planMapper.updateById(body);
        }
        auditService.log("党费使用计划", "DuesUsePlan", body.getId(),
                body.getOrgName() + " " + body.getPlanYear() + "年 计划" + body.getPlannedAmount());
        return body;
    }

    // ==================== 使用单 ====================

    public List<DuesExpense> list(Long orgId, Integer year, String auditStatus) {
        QueryWrapper<DuesExpense> qw = new QueryWrapper<>();
        if (orgId != null) qw.eq("org_id", orgId);
        if (auditStatus != null && !auditStatus.isBlank()) qw.eq("audit_status", auditStatus);
        if (year != null) qw.apply("YEAR(expense_date) = {0}", year);
        qw.orderByDesc("create_time");
        return expenseMapper.selectList(qw);
    }

    /** 新建使用申请：五类范围校验 + 纪要必传 + 预算预警（不阻断）。 */
    @Transactional
    public Map<String, Object> create(DuesExpense body) {
        validate(body);
        List<String> warnings = new ArrayList<>();
        body.setId(null);
        body.setAuditStatus("草稿");
        body.setCreateTime(LocalDateTime.now());
        body.setUpdateTime(LocalDateTime.now());
        expenseMapper.insert(body);

        if (body.getPlanId() != null) {
            DuesUsePlan plan = planMapper.selectById(body.getPlanId());
            if (plan != null && plan.getPlannedAmount() != null) {
                BigDecimal used = sumPlannedExpense(body.getPlanId(), body.getId());
                if (used.compareTo(plan.getPlannedAmount()) > 0) {
                    warnings.add("E-UE-03：该计划累计支出(" + used + ")已超出年度计划额度("
                            + plan.getPlannedAmount() + ")，请履行预算调整程序");
                }
            }
        }
        auditService.log("党费使用申请", "DuesExpense", body.getId(),
                body.getOrgName() + " " + body.getUsageType() + " " + body.getAmount() + "元");
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("expense", body);
        r.put("warnings", warnings);
        return r;
    }

    private void validate(DuesExpense body) {
        if (body.getUsageType() == null || !USAGE_CATEGORIES.contains(body.getUsageType())) {
            throw PartyBizException.block("E-UE-01",
                    "党费使用超范围：仅限五类规定用途（培训党员/订阅教育资料设备/表彰先进/补助困难党员/救灾及设施修缮）");
        }
        if (body.getAmount() == null || body.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw PartyBizException.block("E-UE-01", "支出金额必填且大于0");
        }
        if (body.getDiscussionAttachment() == null || body.getDiscussionAttachment().isBlank()) {
            throw PartyBizException.block("E-UE-02", "使用党费必须经支委会/党委会集体讨论，须上传会议纪要附件");
        }
        if (body.getPurpose() == null || body.getPurpose().isBlank()) {
            throw PartyBizException.block("E-UE-01", "具体用途说明必填");
        }
        if (body.getOrgId() == null) {
            throw PartyBizException.block("E-UE-01", "请选择党组织");
        }
    }

    private BigDecimal sumPlannedExpense(Long planId, Long excludeId) {
        List<DuesExpense> list = expenseMapper.selectList(new QueryWrapper<DuesExpense>()
                .eq("plan_id", planId).ne(excludeId != null, "id", excludeId));
        return list.stream().map(DuesExpense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(BigDecimal.ZERO);
    }

    @Transactional
    public DuesExpense audit(Long id, boolean pass, String opinion) {
        DuesExpense e = expenseMapper.selectById(id);
        if (e == null) throw PartyBizException.block("E-UE-06", "使用单不存在");
        if (!"草稿".equals(e.getAuditStatus()) && !"待审批".equals(e.getAuditStatus())) {
            throw PartyBizException.block("E-UE-06", "该使用单已审批");
        }
        e.setAuditStatus(pass ? "已批准" : "草稿");
        e.setApprover(CurrentHeader.operator());
        if (opinion != null) e.setPurpose(e.getPurpose() + "（审批意见：" + opinion + "）");
        e.setUpdateTime(LocalDateTime.now());
        expenseMapper.updateById(e);
        auditService.log(pass ? "党费使用批准" : "党费使用驳回", "DuesExpense", id,
                e.getUsageType() + " " + e.getAmount());
        return e;
    }

    /** 财务支出登记：收支平衡校验（使用不得超收，E-UE-04）。 */
    @Transactional
    public DuesExpense register(Long id, LocalDate expenseDate, String result) {
        DuesExpense e = expenseMapper.selectById(id);
        if (e == null) throw PartyBizException.block("E-UE-06", "使用单不存在");
        if (!"已批准".equals(e.getAuditStatus())) {
            throw PartyBizException.block("E-UE-06", "须经规定权限审批通过后方可支出登记");
        }
        Map<String, Object> summary = yearSummary(e.getOrgId(),
                (expenseDate != null ? expenseDate : LocalDate.now()).getYear());
        BigDecimal balance = new BigDecimal(summary.get("availableBalance").toString());
        if (e.getAmount().compareTo(balance) > 0) {
            throw PartyBizException.block("E-UE-04",
                    "收支平衡校验失败：当年可用余额(" + balance + ")不足以支付本笔(" + e.getAmount() + ")");
        }
        e.setExpenseDate(expenseDate != null ? expenseDate : LocalDate.now());
        e.setHandler(CurrentHeader.operator());
        e.setExecuteResult(result);
        e.setAuditStatus("已登记");
        e.setStatus("已登记");
        e.setUpdateTime(LocalDateTime.now());
        expenseMapper.updateById(e);
        auditService.log("党费支出登记", "DuesExpense", id, e.getUsageType() + " " + e.getAmount());
        return e;
    }

    /** 年度收支汇总：实交/已上缴/已支出/可用余额。 */
    public Map<String, Object> yearSummary(Long orgId, Integer year) {
        int y = year == null ? java.time.LocalDate.now().getYear() : year;
        BigDecimal collected = paymentMapper.selectList(new QueryWrapper<DuesPayment>()
                        .eq(orgId != null, "org_id", orgId).eq("fee_year", y).eq("status", "已缴"))
                .stream().map(p -> p.getPaidAmount() == null ? BigDecimal.ZERO : p.getPaidAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remitted = remittanceMapper.selectList(new QueryWrapper<DuesRemittance>()
                        .eq(orgId != null, "org_id", orgId).eq("fee_year", y))
                .stream().map(r -> r.getAmount() == null ? BigDecimal.ZERO : r.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal used = expenseMapper.selectList(new QueryWrapper<DuesExpense>()
                        .eq(orgId != null, "org_id", orgId).in("audit_status", "已登记", "已核销")
                        .apply("YEAR(expense_date) = {0}", y))
                .stream().map(x -> x.getAmount() == null ? BigDecimal.ZERO : x.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("orgId", orgId);
        r.put("year", y);
        r.put("collected", collected);
        r.put("remitted", remitted);
        r.put("used", used);
        r.put("availableBalance", collected.subtract(remitted).subtract(used));
        return r;
    }

    public List<String> categories() {
        return USAGE_CATEGORIES;
    }
}
