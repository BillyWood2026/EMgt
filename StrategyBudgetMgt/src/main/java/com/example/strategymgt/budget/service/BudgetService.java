package com.example.strategymgt.budget.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.strategymgt.budget.entity.*;
import com.example.strategymgt.budget.mapper.*;
import com.example.strategymgt.common.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BudgetService extends ServiceImpl<AnnualBudgetMapper, AnnualBudget> {

    private final BudgetSubjectMapper subjectMapper;
    private final BudgetDimensionMapper dimMapper;
    private final BudgetParamMapper paramMapper;
    private final BudgetControlRuleMapper ruleMapper;
    private final BudgetVersionMapper versionMapper;
    private final ProjectBudgetMapper projectMapper;
    private final BudgetOccupationMapper occupationMapper;
    private final BudgetChangeMapper changeMapper;
    private final RollingForecastMapper forecastMapper;
    private final BudgetVarianceMapper varianceMapper;
    private final BudgetAuditMapper auditMapper;

    public BudgetService(BudgetSubjectMapper subjectMapper, BudgetDimensionMapper dimMapper,
                         BudgetParamMapper paramMapper, BudgetControlRuleMapper ruleMapper,
                         BudgetVersionMapper versionMapper, ProjectBudgetMapper projectMapper,
                         BudgetOccupationMapper occupationMapper, BudgetChangeMapper changeMapper,
                         RollingForecastMapper forecastMapper, BudgetVarianceMapper varianceMapper,
                         BudgetAuditMapper auditMapper) {
        this.subjectMapper = subjectMapper;
        this.dimMapper = dimMapper;
        this.paramMapper = paramMapper;
        this.ruleMapper = ruleMapper;
        this.versionMapper = versionMapper;
        this.projectMapper = projectMapper;
        this.occupationMapper = occupationMapper;
        this.changeMapper = changeMapper;
        this.forecastMapper = forecastMapper;
        this.varianceMapper = varianceMapper;
        this.auditMapper = auditMapper;
    }

    // ==================== 预算主数据 ====================
    public List<BudgetSubject> listSubjects() { return subjectMapper.selectList(null); }
    public List<BudgetDimension> listDimensions() { return dimMapper.selectList(null); }
    public List<BudgetParam> listParams() { return paramMapper.selectList(null); }
    public List<BudgetControlRule> listRules() { return ruleMapper.selectList(null); }

    @Transactional
    public BudgetSubject saveSubject(BudgetSubject s) {
        if (s.getId() != null) { s.setUpdateTime(LocalDateTime.now()); subjectMapper.updateById(s); return s; }
        s.setCreateTime(LocalDateTime.now());
        subjectMapper.insert(s);
        return s;
    }
    public boolean updateSubject(BudgetSubject s) { s.setUpdateTime(LocalDateTime.now()); return subjectMapper.updateById(s) > 0; }
    public boolean deleteSubject(Long id) { return subjectMapper.deleteById(id) > 0; }

    @Transactional
    public BudgetDimension saveDimension(BudgetDimension d) {
        if (d.getId() != null) { d.setUpdateTime(LocalDateTime.now()); dimMapper.updateById(d); return d; }
        d.setCreateTime(LocalDateTime.now());
        dimMapper.insert(d);
        return d;
    }
    public boolean updateDimension(BudgetDimension d) { d.setUpdateTime(LocalDateTime.now()); return dimMapper.updateById(d) > 0; }
    public boolean deleteDimension(Long id) { return dimMapper.deleteById(id) > 0; }

    @Transactional
    public BudgetParam saveParam(BudgetParam p) {
        if (p.getId() != null) { p.setUpdateTime(LocalDateTime.now()); paramMapper.updateById(p); return p; }
        p.setCreateTime(LocalDateTime.now());
        paramMapper.insert(p);
        return p;
    }
    public boolean updateParam(BudgetParam p) { p.setUpdateTime(LocalDateTime.now()); return paramMapper.updateById(p) > 0; }
    public boolean deleteParam(Long id) { return paramMapper.deleteById(id) > 0; }

    @Transactional
    public BudgetControlRule saveRule(BudgetControlRule r) {
        if (r.getId() != null) { r.setUpdateTime(LocalDateTime.now()); ruleMapper.updateById(r); return r; }
        r.setCreateTime(LocalDateTime.now());
        ruleMapper.insert(r);
        return r;
    }
    public boolean updateRule(BudgetControlRule r) { r.setUpdateTime(LocalDateTime.now()); return ruleMapper.updateById(r) > 0; }
    public boolean deleteRule(Long id) { return ruleMapper.deleteById(id) > 0; }

    // ==================== 三上三下编制 ====================
    public List<BudgetVersion> listVersions(Integer year) {
        LambdaQueryWrapper<BudgetVersion> w = new LambdaQueryWrapper<>();
        if (year != null) w.eq(BudgetVersion::getBudgetYear, year);
        return versionMapper.selectList(w);
    }

    @Transactional
    public BudgetVersion saveVersion(BudgetVersion v) {
        if (v.getId() != null) { v.setUpdateTime(LocalDateTime.now()); versionMapper.updateById(v); return v; }
        v.setCreateTime(LocalDateTime.now());
        versionMapper.insert(v);
        return v;
    }
    public boolean updateVersion(BudgetVersion v) { v.setUpdateTime(LocalDateTime.now()); return versionMapper.updateById(v) > 0; }
    public boolean deleteVersion(Long id) { return versionMapper.deleteById(id) > 0; }

    public List<AnnualBudget> listAnnualBudgets(Integer year) {
        LambdaQueryWrapper<AnnualBudget> w = new LambdaQueryWrapper<>();
        if (year != null) w.eq(AnnualBudget::getBudgetYear, year);
        return baseMapper.selectList(w);
    }

    @Transactional
    public AnnualBudget saveAnnualBudget(AnnualBudget b) {
        calcAnnual(b);
        if (b.getId() != null) { b.setUpdateTime(LocalDateTime.now()); baseMapper.updateById(b); return b; }
        b.setCreateTime(LocalDateTime.now());
        baseMapper.insert(b);
        return b;
    }

    private void calcAnnual(AnnualBudget b) {
        if (b.getBudgetAmount() != null) {
            b.setUsedAmount(b.getUsedAmount() == null ? BigDecimal.ZERO : b.getUsedAmount());
            b.setOccupiedAmount(b.getOccupiedAmount() == null ? BigDecimal.ZERO : b.getOccupiedAmount());
            b.setRemainingAmount(b.getBudgetAmount().subtract(b.getUsedAmount()).subtract(b.getOccupiedAmount()));
        }
    }

    public boolean updateAnnualBudget(AnnualBudget b) {
        calcAnnual(b);
        b.setUpdateTime(LocalDateTime.now());
        return baseMapper.updateById(b) > 0;
    }

    public boolean deleteAnnualBudget(Long id) { return baseMapper.deleteById(id) > 0; }

    // ==================== 项目四算 ====================
    public List<ProjectBudget> listProjects() { return projectMapper.selectList(null); }

    @Transactional
    public ProjectBudget saveProject(ProjectBudget p) {
        calcProject(p);
        if (p.getId() != null) { p.setUpdateTime(LocalDateTime.now()); projectMapper.updateById(p); return p; }
        p.setCreateTime(LocalDateTime.now());
        projectMapper.insert(p);
        return p;
    }

    private void calcProject(ProjectBudget p) {
        // 决算-预算差异
        if (p.getFinalAmount() != null && p.getBudgetAmount() != null) {
            p.setVarianceAmount(p.getFinalAmount().subtract(p.getBudgetAmount()));
            if (p.getBudgetAmount().compareTo(BigDecimal.ZERO) != 0) {
                p.setVarianceRate(p.getVarianceAmount().divide(p.getBudgetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            }
        }
    }

    public boolean updateProject(ProjectBudget p) {
        calcProject(p);
        p.setUpdateTime(LocalDateTime.now());
        return projectMapper.updateById(p) > 0;
    }

    public boolean deleteProject(Long id) { return projectMapper.deleteById(id) > 0; }

    // ==================== 实时控制引擎 ====================
    // 预算占用: 预占用/正式占用/释放
    @Transactional
    public BudgetOccupation occupy(BudgetOccupation occ) {
        if (occ.getAmount() == null) throw new BizException("占用金额不能为空");
        // 带id的编辑请求仅更新台账字段，不重复增减预算余额
        if (occ.getId() != null) {
            occ.setUpdateTime(LocalDateTime.now());
            occupationMapper.updateById(occ);
            return occ;
        }
        AnnualBudget budget = baseMapper.selectById(occ.getRelatedBudgetId());
        if (budget == null) throw new BizException("预算项不存在");
        budget.setUsedAmount(budget.getUsedAmount() == null ? BigDecimal.ZERO : budget.getUsedAmount());
        budget.setOccupiedAmount(budget.getOccupiedAmount() == null ? BigDecimal.ZERO : budget.getOccupiedAmount());
        // 检查控制策略
        BudgetControlRule rule = ruleMapper.selectOne(
                new LambdaQueryWrapper<BudgetControlRule>().eq(BudgetControlRule::getBudgetDim, budget.getBudgetDim()));
        if (rule != null && "严格拦截".equals(rule.getControlStrategy())) {
            BigDecimal available = budget.getRemainingAmount() == null ? BigDecimal.ZERO : budget.getRemainingAmount();
            if (available.compareTo(occ.getAmount()) < 0) {
                throw new BizException("预算不足，可用余额:" + available + "，申请占用:" + occ.getAmount());
            }
        }
        // 三级占用处理
        if ("预占用".equals(occ.getOccupationType())) {
            budget.setOccupiedAmount(budget.getOccupiedAmount().add(occ.getAmount()));
            budget.setRemainingAmount(budget.getBudgetAmount().subtract(budget.getUsedAmount()).subtract(budget.getOccupiedAmount()));
        } else if ("正式占用".equals(occ.getOccupationType())) {
            budget.setUsedAmount(budget.getUsedAmount().add(occ.getAmount()));
            budget.setOccupiedAmount(budget.getOccupiedAmount().subtract(occ.getAmount()));
            budget.setRemainingAmount(budget.getBudgetAmount().subtract(budget.getUsedAmount()).subtract(budget.getOccupiedAmount()));
        } else if ("释放".equals(occ.getOccupationType())) {
            budget.setOccupiedAmount(budget.getOccupiedAmount().subtract(occ.getAmount()));
            budget.setRemainingAmount(budget.getBudgetAmount().subtract(budget.getUsedAmount()).subtract(budget.getOccupiedAmount()));
            occ.setReleaseTime(LocalDateTime.now());
        }
        budget.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(budget);
        occ.setOccupationTime(LocalDateTime.now());
        occ.setCreateTime(LocalDateTime.now());
        occupationMapper.insert(occ);
        return occ;
    }

    public boolean updateOccupation(BudgetOccupation occ) {
        occ.setUpdateTime(LocalDateTime.now());
        return occupationMapper.updateById(occ) > 0;
    }

    public boolean deleteOccupation(Long id) { return occupationMapper.deleteById(id) > 0; }

    public List<BudgetOccupation> listOccupations(Long budgetId) {
        LambdaQueryWrapper<BudgetOccupation> w = new LambdaQueryWrapper<>();
        if (budgetId != null) w.eq(BudgetOccupation::getRelatedBudgetId, budgetId);
        return occupationMapper.selectList(w);
    }

    // 预算执行率 = 实际/预算×100%
    public BigDecimal execRate(Long budgetId) {
        AnnualBudget b = baseMapper.selectById(budgetId);
        if (b == null || b.getBudgetAmount() == null || b.getBudgetAmount().compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        return (b.getUsedAmount() == null ? BigDecimal.ZERO : b.getUsedAmount())
                .divide(b.getBudgetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== 调整追加调剂 ====================
    public List<BudgetChange> listChanges() { return changeMapper.selectList(null); }

    @Transactional
    public BudgetChange saveChange(BudgetChange c) {
        calcChange(c);
        if (c.getId() != null) { c.setUpdateTime(LocalDateTime.now()); changeMapper.updateById(c); return c; }
        c.setCreateTime(LocalDateTime.now());
        changeMapper.insert(c);
        return c;
    }

    private void calcChange(BudgetChange c) {
        if (c.getBeforeAmount() != null && c.getChangeAmount() != null) {
            c.setAfterAmount(c.getBeforeAmount().add(c.getChangeAmount()));
        }
    }

    public boolean updateChange(BudgetChange c) {
        calcChange(c);
        c.setUpdateTime(LocalDateTime.now());
        return changeMapper.updateById(c) > 0;
    }

    public boolean deleteChange(Long id) { return changeMapper.deleteById(id) > 0; }

    // ==================== 月度滚动预测 ====================
    public List<RollingForecast> listForecasts() { return forecastMapper.selectList(null); }

    @Transactional
    public RollingForecast saveForecast(RollingForecast f) {
        calcForecast(f);
        if (f.getId() != null) { f.setUpdateTime(LocalDateTime.now()); forecastMapper.updateById(f); return f; }
        f.setCreateTime(LocalDateTime.now());
        forecastMapper.insert(f);
        return f;
    }

    private void calcForecast(RollingForecast f) {
        if (f.getForecastAmount() != null && f.getActualAmount() != null) {
            f.setVarianceAmount(f.getActualAmount().subtract(f.getForecastAmount()));
        }
    }

    public boolean updateForecast(RollingForecast f) {
        calcForecast(f);
        f.setUpdateTime(LocalDateTime.now());
        return forecastMapper.updateById(f) > 0;
    }

    public boolean deleteForecast(Long id) { return forecastMapper.deleteById(id) > 0; }

    // ==================== 差异分析 ====================
    public List<BudgetVariance> listVariances() { return varianceMapper.selectList(null); }

    @Transactional
    public BudgetVariance saveVariance(BudgetVariance v) {
        calcVariance(v);
        if (v.getId() != null) { v.setUpdateTime(LocalDateTime.now()); varianceMapper.updateById(v); return v; }
        v.setCreateTime(LocalDateTime.now());
        varianceMapper.insert(v);
        return v;
    }

    private void calcVariance(BudgetVariance v) {
        if (v.getBudgetAmount() != null && v.getActualAmount() != null) {
            v.setVarianceAmount(v.getActualAmount().subtract(v.getBudgetAmount()));
            if (v.getBudgetAmount().compareTo(BigDecimal.ZERO) != 0) {
                v.setVarianceRate(v.getVarianceAmount().divide(v.getBudgetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            }
        }
    }

    public boolean updateVariance(BudgetVariance v) {
        calcVariance(v);
        v.setUpdateTime(LocalDateTime.now());
        return varianceMapper.updateById(v) > 0;
    }

    public boolean deleteVariance(Long id) { return varianceMapper.deleteById(id) > 0; }

    // ==================== 内控审计 ====================
    public List<BudgetAudit> listAudits() { return auditMapper.selectList(null); }

    @Transactional
    public BudgetAudit saveAudit(BudgetAudit a) {
        if (a.getId() != null) { a.setUpdateTime(LocalDateTime.now()); auditMapper.updateById(a); return a; }
        a.setCreateTime(LocalDateTime.now());
        auditMapper.insert(a);
        return a;
    }

    public boolean updateAudit(BudgetAudit a) { a.setUpdateTime(LocalDateTime.now()); return auditMapper.updateById(a) > 0; }
    public boolean deleteAudit(Long id) { return auditMapper.deleteById(id) > 0; }

    // 预算总览
    public Map<String, Object> overview(Integer year) {
        List<AnnualBudget> budgets = listAnnualBudgets(year);
        BigDecimal total = budgets.stream().map(b -> b.getBudgetAmount() == null ? BigDecimal.ZERO : b.getBudgetAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal used = budgets.stream().map(b -> b.getUsedAmount() == null ? BigDecimal.ZERO : b.getUsedAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> r = new HashMap<>();
        r.put("totalBudget", total);
        r.put("usedAmount", used);
        r.put("execRate", total.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : used.divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        r.put("budgetCount", budgets.size());
        return r;
    }
}
