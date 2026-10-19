package com.example.partyaffairsmgt.membership.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.member.entity.PartyMember;
import com.example.partyaffairsmgt.member.mapper.PartyMemberMapper;
import com.example.partyaffairsmgt.membership.entity.DuesAccount;
import com.example.partyaffairsmgt.membership.entity.DuesBackpay;
import com.example.partyaffairsmgt.membership.entity.DuesBase;
import com.example.partyaffairsmgt.membership.entity.DuesPayment;
import com.example.partyaffairsmgt.membership.entity.DuesReduce;
import com.example.partyaffairsmgt.membership.entity.DuesRemittance;
import com.example.partyaffairsmgt.membership.entity.MembershipChange;
import com.example.partyaffairsmgt.membership.mapper.DuesAccountMapper;
import com.example.partyaffairsmgt.membership.mapper.DuesBackpayMapper;
import com.example.partyaffairsmgt.membership.mapper.DuesBaseMapper;
import com.example.partyaffairsmgt.membership.mapper.DuesPaymentMapper;
import com.example.partyaffairsmgt.membership.mapper.DuesReduceMapper;
import com.example.partyaffairsmgt.membership.mapper.DuesRemittanceMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 党费收缴服务（需求 5.3）。
 * 比例分段：月薪/年薪/个体 0-3000=0.5%，3000-5000=1%，5000-10000=1.5%，10000+=2%；
 * 离退休 ≤5000=0.5%，&gt;5000=1%；农民固定 0.2-1 元（默认 0.5）；学生/下岗/抚恤/低保固定 0.2 元。
 */
@Service
public class DuesService {

    public static final String CAT_MONTHLY = "月薪";
    public static final String CAT_ANNUAL = "年薪制";
    public static final String CAT_SELF = "个体";
    public static final String CAT_RETIRED = "离退休";
    public static final String CAT_FARMER = "农民";
    public static final String CAT_STUDENT = "学生";
    public static final String CAT_LAYOFF = "下岗失业";
    public static final String CAT_PENSION = "抚恤救济";
    public static final String CAT_ALLOWANCE = "低保";

    private static final Set<String> TIER_CATEGORIES = Set.of(CAT_MONTHLY, CAT_ANNUAL, CAT_SELF);
    private static final Set<String> FIXED_02 = Set.of(CAT_STUDENT, CAT_LAYOFF, CAT_PENSION, CAT_ALLOWANCE);
    private static final BigDecimal DEFAULT_FARMER = new BigDecimal("0.50");
    private static final BigDecimal FIXED_02_AMOUNT = new BigDecimal("0.20");
    private static final BigDecimal BIG_AMOUNT = new BigDecimal("1000");
    private static final BigDecimal REMIT_RATIO = new BigDecimal("0.05");

    /** 党费账户须为六大行（E-DU-09，参数化）。 */
    public static final List<String> ALLOWED_BANKS =
            List.of("工商银行", "农业银行", "中国银行", "建设银行", "交通银行", "邮储银行");

    @Autowired private DuesBaseMapper baseMapper;
    @Autowired private DuesPaymentMapper paymentMapper;
    @Autowired private DuesReduceMapper reduceMapper;
    @Autowired private DuesBackpayMapper backpayMapper;
    @Autowired private DuesRemittanceMapper remittanceMapper;
    @Autowired private DuesAccountMapper accountMapper;
    @Autowired private PartyMemberMapper memberMapper;
    @Autowired private MembershipService membershipService;
    @Autowired private AuditService auditService;

    // ==================== 比例引擎 ====================

    /** 计算适用比例：返回 {category, ratio, mode}，mode=TIERN/FIXED。 */
    public Map<String, Object> resolveRate(String category, BigDecimal base) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("category", category);
        if (FIXED_02.contains(category)) {
            r.put("mode", "FIXED");
            r.put("fixed", FIXED_02_AMOUNT);
            r.put("ratio", null);
            return r;
        }
        if (CAT_FARMER.equals(category)) {
            r.put("mode", "FIXED");
            r.put("fixed", DEFAULT_FARMER);
            r.put("ratio", null);
            return r;
        }
        BigDecimal b = base == null ? BigDecimal.ZERO : base;
        BigDecimal ratio;
        if (CAT_RETIRED.equals(category)) {
            ratio = b.compareTo(new BigDecimal("5000")) <= 0
                    ? new BigDecimal("0.005") : new BigDecimal("0.01");
        } else if (TIER_CATEGORIES.contains(category)) {
            if (b.compareTo(new BigDecimal("3000")) <= 0) {
                ratio = new BigDecimal("0.005");
            } else if (b.compareTo(new BigDecimal("5000")) <= 0) {
                ratio = new BigDecimal("0.01");
            } else if (b.compareTo(new BigDecimal("10000")) <= 0) {
                ratio = new BigDecimal("0.015");
            } else {
                ratio = new BigDecimal("0.02");
            }
        } else {
            throw PartyBizException.block("E-DU-00", "未知人员类别：" + category);
        }
        r.put("mode", "TIER");
        r.put("ratio", ratio);
        return r;
    }

    /** 试算应缴金额（分以下四舍五入）。 */
    public Map<String, Object> calcPreview(String category, BigDecimal base, BigDecimal fixed) {
        Map<String, Object> r = resolveRate(category, base);
        BigDecimal due;
        if ("FIXED".equals(r.get("mode"))) {
            due = fixed != null && fixed.compareTo(BigDecimal.ZERO) > 0
                    ? fixed.setScale(2, RoundingMode.HALF_UP)
                    : ((BigDecimal) r.get("fixed")).setScale(2, RoundingMode.HALF_UP);
        } else {
            due = base.multiply((BigDecimal) r.get("ratio")).setScale(2, RoundingMode.HALF_UP);
        }
        r.put("base", base);
        r.put("due", due);
        return r;
    }

    // ==================== 基数维护 ====================

    public List<DuesBase> baseList(Long orgId, String keyword) {
        QueryWrapper<DuesBase> qw = new QueryWrapper<>();
        if (orgId != null) {
            qw.eq("org_id", orgId);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like("member_name", keyword);
        }
        qw.orderByDesc("eff_year", "eff_month").orderByAsc("org_id", "member_id");
        return baseMapper.selectList(qw);
    }

    /** 取党员在指定年月的有效基数（最新生效且已通过/正常）。 */
    public DuesBase effectiveBase(Long memberId, int year, int month) {
        List<DuesBase> list = baseMapper.selectList(new QueryWrapper<DuesBase>()
                .eq("member_id", memberId)
                .in("audit_status", "正常", "已批准")
                .and(w -> w.lt("eff_year", year).or(q -> q.eq("eff_year", year).le("eff_month", month)))
                .orderByDesc("eff_year", "eff_month").last("limit 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    @Transactional
    public DuesBase saveBase(DuesBase body) {
        if (body.getMemberId() == null) {
            throw PartyBizException.block("E-DU-01", "请选择党员");
        }
        PartyMember m = memberMapper.selectById(body.getMemberId());
        if (m == null) {
            throw PartyBizException.block("E-DU-01", "党员不存在");
        }
        String category = body.getMemberCategory() == null ? CAT_MONTHLY : body.getMemberCategory();
        body.setMemberCategory(category);
        BigDecimal base = body.getBaseAmount() == null ? BigDecimal.ZERO : body.getBaseAmount();
        if ("FIXED".equals(resolveRate(category, base).get("mode"))) {
            Map<String, Object> pv = calcPreview(category, base, body.getFixedAmount());
            body.setFixedAmount((BigDecimal) pv.get("due"));
            body.setRatio(null);
            body.setBaseAmount(BigDecimal.ZERO);
        } else {
            if (base.compareTo(BigDecimal.ZERO) <= 0) {
                throw PartyBizException.block("E-DU-01", "计算基数缺失或为0，请先维护基数");
            }
            body.setFixedAmount(null);
            body.setRatio((BigDecimal) resolveRate(category, base).get("ratio"));
        }
        if (body.getEffYear() == null || body.getEffMonth() == null) {
            YearMonth now = YearMonth.now();
            body.setEffYear(now.getYear());
            body.setEffMonth(now.getMonthValue());
        }

        DuesBase prev = baseMapper.selectOne(new QueryWrapper<DuesBase>()
                .eq("member_id", m.getId())
                .and(w -> w.lt("eff_year", body.getEffYear())
                        .or(q -> q.eq("eff_year", body.getEffYear()).lt("eff_month", body.getEffMonth())))
                .orderByDesc("eff_year", "eff_month").last("limit 1"));
        body.setPrevBase(prev != null && prev.getBaseAmount() != null ? prev.getBaseAmount() : null);
        // E-DU-02：基数调整超 ±20% 需审批后方可生效
        boolean needAudit = false;
        if (body.getPrevBase() != null && body.getPrevBase().compareTo(BigDecimal.ZERO) > 0
                && base.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = base.subtract(body.getPrevBase()).abs()
                    .divide(body.getPrevBase(), 4, RoundingMode.HALF_UP);
            needAudit = diff.compareTo(new BigDecimal("0.20")) > 0;
        }
        body.setAuditStatus(needAudit ? "待审批" : "正常");
        body.setMemberName(m.getName());
        body.setOrgId(m.getOrgId());
        body.setOrgName(m.getOrgName());
        body.setCreator(CurrentHeader.operator());
        body.setId(null);
        body.setCreateTime(LocalDateTime.now());
        body.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(body);
        auditService.log("党费基数维护", "DuesBase", body.getId(),
                m.getName() + " " + category + " 基数" + body.getBaseAmount()
                        + (needAudit ? "（调整超20%，待审批 E-DU-02）" : ""));
        return body;
    }

    @Transactional
    public DuesBase auditBase(Long id, boolean pass, String opinion) {
        DuesBase b = baseMapper.selectById(id);
        if (b == null) {
            throw PartyBizException.block("E-DU-02", "基数记录不存在");
        }
        if (!"待审批".equals(b.getAuditStatus())) {
            throw PartyBizException.block("E-DU-02", "该基数调整无需审批");
        }
        b.setAuditStatus(pass ? "已批准" : "正常");
        b.setApprover(CurrentHeader.operator());
        b.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(b);
        auditService.log(pass ? "基数调整批准" : "基数调整驳回", "DuesBase", id,
                b.getMemberName() + (opinion == null ? "" : "：" + opinion));
        return b;
    }

    // ==================== 月度账单 ====================

    @Transactional
    public Map<String, Object> generateBills(Long orgId, int year, int month) {
        QueryWrapper<PartyMember> qw = new QueryWrapper<>();
        if (orgId != null) {
            qw.eq("org_id", orgId);
        }
        qw.orderByAsc("org_id", "id");
        List<PartyMember> members = memberMapper.selectList(qw);

        List<Map<String, Object>> created = new ArrayList<>();
        List<Map<String, Object>> skipped = new ArrayList<>();
        int seq = 0;
        for (PartyMember m : members) {
            Map<String, Object> skipRow = null;
            String partyStatus = "正常".equals(membershipService.currentStatus(m.getId())) ? null : membershipService.currentStatus(m.getId());
            if (!"正式党员".equals(m.getMemberStatus()) && !"预备党员".equals(m.getMemberStatus())) {
                skipRow = skip(m, "非在册缴费党员（" + m.getMemberStatus() + "）");
            } else if (partyStatus != null) {
                skipRow = skip(m, "党籍状态[" + partyStatus + "]，停止党费");
            }
            if (skipRow != null) {
                skipped.add(skipRow);
                continue;
            }
            DuesBase base = effectiveBase(m.getId(), year, month);
            if (base == null) {
                skipped.add(skip(m, "计算基数缺失或为0（E-DU-01）"));
                continue;
            }
            Long exists = paymentMapper.selectCount(new QueryWrapper<DuesPayment>()
                    .eq("member_id", m.getId()).eq("fee_year", year).eq("fee_month", month));
            if (exists != null && exists > 0) {
                skipped.add(skip(m, year + "年" + month + "月账单已存在（幂等跳过）"));
                continue;
            }
            Map<String, Object> pv = calcPreview(base.getMemberCategory(), base.getBaseAmount(), base.getFixedAmount());
            DuesPayment p = new DuesPayment();
            p.setMemberId(m.getId());
            p.setMemberName(m.getName());
            p.setOrgId(m.getOrgId());
            p.setOrgName(m.getOrgName());
            p.setFeeYear(year);
            p.setFeeMonth(month);
            p.setBaseAmount(base.getBaseAmount());
            p.setMemberCategory(base.getMemberCategory());
            p.setRatio((BigDecimal) pv.get("ratio"));
            p.setDueAmount((BigDecimal) pv.get("due"));
            p.setStatus("待缴");
            p.setLargeFlag(0);
            p.setBillNo("ZD" + year + String.format("%02d", month) + String.format("%04d", ++seq));
            p.setCreateTime(LocalDateTime.now());
            p.setUpdateTime(LocalDateTime.now());
            paymentMapper.insert(p);
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("billId", p.getId());
            c.put("memberName", m.getName());
            c.put("due", p.getDueAmount());
            created.add(c);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("createdCount", created.size());
        result.put("created", created);
        result.put("skipped", skipped);
        auditService.log("月度账单生成", "DuesPayment", null,
                year + "-" + month + " 生成" + created.size() + "张，跳过" + skipped.size() + "人");
        return result;
    }

    private Map<String, Object> skip(PartyMember m, String reason) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("memberId", m.getId());
        r.put("memberName", m.getName());
        r.put("orgName", m.getOrgName());
        r.put("reason", reason);
        return r;
    }

    public List<DuesPayment> bills(Integer year, Integer month, Long orgId, String status) {
        QueryWrapper<DuesPayment> qw = new QueryWrapper<>();
        if (year != null) qw.eq("fee_year", year);
        if (month != null) qw.eq("fee_month", month);
        if (orgId != null) qw.eq("org_id", orgId);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        qw.orderByAsc("org_id", "member_id", "fee_year", "fee_month");
        return paymentMapper.selectList(qw);
    }

    // ==================== 收缴登记 / 财务确认 ====================

    @Transactional
    public DuesPayment registerPayment(Map<String, Object> body) {
        Long billId = body.get("billId") == null ? null : Long.valueOf(body.get("billId").toString());
        DuesPayment bill = billId == null ? null : paymentMapper.selectById(billId);
        if (bill == null && body.get("memberId") != null && body.get("feeYear") != null && body.get("feeMonth") != null) {
            Long memberId = Long.valueOf(body.get("memberId").toString());
            bill = paymentMapper.selectOne(new QueryWrapper<DuesPayment>()
                    .eq("member_id", memberId)
                    .eq("fee_year", Integer.valueOf(body.get("feeYear").toString()))
                    .eq("fee_month", Integer.valueOf(body.get("feeMonth").toString())).last("limit 1"));
        }
        if (bill == null) {
            throw PartyBizException.block("E-DU-03", "账单不存在，请先生成月度账单");
        }
        if ("已缴".equals(bill.getStatus())) {
            throw PartyBizException.block("E-DU-03", "同一党员同一月份已有收缴记录，不得重复登记");
        }
        String payMethod = body.get("payMethod") == null ? null : body.get("payMethod").toString();
        if (payMethod == null || payMethod.isBlank()) {
            throw PartyBizException.block("E-DU-03", "缴纳方式必填（现金/转账/银行代扣/线上缴纳/委托代缴）");
        }
        BigDecimal paid = body.get("paidAmount") == null ? null
                : new BigDecimal(body.get("paidAmount").toString());
        if (paid == null || paid.compareTo(BigDecimal.ZERO) <= 0) {
            throw PartyBizException.block("E-DU-03", "实缴金额必须大于0");
        }
        String diffReason = body.get("diffReason") == null ? null : body.get("diffReason").toString();
        boolean hasReduce = bill.getReduceId() != null
                || reduceMapper.selectCount(new QueryWrapper<DuesReduce>()
                        .eq("member_id", bill.getMemberId())
                        .eq("fee_year", bill.getFeeYear()).eq("fee_month", bill.getFeeMonth())
                        .eq("audit_status", "已批准")) > 0;
        boolean hasBackpay = bill.getBackpayId() != null;
        if (paid.compareTo(bill.getDueAmount()) != 0 && !hasReduce && !hasBackpay
                && (diffReason == null || diffReason.isBlank())) {
            throw PartyBizException.block("E-DU-04",
                    "实缴金额(" + paid + ")与应缴(" + bill.getDueAmount() + ")不一致，须填写差异原因或生成减免/补缴单");
        }
        bill.setPaidAmount(paid.setScale(2, RoundingMode.HALF_UP));
        bill.setPayMethod(payMethod);
        bill.setPayType(payMethod);
        if (body.get("voucherNo") != null) bill.setVoucherNo(body.get("voucherNo").toString());
        if (body.get("payDate") != null) bill.setPayDate(LocalDate.parse(body.get("payDate").toString()));
        else bill.setPayDate(LocalDate.now());
        if (diffReason != null) bill.setDiffReason(diffReason);
        bill.setStatus("已缴");
        bill.setLargeFlag(paid.compareTo(BIG_AMOUNT) >= 0 ? 1 : 0);
        bill.setCollectorName(CurrentHeader.operator());
        bill.setCollector(CurrentHeader.operator());
        bill.setUpdateTime(LocalDateTime.now());
        paymentMapper.updateById(bill);
        auditService.log("党费收缴登记", "DuesPayment", bill.getId(),
                bill.getMemberName() + " " + bill.getFeeYear() + "-" + bill.getFeeMonth()
                        + " 实缴" + paid + (bill.getLargeFlag() == 1 ? "（大额党费≥1000）" : ""));
        return bill;
    }

    /** 财务确认入账（R12；会计出纳分设：确认人不得是登记经办人）。 */
    @Transactional
    public int confirmPayments(List<Long> ids) {
        String me = CurrentHeader.operator();
        int n = 0;
        for (Long id : ids) {
            DuesPayment bill = paymentMapper.selectById(id);
            if (bill == null || !"已缴".equals(bill.getStatus()) || bill.getConfirmer() != null) {
                continue;
            }
            if (bill.getLargeFlag() != null && bill.getLargeFlag() == 1 && bill.getRemitId() == null) {
                throw PartyBizException.block("E-DU-08",
                        bill.getMemberName() + " " + bill.getFeeYear() + "-" + bill.getFeeMonth()
                                + " 为大额党费，须先生成逐级上缴单后方可归档确认");
            }
            if (me.equals(bill.getCollectorName())) {
                throw PartyBizException.block("E-DU-12",
                        "会计出纳分设：财务确认人(" + me + ")不得是该笔收缴的登记经办人");
            }
            bill.setConfirmer(me);
            bill.setConfirmTime(LocalDateTime.now());
            bill.setUpdateTime(LocalDateTime.now());
            paymentMapper.updateById(bill);
            n++;
        }
        auditService.log("党费财务确认", "DuesPayment", null, "批量确认入账 " + n + " 笔，确认人=" + me);
        return n;
    }

    // ==================== 减免 / 补缴 ====================

    public List<DuesReduce> reduceList(Long orgId, String status) {
        QueryWrapper<DuesReduce> qw = new QueryWrapper<>();
        if (orgId != null) qw.eq("org_id", orgId);
        if (status != null && !status.isBlank()) qw.eq("audit_status", status);
        qw.orderByDesc("create_time");
        return reduceMapper.selectList(qw);
    }

    @Transactional
    public DuesReduce createReduce(DuesReduce body) {
        if (body.getMemberId() == null) throw PartyBizException.block("E-DU-05", "请选择党员");
        if (body.getFeeYear() == null || body.getFeeMonth() == null) throw PartyBizException.block("E-DU-05", "减免年月必填");
        if (body.getBranchRecord() == null || body.getBranchRecord().isBlank()) {
            throw PartyBizException.block("E-DU-05", "须附支部委员会研究记录");
        }
        PartyMember m = memberMapper.selectById(body.getMemberId());
        if (m == null) throw PartyBizException.block("E-DU-05", "党员不存在");
        body.setId(null);
        body.setMemberName(m.getName());
        body.setOrgId(m.getOrgId());
        body.setOrgName(m.getOrgName());
        body.setAuditStatus("待审批");
        body.setCreator(CurrentHeader.operator());
        body.setCreateTime(LocalDateTime.now());
        body.setUpdateTime(LocalDateTime.now());
        reduceMapper.insert(body);
        auditService.log("党费减免申请", "DuesReduce", body.getId(),
                m.getName() + " " + body.getReduceType() + " " + body.getFeeYear() + "-" + body.getFeeMonth());
        return body;
    }

    @Transactional
    public DuesReduce auditReduce(Long id, boolean pass, String opinion) {
        DuesReduce r = reduceMapper.selectById(id);
        if (r == null) throw PartyBizException.block("E-DU-05", "减免申请不存在");
        if (!"待审批".equals(r.getAuditStatus())) throw PartyBizException.block("E-DU-05", "该申请已审批");
        r.setAuditStatus(pass ? "已批准" : "已驳回");
        r.setAuditOpinion(opinion);
        r.setApprover(CurrentHeader.operator());
        r.setApproveDate(LocalDate.now());
        r.setUpdateTime(LocalDateTime.now());
        reduceMapper.updateById(r);
        if (pass) {
            // 批准后联动当月账单
            DuesPayment bill = paymentMapper.selectOne(new QueryWrapper<DuesPayment>()
                    .eq("member_id", r.getMemberId()).eq("fee_year", r.getFeeYear())
                    .eq("fee_month", r.getFeeMonth()).last("limit 1"));
            if (bill != null) {
                bill.setReduceId(r.getId());
                bill.setStatus("减免");
                if ("免交".equals(r.getReduceType())) {
                    bill.setDueAmount(BigDecimal.ZERO.setScale(2));
                } else if (r.getAmount() != null) {
                    bill.setDueAmount(r.getAmount().setScale(2, RoundingMode.HALF_UP));
                }
                paymentMapper.updateById(bill);
            }
        }
        auditService.log(pass ? "党费减免批准" : "党费减免驳回", "DuesReduce", id,
                r.getMemberName() + (opinion == null ? "" : "：" + opinion));
        return r;
    }

    public List<DuesBackpay> backpayList(Long orgId) {
        QueryWrapper<DuesBackpay> qw = new QueryWrapper<>();
        if (orgId != null) qw.eq("org_id", orgId);
        qw.orderByDesc("create_time");
        return backpayMapper.selectList(qw);
    }

    @Transactional
    public DuesBackpay createBackpay(DuesBackpay body) {
        if (body.getMemberId() == null) throw PartyBizException.block("E-DU-11", "请选择党员");
        if (body.getFromYearmonth() == null || body.getToYearmonth() == null) {
            throw PartyBizException.block("E-DU-11", "补缴起止月份(YYYYMM)必填");
        }
        if (body.getFromYearmonth() > body.getToYearmonth()) {
            throw PartyBizException.block("E-DU-11", "补缴起始月不得晚于截止月");
        }
        PartyMember m = memberMapper.selectById(body.getMemberId());
        if (m == null) throw PartyBizException.block("E-DU-11", "党员不存在");
        int months = monthSpan(body.getFromYearmonth(), body.getToYearmonth()) + 1;
        boolean over6 = months > 6;
        if (over6 && (body.getBranchAgreed() == null || body.getBranchAgreed() != 1)) {
            throw PartyBizException.block("E-DU-11",
                    "补交时间超过6个月，须说明原因并经党支部同意后方可登记");
        }
        body.setOverSixMonth(over6 ? 1 : 0);
        body.setId(null);
        body.setMemberName(m.getName());
        body.setOrgId(m.getOrgId());
        body.setOrgName(m.getOrgName());
        body.setOperator(CurrentHeader.operator());
        body.setPayDate(body.getPayDate() == null ? LocalDate.now() : body.getPayDate());
        body.setCreateTime(LocalDateTime.now());
        backpayMapper.insert(body);

        // 为区间内缺账月份生成补缴账单（已有账单的月份跳过）
        BigDecimal sum = BigDecimal.ZERO;
        YearMonth cur = ym(body.getFromYearmonth());
        YearMonth end = ym(body.getToYearmonth());
        int seq = 0;
        while (!cur.isAfter(end)) {
            Long exists = paymentMapper.selectCount(new QueryWrapper<DuesPayment>()
                    .eq("member_id", m.getId()).eq("fee_year", cur.getYear())
                    .eq("fee_month", cur.getMonthValue()));
            if (exists == null || exists == 0) {
                DuesBase base = effectiveBase(m.getId(), cur.getYear(), cur.getMonthValue());
                BigDecimal due = base == null ? BigDecimal.ZERO
                        : (BigDecimal) calcPreview(base.getMemberCategory(), base.getBaseAmount(), base.getFixedAmount()).get("due");
                DuesPayment p = new DuesPayment();
                p.setMemberId(m.getId());
                p.setMemberName(m.getName());
                p.setOrgId(m.getOrgId());
                p.setOrgName(m.getOrgName());
                p.setFeeYear(cur.getYear());
                p.setFeeMonth(cur.getMonthValue());
                if (base != null) {
                    p.setBaseAmount(base.getBaseAmount());
                    p.setMemberCategory(base.getMemberCategory());
                    p.setRatio(base.getRatio());
                }
                p.setDueAmount(due);
                p.setStatus("补缴");
                p.setBackpayId(body.getId());
                p.setLargeFlag(0);
                p.setBillNo("BJ" + cur.getYear() + String.format("%02d", cur.getMonthValue())
                        + String.format("%04d", ++seq));
                p.setCreateTime(LocalDateTime.now());
                p.setUpdateTime(LocalDateTime.now());
                paymentMapper.insert(p);
                sum = sum.add(due);
            }
            cur = cur.plusMonths(1);
        }
        if (body.getAmount() == null || body.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            body.setAmount(sum.setScale(2, RoundingMode.HALF_UP));
            backpayMapper.updateById(body);
        }
        auditService.log("党费补缴登记", "DuesBackpay", body.getId(),
                m.getName() + " " + body.getFromYearmonth() + "-" + body.getToYearmonth()
                        + (over6 ? "（超6个月，支部已同意 E-DU-11）" : ""));
        return body;
    }

    private int monthSpan(int from, int to) {
        return (to / 100 - from / 100) * 12 + (to % 100 - from % 100);
    }

    private YearMonth ym(int yyyymm) {
        return YearMonth.of(yyyymm / 100, yyyymm % 100);
    }

    // ==================== 催缴 / 脱党预警 ====================

    /**
     * 连续未缴预警：warn3=连续3个月（E-DU-06，通知支部书记），alarm6=连续6个月（E-DU-07，自行脱党告警）。
     * 统计口径：截至指定年/月（默认当前月上月），成员月末连续待缴账单长度。
     */
    public Map<String, Object> remind(Long orgId, Integer year, Integer month) {
        YearMonth end = (year == null || month == null) ? YearMonth.now().minusMonths(1) : YearMonth.of(year, month);
        List<PartyMember> members = memberMapper.selectList(
                orgId == null ? new QueryWrapper<PartyMember>() : new QueryWrapper<PartyMember>().eq("org_id", orgId));
        List<Map<String, Object>> warn3 = new ArrayList<>();
        List<Map<String, Object>> alarm6 = new ArrayList<>();
        for (PartyMember m : members) {
            if (!"正式党员".equals(m.getMemberStatus()) && !"预备党员".equals(m.getMemberStatus())) {
                continue;
            }
            int streak = 0;
            YearMonth c = end;
            List<String> months = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                DuesPayment p = paymentMapper.selectOne(new QueryWrapper<DuesPayment>()
                        .eq("member_id", m.getId()).eq("fee_year", c.getYear())
                        .eq("fee_month", c.getMonthValue()).last("limit 1"));
                boolean unpaid = p != null && ("待缴".equals(p.getStatus()) || "逾期".equals(p.getStatus()));
                if (unpaid) {
                    streak++;
                    months.add(c.getYear() + "-" + String.format("%02d", c.getMonthValue()));
                    c = c.minusMonths(1);
                } else {
                    break;
                }
            }
            if (streak >= 6) {
                Map<String, Object> row = remindRow(m, streak, months);
                row.put("level", "告警");
                alarm6.add(row);
            } else if (streak >= 3) {
                Map<String, Object> row = remindRow(m, streak, months);
                row.put("level", "预警");
                warn3.add(row);
            }
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("asOf", end.toString());
        r.put("warn3", warn3);
        r.put("alarm6", alarm6);
        return r;
    }

    private Map<String, Object> remindRow(PartyMember m, int streak, List<String> months) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("memberId", m.getId());
        row.put("memberName", m.getName());
        row.put("orgId", m.getOrgId());
        row.put("orgName", m.getOrgName());
        row.put("unpaidMonths", streak);
        row.put("months", months);
        return row;
    }

    /** 对连续6个月未缴党员生成"自行脱党"处置工单（党籍变动待审批）。 */
    @Transactional
    public int deserterWorkflow(List<Long> memberIds) {
        int n = 0;
        for (Long mid : memberIds) {
            PartyMember m = memberMapper.selectById(mid);
            if (m == null) {
                continue;
            }
            MembershipChange ch = new MembershipChange();
            ch.setMemberId(m.getId());
            ch.setChangeType("自行脱党");
            ch.setReason("无正当理由连续6个月不交纳党费，依据《党章》规定按自行脱党处理（系统 E-DU-07 自动预警联动）");
            ch.setEffectiveDate(LocalDate.now());
            ch.setBranchMeetingDate(LocalDate.now());
            ch.setSource("DUES");
            membershipService.createChange(ch);
            n++;
        }
        auditService.log("自行脱党处置工单", "MembershipChange", null, "生成 " + n + " 条");
        return n;
    }

    // ==================== 上缴（大额 / 年度5%） ====================

    public List<DuesRemittance> remittanceList(String type, Integer year) {
        QueryWrapper<DuesRemittance> qw = new QueryWrapper<>();
        if (type != null && !type.isBlank()) qw.eq("remit_type", type);
        if (year != null) qw.eq("fee_year", year);
        qw.orderByDesc("create_time");
        return remittanceMapper.selectList(qw);
    }

    @Transactional
    public DuesRemittance createLargeRemittance(Long paymentId) {
        DuesPayment bill = paymentMapper.selectById(paymentId);
        if (bill == null) throw PartyBizException.block("E-DU-08", "缴费记录不存在");
        if (bill.getLargeFlag() == null || bill.getLargeFlag() != 1) {
            throw PartyBizException.block("E-DU-08", "该笔党费不足1000元，不属于大额党费");
        }
        Long exists = remittanceMapper.selectCount(new QueryWrapper<DuesRemittance>()
                .eq("related_payment_id", paymentId));
        if (exists != null && exists > 0) {
            throw PartyBizException.block("E-DU-08", "该笔大额党费已生成上缴单");
        }
        DuesRemittance r = new DuesRemittance();
        r.setRemitNo("DE" + LocalDate.now().toString().replace("-", "") + System.currentTimeMillis() % 10000);
        r.setRemitType("大额党费");
        r.setOrgId(bill.getOrgId());
        r.setOrgName(bill.getOrgName());
        r.setFeeYear(bill.getFeeYear());
        r.setAmount(bill.getPaidAmount());
        r.setRatio(BigDecimal.ONE);
        r.setPayee("中央组织部（逐级转交）");
        r.setStatus("待汇出");
        r.setRelatedPaymentId(paymentId);
        r.setCreator(CurrentHeader.operator());
        r.setCreateTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        remittanceMapper.insert(r);
        bill.setRemitId(r.getId());
        paymentMapper.updateById(bill);
        auditService.log("大额党费上缴单", "DuesRemittance", r.getId(),
                bill.getMemberName() + " " + bill.getPaidAmount() + "元，全部上缴中央");
        return r;
    }

    /** 年度上缴：全年实交党费总数 × 5%（国资委党委系统口径，次年4月底前汇入）。 */
    @Transactional
    public DuesRemittance createYearlyRemittance(Long orgId, int year) {
        List<DuesPayment> bills = paymentMapper.selectList(new QueryWrapper<DuesPayment>()
                .eq("fee_year", year).eq("status", "已缴")
                .eq(orgId != null, "org_id", orgId));
        BigDecimal total = bills.stream().map(DuesPayment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw PartyBizException.block("E-DU-13", year + "年度无实交党费，无法生成上缴单");
        }
        Long exists = remittanceMapper.selectCount(new QueryWrapper<DuesRemittance>()
                .eq("remit_type", "年度上缴").eq("fee_year", year)
                .eq(orgId != null, "org_id", orgId).isNull(orgId == null, "org_id"));
        if (exists != null && exists > 0) {
            throw PartyBizException.block("E-DU-13", year + "年度上缴单已存在，不得重复生成");
        }
        String orgName = null;
        if (orgId != null) {
            PartyMember any = memberMapper.selectOne(new QueryWrapper<PartyMember>().eq("org_id", orgId).last("limit 1"));
            orgName = any == null ? null : any.getOrgName();
        }
        DuesRemittance r = new DuesRemittance();
        r.setRemitNo("NJ" + year + (orgId == null ? "ALL" : orgId.toString()));
        r.setRemitType("年度上缴");
        r.setOrgId(orgId);
        r.setOrgName(orgName);
        r.setFeeYear(year);
        r.setAmount(total.multiply(REMIT_RATIO).setScale(2, RoundingMode.HALF_UP));
        r.setRatio(REMIT_RATIO);
        r.setPayee("中央组织部党费账户（次年4月底前汇入）");
        r.setStatus("待汇出");
        r.setRemark("全年实交总数" + total + "元 × 5%");
        r.setCreator(CurrentHeader.operator());
        r.setCreateTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        remittanceMapper.insert(r);
        auditService.log("年度党费上缴单", "DuesRemittance", r.getId(),
                year + "年实交" + total + "，上缴5%=" + r.getAmount());
        return r;
    }

    @Transactional
    public DuesRemittance receipt(Long id, String receiptNo, LocalDate receiptDate, LocalDate remitDate) {
        DuesRemittance r = remittanceMapper.selectById(id);
        if (r == null) throw PartyBizException.block("E-DU-13", "上缴单不存在");
        if (receiptNo == null || receiptNo.isBlank()) throw PartyBizException.block("E-DU-13", "收据号必填");
        r.setReceiptNo(receiptNo);
        r.setReceiptDate(receiptDate == null ? LocalDate.now() : receiptDate);
        if (remitDate != null) r.setRemitDate(remitDate);
        if (r.getRemitDate() == null) r.setRemitDate(LocalDate.now());
        r.setStatus("已回收据");
        r.setUpdateTime(LocalDateTime.now());
        remittanceMapper.updateById(r);
        auditService.log("上缴收据登记", "DuesRemittance", id, receiptNo);
        return r;
    }

    // ==================== 党费账户 ====================

    public List<DuesAccount> accountList() {
        return accountMapper.selectList(new QueryWrapper<DuesAccount>().orderByDesc("create_time"));
    }

    @Transactional
    public DuesAccount saveAccount(DuesAccount body) {
        if (body.getBankName() == null || body.getBankName().isBlank()) {
            throw PartyBizException.block("E-DU-09", "开户银行必填");
        }
        boolean ok = ALLOWED_BANKS.stream().anyMatch(b -> body.getBankName().contains(b));
        if (!ok) {
            throw PartyBizException.block("E-DU-09",
                    "党费账户须在规定银行（六大行：" + String.join("/", ALLOWED_BANKS) + "）开立");
        }
        if (body.getAccountNo() == null || body.getAccountNo().isBlank()) {
            throw PartyBizException.block("E-DU-09", "银行账号必填");
        }
        body.setId(null);
        body.setEnabled(body.getEnabled() == null ? 1 : body.getEnabled());
        body.setCreator(CurrentHeader.operator());
        body.setCreateTime(LocalDateTime.now());
        body.setUpdateTime(LocalDateTime.now());
        accountMapper.insert(body);
        return body;
    }

    // ==================== 统计 ====================

    public Map<String, Object> stats(int year, Long orgId) {
        List<DuesPayment> bills = paymentMapper.selectList(new QueryWrapper<DuesPayment>()
                .eq("fee_year", year).eq(orgId != null, "org_id", orgId));
        BigDecimal totalDue = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        Map<Integer, BigDecimal> dueByMonth = new LinkedHashMap<>();
        Map<Integer, BigDecimal> paidByMonth = new LinkedHashMap<>();
        Map<String, BigDecimal> categoryPaid = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            dueByMonth.put(i, BigDecimal.ZERO);
            paidByMonth.put(i, BigDecimal.ZERO);
        }
        for (DuesPayment p : bills) {
            BigDecimal due = p.getDueAmount() == null ? BigDecimal.ZERO : p.getDueAmount();
            BigDecimal paid = p.getPaidAmount() == null ? BigDecimal.ZERO : p.getPaidAmount();
            if (!"减免".equals(p.getStatus())) {
                totalDue = totalDue.add(due);
            }
            if ("已缴".equals(p.getStatus())) {
                totalPaid = totalPaid.add(paid);
                paidByMonth.merge(p.getFeeMonth(), paid, BigDecimal::add);
                String cat = p.getMemberCategory() == null ? "未分类" : p.getMemberCategory();
                categoryPaid.merge(cat, paid, BigDecimal::add);
            }
            if (!"减免".equals(p.getStatus())) {
                dueByMonth.merge(p.getFeeMonth(), due, BigDecimal::add);
            }
        }
        List<Map<String, Object>> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> mo = new LinkedHashMap<>();
            mo.put("month", i);
            mo.put("due", dueByMonth.get(i));
            mo.put("paid", paidByMonth.get(i));
            mo.put("rate", dueByMonth.get(i).compareTo(BigDecimal.ZERO) == 0 ? null
                    : paidByMonth.get(i).multiply(new BigDecimal("100"))
                            .divide(dueByMonth.get(i), 1, RoundingMode.HALF_UP));
            months.add(mo);
        }
        List<Map<String, Object>> categories = new ArrayList<>();
        categoryPaid.forEach((k, v) -> {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("category", k);
            c.put("paid", v);
            categories.add(c);
        });
        BigDecimal remitted = remittanceMapper.selectList(new QueryWrapper<DuesRemittance>()
                        .eq("fee_year", year).eq("remit_type", "年度上缴")
                        .eq(orgId != null, "org_id", orgId)).stream()
                .map(DuesRemittance::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("year", year);
        r.put("totalDue", totalDue);
        r.put("totalPaid", totalPaid);
        r.put("collectRate", totalDue.compareTo(BigDecimal.ZERO) == 0 ? null
                : totalPaid.multiply(new BigDecimal("100")).divide(totalDue, 1, RoundingMode.HALF_UP));
        r.put("remitted", remitted);
        r.put("retained", totalPaid.subtract(remitted));
        r.put("months", months);
        r.put("categories", categories);
        return r;
    }

    public List<String> categories() {
        return Arrays.asList(CAT_MONTHLY, CAT_ANNUAL, CAT_SELF, CAT_RETIRED, CAT_FARMER,
                CAT_STUDENT, CAT_LAYOFF, CAT_PENSION, CAT_ALLOWANCE);
    }
}
