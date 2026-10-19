package com.example.culturemgt.behavior.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.culturemgt.behavior.entity.*;
import com.example.culturemgt.behavior.mapper.*;
import com.example.culturemgt.common.cul.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ScoreService {

    private final BehaviorScoreAccountMapper accountMapper;
    private final BehaviorScoreTxnMapper txnMapper;
    private final AuditService auditService;

    private static final String[] DIMS = {"客户导向", "奋斗担当", "艰苦奋斗", "自我批判"};

    public ScoreService(BehaviorScoreAccountMapper accountMapper,
                        BehaviorScoreTxnMapper txnMapper,
                        AuditService auditService) {
        this.accountMapper = accountMapper;
        this.txnMapper = txnMapper;
        this.auditService = auditService;
    }

    /* ========== 隐私隔离查询 ========== */

    public BehaviorScoreAccount myAccount() {
        String me = CurrentHeader.operator();
        BehaviorScoreAccount acc = accountMapper.selectOne(
            new LambdaQueryWrapper<BehaviorScoreAccount>()
                .eq(BehaviorScoreAccount::getUserName, me)
                .eq(BehaviorScoreAccount::getDeleted, 0));
        if (acc == null) {
            acc = ensureAccount(me, null);
        }
        return acc;
    }

    public BehaviorScoreAccount accountOf(String userName) {
        BehaviorFlow.require(userName != null && !userName.isBlank(), "12", "参数缺失");
        String me = CurrentHeader.operator();
        Set<String> roles = CulRole.currentRoles();
        boolean canView = me.equals(userName)
            || roles.contains("C99")
            || roles.contains("C03")
            || roles.contains("C02")
            || roles.contains("C07");
        if (!canView && (roles.contains("C04") || roles.contains("C05"))) {
            BehaviorScoreAccount mine = accountMapper.selectOne(
                new LambdaQueryWrapper<BehaviorScoreAccount>()
                    .eq(BehaviorScoreAccount::getUserName, me)
                    .eq(BehaviorScoreAccount::getDeleted, 0));
            if (mine != null && mine.getDept() != null) {
                BehaviorScoreAccount target = accountMapper.selectOne(
                    new LambdaQueryWrapper<BehaviorScoreAccount>()
                        .eq(BehaviorScoreAccount::getUserName, userName)
                        .eq(BehaviorScoreAccount::getDeleted, 0));
                canView = target != null && mine.getDept().equals(target.getDept());
            }
        }
        BehaviorFlow.require(canView, "12", "无权查看他人积分");
        BehaviorScoreAccount acc = accountMapper.selectOne(
            new LambdaQueryWrapper<BehaviorScoreAccount>()
                .eq(BehaviorScoreAccount::getUserName, userName)
                .eq(BehaviorScoreAccount::getDeleted, 0));
        return acc != null ? acc : ensureAccount(userName, null);
    }

    public List<BehaviorScoreAccount> deptList(String dept) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        if (dept == null || dept.isBlank()) {
            return accountMapper.selectList(
                new LambdaQueryWrapper<BehaviorScoreAccount>()
                    .eq(BehaviorScoreAccount::getDeleted, 0)
                    .orderByDesc(BehaviorScoreAccount::getTotalScore));
        }
        return accountMapper.selectList(
            new LambdaQueryWrapper<BehaviorScoreAccount>()
                .eq(BehaviorScoreAccount::getDept, dept)
                .eq(BehaviorScoreAccount::getDeleted, 0)
                .orderByDesc(BehaviorScoreAccount::getTotalScore));
    }

    public List<BehaviorScoreTxn> txnList(String userName, String dimension, String periodTag) {
        String me = CurrentHeader.operator();
        if (!me.equals(userName)) {
            Set<String> roles = CulRole.currentRoles();
            boolean ok = roles.contains("C99") || roles.contains("C03") || roles.contains("C02") || roles.contains("C07");
            if (!ok && (roles.contains("C04") || roles.contains("C05"))) {
                BehaviorScoreAccount mine = accountMapper.selectOne(
                    new LambdaQueryWrapper<BehaviorScoreAccount>()
                        .eq(BehaviorScoreAccount::getUserName, me)
                        .eq(BehaviorScoreAccount::getDeleted, 0));
                BehaviorScoreAccount target = accountMapper.selectOne(
                    new LambdaQueryWrapper<BehaviorScoreAccount>()
                        .eq(BehaviorScoreAccount::getUserName, userName)
                        .eq(BehaviorScoreAccount::getDeleted, 0));
                ok = mine != null && target != null && mine.getDept().equals(target.getDept());
            }
            BehaviorFlow.require(ok, "12", "无权查看他人流水");
        }
        LambdaQueryWrapper<BehaviorScoreTxn> w = new LambdaQueryWrapper<>();
        w.eq(BehaviorScoreTxn::getUserName, userName).eq(BehaviorScoreTxn::getDeleted, 0);
        if (dimension != null && !dimension.isBlank()) w.eq(BehaviorScoreTxn::getDimension, dimension);
        if (periodTag != null && !periodTag.isBlank()) w.eq(BehaviorScoreTxn::getPeriodTag, periodTag);
        w.orderByDesc(BehaviorScoreTxn::getTxnTime);
        return txnMapper.selectList(w);
    }

    public List<Map<String, Object>> rankList(String period, String scope) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        List<BehaviorScoreAccount> list = accountMapper.selectList(
            new LambdaQueryWrapper<BehaviorScoreAccount>()
                .eq(BehaviorScoreAccount::getDeleted, 0));
        list.sort((a, b) -> {
            int c = Double.compare(b.getTotalScore() == null ? 0 : b.getTotalScore(),
                                     a.getTotalScore() == null ? 0 : a.getTotalScore());
            return c != 0 ? c : Integer.compare(a.getNegCount() == null ? 0 : a.getNegCount(),
                                                  b.getNegCount() == null ? 0 : b.getNegCount());
        });
        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (BehaviorScoreAccount a : list) {
            if ("dept".equals(scope) && a.getDept() == null) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rank", rank++);
            m.put("userName", a.getUserName());
            m.put("dept", a.getDept());
            m.put("totalScore", a.getTotalScore());
            m.put("posCount", a.getPosCount());
            m.put("negCount", a.getNegCount());
            result.add(m);
        }
        return result;
    }

    public Map<String, Object> deptCompliance(String dept, String periodTag) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        List<BehaviorScoreAccount> list = accountMapper.selectList(
            new LambdaQueryWrapper<BehaviorScoreAccount>()
                .eq(BehaviorScoreAccount::getDept, dept)
                .eq(BehaviorScoreAccount::getDeleted, 0));
        int total = list.size();
        int passCount = (int) list.stream()
            .filter(a -> a.getTotalScore() != null && a.getTotalScore() >= 70.0)
            .count();
        double rate = total > 0 ? new BigDecimal(passCount * 10000.0 / total)
            .setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0;
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("dept", dept);
        r.put("total", total);
        r.put("passCount", passCount);
        r.put("complianceRate", rate);
        return r;
    }

    /* ========== 记账核心（包级可见，供 Evidence/Audit/Review 调用） ========== */

    @Transactional
    public Long bookFromEvidence(BehaviorEvidence e) {
        return book(e.getContributor(), e.getValueDimension(), "加",
            e.getFinalScore() != null ? e.getFinalScore() : (e.getSelfScore() != null ? e.getSelfScore() : 0.0),
            "举证", e.getId(), e.getEvidenceNo(), CurrentHeader.operator(), "举证复核入账");
    }

    @Transactional
    public Long bookFromAudit(BehaviorAudit a) {
        return book(a.getTargetUser(), a.getViolateDimension(), "减",
            a.getDeductScore() != null ? a.getDeductScore() : 0.0,
            "稽核", a.getId(), a.getAuditNo(), CurrentHeader.operator(), "稽核扣分生效");
    }

    @Transactional
    public Long bookFromReview(BehaviorReviewRecord r) {
        double amt = r.getTotalScore() != null ? r.getTotalScore() / 10.0 : 0.0;
        return book(r.getEvaluatee(), "奋斗担当", "加", amt,
            "评议", r.getId(), r.getPeriodTag(), CurrentHeader.operator(), "价值观评议加分");
    }

    private Long book(String userName, String dimension, String direction, double amount,
                      String sourceType, Long sourceId, String sourceNo, String operator, String remark) {
        if (amount <= 0) return null;
        // 幂等
        long exists = txnMapper.selectCount(
            new LambdaQueryWrapper<BehaviorScoreTxn>()
                .eq(BehaviorScoreTxn::getSourceType, sourceType)
                .eq(BehaviorScoreTxn::getSourceId, sourceId)
                .eq(BehaviorScoreTxn::getDirection, direction)
                .eq(BehaviorScoreTxn::getDeleted, 0));
        BehaviorFlow.require(exists == 0, "13", "积分已入账，不可重复");

        BehaviorScoreAccount acc = ensureAccount(userName, null);
        // 写流水
        BehaviorScoreTxn txn = new BehaviorScoreTxn();
        txn.setUserName(userName);
        txn.setDimension(dimension);
        txn.setDirection(direction);
        txn.setAmount(new BigDecimal(amount).setScale(1, RoundingMode.HALF_UP).doubleValue());
        txn.setSourceType(sourceType);
        txn.setSourceId(sourceId);
        txn.setSourceNo(sourceNo);
        txn.setPeriodTag(LocalDateTime.now().toString().substring(0, 7));
        txn.setOperator(operator);
        txn.setRemark(remark);
        txn.setTxnTime(LocalDateTime.now());
        txn.setCreateTime(LocalDateTime.now());
        txnMapper.insert(txn);

        // 更新主档
        double delta = "加".equals(direction) ? amount : -amount;
        double newTotal = (acc.getTotalScore() != null ? acc.getTotalScore() : 60.0) + delta;
        LambdaUpdateWrapper<BehaviorScoreAccount> up = new LambdaUpdateWrapper<>();
        up.eq(BehaviorScoreAccount::getId, acc.getId());
        up.set(BehaviorScoreAccount::getTotalScore,
            new BigDecimal(newTotal).setScale(1, RoundingMode.HALF_UP).doubleValue());
        up.set(BehaviorScoreAccount::getLastCalcTime, LocalDateTime.now());
        if ("加".equals(direction)) {
            up.setSql("pos_count = pos_count + 1");
        } else {
            up.setSql("neg_count = neg_count + 1");
        }
        // 维度累计
        String dimCol = dimColumn(dimension);
        if (dimCol != null) {
            up.setSql(dimCol + " = " + dimCol + " + " + new BigDecimal(delta).setScale(1, RoundingMode.HALF_UP).doubleValue());
        }
        accountMapper.update(null, up);

        auditService.log(sourceType + "_BOOK", "cul_behavior_score_txn",
            txn.getId(), userName + " 积分" + direction + amount + " 来源=" + sourceType + " #" + sourceId);
        return txn.getId();
    }

    private String dimColumn(String dimension) {
        return switch (dimension) {
            case "客户导向" -> "dim_customer";
            case "奋斗担当" -> "dim_strive";
            case "艰苦奋斗" -> "dim_hardship";
            case "自我批判" -> "dim_reflect";
            default -> null;
        };
    }

    @Transactional
    public BehaviorScoreAccount ensureAccount(String userName, String dept) {
        BehaviorScoreAccount acc = accountMapper.selectOne(
            new LambdaQueryWrapper<BehaviorScoreAccount>()
                .eq(BehaviorScoreAccount::getUserName, userName)
                .eq(BehaviorScoreAccount::getDeleted, 0));
        if (acc == null) {
            acc = new BehaviorScoreAccount();
            acc.setUserName(userName);
            acc.setDept(dept != null ? dept : "");
            acc.setTotalScore(60.0);
            acc.setDimCustomer(0.0);
            acc.setDimStrive(0.0);
            acc.setDimHardship(0.0);
            acc.setDimReflect(0.0);
            acc.setPosCount(0);
            acc.setNegCount(0);
            acc.setLastCalcTime(LocalDateTime.now());
            acc.setCreateTime(LocalDateTime.now());
            acc.setUpdateTime(LocalDateTime.now());
            accountMapper.insert(acc);
        }
        return acc;
    }

    /* ========== 算法71/72 ========== */

    public static double calcTotal(double customer, double strive, double hardship, double reflect) {
        return new BigDecimal(customer * 0.25 + strive * 0.30 + hardship * 0.25 + reflect * 0.20)
            .setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    public static String levelOf(double total) {
        if (total >= 95.0) return "S";
        if (total >= 85.0) return "A";
        if (total >= 70.0) return "B";
        if (total >= 60.0) return "C";
        return "D";
    }
}
