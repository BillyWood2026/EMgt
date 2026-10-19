package com.example.strategymgt.blm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.strategymgt.blm.entity.*;
import com.example.strategymgt.blm.mapper.*;
import com.example.strategymgt.common.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BlmService extends ServiceImpl<BlmSpPlanMapper, BlmSpPlan> {

    private final BlmGapAnalysisMapper gapMapper;
    private final BlmMarketInsightMapper insightMapper;
    private final BlmStrategicIntentMapper intentMapper;
    private final BlmInnovationFocusMapper innovationMapper;
    private final BlmBusinessDesignMapper designMapper;
    private final BlmInvestmentPortfolioMapper portfolioMapper;

    public BlmService(BlmGapAnalysisMapper gapMapper, BlmMarketInsightMapper insightMapper,
                      BlmStrategicIntentMapper intentMapper, BlmInnovationFocusMapper innovationMapper,
                      BlmBusinessDesignMapper designMapper, BlmInvestmentPortfolioMapper portfolioMapper) {
        this.gapMapper = gapMapper;
        this.insightMapper = insightMapper;
        this.intentMapper = intentMapper;
        this.innovationMapper = innovationMapper;
        this.designMapper = designMapper;
        this.portfolioMapper = portfolioMapper;
    }

    // ==================== 差距分析 ====================
    public List<BlmGapAnalysis> listGaps(Integer planYear) {
        LambdaQueryWrapper<BlmGapAnalysis> w = new LambdaQueryWrapper<>();
        if (planYear != null) w.eq(BlmGapAnalysis::getPlanYear, planYear);
        w.orderByDesc(BlmGapAnalysis::getCreateTime);
        return gapMapper.selectList(w);
    }

    @Transactional
    public BlmGapAnalysis saveGap(BlmGapAnalysis gap) {
        // 差距数值自动计算: 现状-目标
        if (gap.getCurrentValue() != null && gap.getTargetValue() != null) {
            gap.setGapValue(gap.getCurrentValue().subtract(gap.getTargetValue()));
        }
        // 差距率: 差距数值/标杆数据×100%
        if (gap.getGapValue() != null && gap.getBenchmarkValue() != null
                && gap.getBenchmarkValue().compareTo(BigDecimal.ZERO) != 0) {
            gap.setGapRate(gap.getGapValue()
                    .divide(gap.getBenchmarkValue(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        }
        if (gap.getId() != null) {
            gap.setUpdateTime(LocalDateTime.now());
            gapMapper.updateById(gap);
            return gap;
        }
        if (gap.getGapCode() == null) {
            gap.setGapCode("GAP-" + (gap.getPlanYear() != null ? gap.getPlanYear() : "") + "-" + System.currentTimeMillis() % 100000);
        }
        gap.setCreateTime(LocalDateTime.now());
        gap.setStatus(gap.getStatus() == null ? "草稿" : gap.getStatus());
        gapMapper.insert(gap);
        return gap;
    }

    public boolean updateGap(BlmGapAnalysis gap) {
        if (gap.getCurrentValue() != null && gap.getTargetValue() != null) {
            gap.setGapValue(gap.getCurrentValue().subtract(gap.getTargetValue()));
        }
        if (gap.getGapValue() != null && gap.getBenchmarkValue() != null
                && gap.getBenchmarkValue().compareTo(BigDecimal.ZERO) != 0) {
            gap.setGapRate(gap.getGapValue().divide(gap.getBenchmarkValue(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        }
        gap.setUpdateTime(LocalDateTime.now());
        return gapMapper.updateById(gap) > 0;
    }

    public boolean deleteGap(Long id) {
        return gapMapper.deleteById(id) > 0;
    }

    // 差距等级评估: 基于差距率、优先级
    public String evaluateGapLevel(BlmGapAnalysis gap) {
        BigDecimal rate = gap.getGapRate() == null ? BigDecimal.ZERO : gap.getGapRate().abs();
        String priority = gap.getPriority() == null ? "中" : gap.getPriority();
        if (rate.compareTo(new BigDecimal("30")) >= 0 || "高".equals(priority) && rate.compareTo(new BigDecimal("15")) >= 0)
            return "重大";
        if (rate.compareTo(new BigDecimal("15")) >= 0) return "较大";
        if (rate.compareTo(new BigDecimal("5")) >= 0) return "一般";
        return "轻微";
    }

    // ==================== 市场洞察 ====================
    public List<BlmMarketInsight> listInsights(Integer planYear) {
        LambdaQueryWrapper<BlmMarketInsight> w = new LambdaQueryWrapper<>();
        if (planYear != null) w.eq(BlmMarketInsight::getPlanYear, planYear);
        return insightMapper.selectList(w);
    }

    @Transactional
    public BlmMarketInsight saveInsight(BlmMarketInsight insight) {
        // 市场机会评分算法: 基于增速
        if (insight.getGrowthRate() != null) {
            BigDecimal score = insight.getGrowthRate().multiply(new BigDecimal("0.6"));
            insight.setOpportunityScore(score.setScale(2, RoundingMode.HALF_UP));
        }
        if (insight.getId() != null) {
            insight.setUpdateTime(LocalDateTime.now());
            insightMapper.updateById(insight);
            return insight;
        }
        insight.setCreateTime(LocalDateTime.now());
        insightMapper.insert(insight);
        return insight;
    }

    public boolean updateInsight(BlmMarketInsight insight) {
        if (insight.getGrowthRate() != null) {
            insight.setOpportunityScore(insight.getGrowthRate().multiply(new BigDecimal("0.6"))
                    .setScale(2, RoundingMode.HALF_UP));
        }
        insight.setUpdateTime(LocalDateTime.now());
        return insightMapper.updateById(insight) > 0;
    }

    public boolean deleteInsight(Long id) {
        return insightMapper.deleteById(id) > 0;
    }

    // ==================== 战略意图 ====================
    public List<BlmStrategicIntent> listIntents() {
        return intentMapper.selectList(null);
    }

    @Transactional
    public BlmStrategicIntent saveIntent(BlmStrategicIntent intent) {
        if (intent.getId() != null) {
            intent.setUpdateTime(LocalDateTime.now());
            intentMapper.updateById(intent);
            return intent;
        }
        intent.setCreateTime(LocalDateTime.now());
        intentMapper.insert(intent);
        return intent;
    }

    public boolean updateIntent(BlmStrategicIntent intent) {
        intent.setUpdateTime(LocalDateTime.now());
        return intentMapper.updateById(intent) > 0;
    }

    public boolean deleteIntent(Long id) {
        return intentMapper.deleteById(id) > 0;
    }

    // ==================== 创新焦点 ====================
    public List<BlmInnovationFocus> listInnovations() {
        return innovationMapper.selectList(null);
    }

    @Transactional
    public BlmInnovationFocus saveInnovation(BlmInnovationFocus inn) {
        if (inn.getId() != null) {
            inn.setUpdateTime(LocalDateTime.now());
            innovationMapper.updateById(inn);
            return inn;
        }
        inn.setCreateTime(LocalDateTime.now());
        innovationMapper.insert(inn);
        return inn;
    }

    public boolean updateInnovation(BlmInnovationFocus inn) {
        inn.setUpdateTime(LocalDateTime.now());
        return innovationMapper.updateById(inn) > 0;
    }

    public boolean deleteInnovation(Long id) {
        return innovationMapper.deleteById(id) > 0;
    }

    // ==================== 业务设计 ====================
    public List<BlmBusinessDesign> listDesigns() {
        return designMapper.selectList(null);
    }

    @Transactional
    public BlmBusinessDesign saveDesign(BlmBusinessDesign design) {
        if (design.getId() != null) {
            design.setUpdateTime(LocalDateTime.now());
            designMapper.updateById(design);
            return design;
        }
        design.setCreateTime(LocalDateTime.now());
        designMapper.insert(design);
        return design;
    }

    public boolean updateDesign(BlmBusinessDesign design) {
        design.setUpdateTime(LocalDateTime.now());
        return designMapper.updateById(design) > 0;
    }

    public boolean deleteDesign(Long id) {
        return designMapper.deleteById(id) > 0;
    }

    // ==================== SP中长期战略规划 ====================
    public List<BlmSpPlan> listSpPlans() {
        return baseMapper.selectList(new LambdaQueryWrapper<BlmSpPlan>().orderByDesc(BlmSpPlan::getCreateTime));
    }

    @Transactional
    public BlmSpPlan saveSpPlan(BlmSpPlan plan) {
        if (plan.getId() != null) {
            plan.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(plan);
            return plan;
        }
        if (plan.getSpCode() == null) {
            plan.setSpCode("SP-" + LocalDateTime.now().getYear() + "-" + System.currentTimeMillis() % 10000);
        }
        plan.setBaselineVersion(plan.getBaselineVersion() == null ? "V1" : plan.getBaselineVersion());
        plan.setStatus(plan.getStatus() == null ? "草稿" : plan.getStatus());
        plan.setCreateTime(LocalDateTime.now());
        baseMapper.insert(plan);
        return plan;
    }

    public boolean updateSpPlan(BlmSpPlan plan) {
        plan.setUpdateTime(LocalDateTime.now());
        return baseMapper.updateById(plan) > 0;
    }

    public boolean deleteSpPlan(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    // SP战略对齐校验算法: 校验创新焦点、业务设计、投资组合与战略意图的一致性
    public BigDecimal checkSpAlignment(Long spId) {
        long innovationCount = innovationMapper.selectCount(null);
        long designCount = designMapper.selectCount(null);
        long portfolioCount = portfolioMapper.selectCount(null);
        // 简化对齐评分: 各要素齐全度
        BigDecimal score = BigDecimal.ZERO;
        if (innovationCount > 0) score = score.add(new BigDecimal("30"));
        if (designCount > 0) score = score.add(new BigDecimal("30"));
        if (portfolioCount > 0) score = score.add(new BigDecimal("20"));
        if (intentMapper.selectCount(null) > 0) score = score.add(new BigDecimal("20"));
        return score;
    }

    // SP发布并锁定基线
    @Transactional
    public BlmSpPlan publishSp(Long id) {
        BlmSpPlan plan = baseMapper.selectById(id);
        if (plan == null) throw new BizException("SP方案不存在");
        plan.setStatus("已发布");
        plan.setBaselineLockTime(LocalDateTime.now());
        baseMapper.updateById(plan);
        return plan;
    }

    // ==================== 战略投资组合 ====================
    public List<BlmInvestmentPortfolio> listPortfolios(Long spId) {
        LambdaQueryWrapper<BlmInvestmentPortfolio> w = new LambdaQueryWrapper<>();
        if (spId != null) w.eq(BlmInvestmentPortfolio::getSpId, spId);
        return portfolioMapper.selectList(w);
    }

    @Transactional
    public BlmInvestmentPortfolio savePortfolio(BlmInvestmentPortfolio p) {
        if (p.getId() != null) {
            p.setUpdateTime(LocalDateTime.now());
            portfolioMapper.updateById(p);
            return p;
        }
        p.setCreateTime(LocalDateTime.now());
        portfolioMapper.insert(p);
        return p;
    }

    public boolean updatePortfolio(BlmInvestmentPortfolio p) {
        p.setUpdateTime(LocalDateTime.now());
        return portfolioMapper.updateById(p) > 0;
    }

    public boolean deletePortfolio(Long id) {
        return portfolioMapper.deleteById(id) > 0;
    }

    // SP任务优先级排序: 基于战略权重、投资等级、风险等级
    public List<BlmInvestmentPortfolio> rankPortfolios() {
        List<BlmInvestmentPortfolio> list = portfolioMapper.selectList(null);
        list.sort((a, b) -> {
            int ra = "高".equals(a.getPriority()) ? 3 : "中".equals(a.getPriority()) ? 2 : 1;
            int rb = "高".equals(b.getPriority()) ? 3 : "中".equals(b.getPriority()) ? 2 : 1;
            return Integer.compare(rb, ra);
        });
        return list;
    }
}
