package com.example.strategymgt.dste.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.strategymgt.dste.entity.*;
import com.example.strategymgt.dste.mapper.*;
import com.example.strategymgt.common.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DsteService extends ServiceImpl<DsteKeyTaskMapper, DsteKeyTask> {

    private final DsteGoalBreakdownMapper goalMapper;
    private final DsteCsfMapper csfMapper;
    private final DsteKpiMapper kpiMapper;
    private final DsteResourceMatchMapper resourceMapper;
    private final DsteResponsibilityLedgerMapper respMapper;
    private final DsteIbpBudgetMapper ibpMapper;
    private final DsteTaskProgressMapper progressMapper;
    private final DsteKpiMonitorMapper kpiMonitorMapper;
    private final DsteRiskWarningMapper riskMapper;
    private final DsteSupervisionMapper supervisionMapper;
    private final DsteReviewMapper reviewMapper;
    private final DsteStrategicIterationMapper iterationMapper;

    public DsteService(DsteGoalBreakdownMapper goalMapper, DsteCsfMapper csfMapper,
                       DsteKpiMapper kpiMapper, DsteResourceMatchMapper resourceMapper,
                       DsteResponsibilityLedgerMapper respMapper, DsteIbpBudgetMapper ibpMapper,
                       DsteTaskProgressMapper progressMapper, DsteKpiMonitorMapper kpiMonitorMapper,
                       DsteRiskWarningMapper riskMapper, DsteSupervisionMapper supervisionMapper,
                       DsteReviewMapper reviewMapper, DsteStrategicIterationMapper iterationMapper) {
        this.goalMapper = goalMapper;
        this.csfMapper = csfMapper;
        this.kpiMapper = kpiMapper;
        this.resourceMapper = resourceMapper;
        this.respMapper = respMapper;
        this.ibpMapper = ibpMapper;
        this.progressMapper = progressMapper;
        this.kpiMonitorMapper = kpiMonitorMapper;
        this.riskMapper = riskMapper;
        this.supervisionMapper = supervisionMapper;
        this.reviewMapper = reviewMapper;
        this.iterationMapper = iterationMapper;
    }

    // ==================== 战略目标拆解 ====================
    public List<DsteGoalBreakdown> listGoals(Long spId) {
        LambdaQueryWrapper<DsteGoalBreakdown> w = new LambdaQueryWrapper<>();
        if (spId != null) w.eq(DsteGoalBreakdown::getRelatedSpId, spId);
        return goalMapper.selectList(w);
    }

    @Transactional
    public DsteGoalBreakdown saveGoal(DsteGoalBreakdown goal) {
        if (goal.getId() != null) {
            goal.setUpdateTime(LocalDateTime.now());
            goalMapper.updateById(goal);
            return goal;
        }
        goal.setCreateTime(LocalDateTime.now());
        goalMapper.insert(goal);
        return goal;
    }

    public boolean updateGoal(DsteGoalBreakdown goal) {
        goal.setUpdateTime(LocalDateTime.now());
        return goalMapper.updateById(goal) > 0;
    }

    public boolean deleteGoal(Long id) {
        return goalMapper.deleteById(id) > 0;
    }

    // 目标对齐校验: 子目标权重合计=100%
    public Map<String, Object> checkGoalAlignment(Long parentGoalId) {
        List<DsteGoalBreakdown> children = goalMapper.selectList(
                new LambdaQueryWrapper<DsteGoalBreakdown>().eq(DsteGoalBreakdown::getParentGoalId, parentGoalId));
        BigDecimal totalWeight = children.stream()
                .map(g -> g.getGoalWeight() == null ? BigDecimal.ZERO : g.getGoalWeight())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> r = new HashMap<>();
        r.put("totalWeight", totalWeight);
        r.put("isAligned", totalWeight.compareTo(new BigDecimal("100")) == 0);
        return r;
    }

    // ==================== CSF ====================
    public List<DsteCsf> listCsfs(Long goalId) {
        LambdaQueryWrapper<DsteCsf> w = new LambdaQueryWrapper<>();
        if (goalId != null) w.eq(DsteCsf::getRelatedGoalId, goalId);
        return csfMapper.selectList(w);
    }

    @Transactional
    public DsteCsf saveCsf(DsteCsf csf) {
        if (csf.getId() != null) {
            csf.setUpdateTime(LocalDateTime.now());
            csfMapper.updateById(csf);
            return csf;
        }
        csf.setCreateTime(LocalDateTime.now());
        csfMapper.insert(csf);
        return csf;
    }

    public boolean updateCsf(DsteCsf csf) {
        csf.setUpdateTime(LocalDateTime.now());
        return csfMapper.updateById(csf) > 0;
    }

    public boolean deleteCsf(Long id) {
        return csfMapper.deleteById(id) > 0;
    }

    // ==================== KPI指标库 ====================
    public List<DsteKpi> listKpis() {
        return kpiMapper.selectList(null);
    }

    @Transactional
    public DsteKpi saveKpi(DsteKpi kpi) {
        if (kpi.getId() != null) {
            kpi.setUpdateTime(LocalDateTime.now());
            kpiMapper.updateById(kpi);
            return kpi;
        }
        kpi.setCreateTime(LocalDateTime.now());
        kpi.setStatus(kpi.getStatus() == null ? "启用" : kpi.getStatus());
        kpiMapper.insert(kpi);
        return kpi;
    }

    public boolean updateKpi(DsteKpi kpi) {
        kpi.setUpdateTime(LocalDateTime.now());
        return kpiMapper.updateById(kpi) > 0;
    }

    public boolean deleteKpi(Long id) {
        return kpiMapper.deleteById(id) > 0;
    }

    // KPI维度占比统计（kpiDim为空归入"未分类"，避免groupingBy对null键抛NPE）
    public Map<String, Long> kpiDimStats() {
        return kpiMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(
                        k -> k.getKpiDim() == null ? "未分类" : k.getKpiDim(),
                        Collectors.counting()));
    }

    // ==================== SP/BP重点任务 ====================
    public List<DsteKeyTask> listTasks(String taskLevel) {
        LambdaQueryWrapper<DsteKeyTask> w = new LambdaQueryWrapper<>();
        if (taskLevel != null) w.eq(DsteKeyTask::getTaskLevel, taskLevel);
        return baseMapper.selectList(w);
    }

    @Transactional
    public DsteKeyTask saveTask(DsteKeyTask task) {
        if (task.getId() != null) {
            task.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(task);
            return task;
        }
        task.setCreateTime(LocalDateTime.now());
        task.setTaskStatus(task.getTaskStatus() == null ? "未启动" : task.getTaskStatus());
        baseMapper.insert(task);
        return task;
    }

    public boolean updateTask(DsteKeyTask task) {
        task.setUpdateTime(LocalDateTime.now());
        return baseMapper.updateById(task) > 0;
    }

    public boolean deleteTask(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    // 任务对齐度算法: 基于战略权重、承接组织层级、关联KPI数
    public BigDecimal calcTaskAlignment(DsteKeyTask task) {
        BigDecimal weightScore = task.getStrategyWeight() == null ? BigDecimal.ZERO : task.getStrategyWeight();
        BigDecimal orgScore = "公司级".equals(task.getPrimaryOrgLevel()) ? new BigDecimal("30")
                : "事业部级".equals(task.getPrimaryOrgLevel()) ? new BigDecimal("25") : new BigDecimal("20");
        return weightScore.multiply(new BigDecimal("0.7")).add(orgScore).setScale(2, RoundingMode.HALF_UP);
    }

    // 任务状态自动判定: 进度+逾期
    @Transactional
    public void refreshTaskStatus() {
        List<DsteKeyTask> tasks = baseMapper.selectList(null);
        for (DsteKeyTask t : tasks) {
            if (t.getPlanEndTime() != null && LocalDate.now().isAfter(t.getPlanEndTime())
                    && (t.getProgress() == null || t.getProgress().compareTo(new BigDecimal("100")) < 0)) {
                long days = ChronoUnit.DAYS.between(t.getPlanEndTime(), LocalDate.now());
                t.setOverdueDays((int) days);
                t.setTaskStatus("逾期");
            } else if (t.getProgress() != null && t.getProgress().compareTo(new BigDecimal("100")) >= 0) {
                t.setTaskStatus("已完成");
                t.setActualEndTime(LocalDate.now());
            }
            baseMapper.updateById(t);
        }
    }

    // ==================== 资源匹配 ====================
    public List<DsteResourceMatch> listResources(Long taskId) {
        LambdaQueryWrapper<DsteResourceMatch> w = new LambdaQueryWrapper<>();
        if (taskId != null) w.eq(DsteResourceMatch::getRelatedTaskId, taskId);
        return resourceMapper.selectList(w);
    }

    @Transactional
    public DsteResourceMatch saveResource(DsteResourceMatch r) {
        calcResource(r);
        if (r.getId() != null) {
            r.setUpdateTime(LocalDateTime.now());
            resourceMapper.updateById(r);
            return r;
        }
        r.setCreateTime(LocalDateTime.now());
        resourceMapper.insert(r);
        return r;
    }

    private void calcResource(DsteResourceMatch r) {
        // 自动计算缺口
        if (r.getRequiredAmount() != null && r.getMatchedAmount() != null) {
            r.setGapAmount(r.getRequiredAmount().subtract(r.getMatchedAmount()));
        }
        // 匹配状态判定
        if (r.getGapAmount() != null) {
            if (r.getGapAmount().compareTo(BigDecimal.ZERO) <= 0) r.setMatchStatus("全部匹配");
            else if (r.getMatchedAmount() != null && r.getMatchedAmount().compareTo(BigDecimal.ZERO) > 0) r.setMatchStatus("部分匹配");
            else r.setMatchStatus("未匹配");
        }
    }

    public boolean updateResource(DsteResourceMatch r) {
        calcResource(r);
        r.setUpdateTime(LocalDateTime.now());
        return resourceMapper.updateById(r) > 0;
    }

    public boolean deleteResource(Long id) {
        return resourceMapper.deleteById(id) > 0;
    }

    // ==================== 责任体系台账 ====================
    public List<DsteResponsibilityLedger> listRespLedgers() {
        return respMapper.selectList(null);
    }

    @Transactional
    public DsteResponsibilityLedger saveRespLedger(DsteResponsibilityLedger r) {
        if (r.getId() != null) {
            r.setUpdateTime(LocalDateTime.now());
            respMapper.updateById(r);
            return r;
        }
        r.setCreateTime(LocalDateTime.now());
        respMapper.insert(r);
        return r;
    }

    public boolean updateRespLedger(DsteResponsibilityLedger r) {
        r.setUpdateTime(LocalDateTime.now());
        return respMapper.updateById(r) > 0;
    }

    public boolean deleteRespLedger(Long id) {
        return respMapper.deleteById(id) > 0;
    }

    // ==================== IBP预算联动 ====================
    public List<DsteIbpBudget> listIbpBudgets() {
        return ibpMapper.selectList(null);
    }

    @Transactional
    public DsteIbpBudget saveIbpBudget(DsteIbpBudget b) {
        calcIbp(b);
        if (b.getId() != null) {
            b.setUpdateTime(LocalDateTime.now());
            ibpMapper.updateById(b);
            return b;
        }
        b.setCreateTime(LocalDateTime.now());
        ibpMapper.insert(b);
        return b;
    }

    private void calcIbp(DsteIbpBudget b) {
        if (b.getBudgetAmount() != null) {
            b.setUsedAmount(b.getUsedAmount() == null ? BigDecimal.ZERO : b.getUsedAmount());
            b.setRemainingAmount(b.getBudgetAmount().subtract(b.getUsedAmount()));
        }
    }

    public boolean updateIbpBudget(DsteIbpBudget b) {
        calcIbp(b);
        b.setUpdateTime(LocalDateTime.now());
        return ibpMapper.updateById(b) > 0;
    }

    public boolean deleteIbpBudget(Long id) {
        return ibpMapper.deleteById(id) > 0;
    }

    // ==================== 任务进度监控 ====================
    public List<DsteTaskProgress> listProgress(Long taskId) {
        LambdaQueryWrapper<DsteTaskProgress> w = new LambdaQueryWrapper<>();
        if (taskId != null) w.eq(DsteTaskProgress::getTaskId, taskId);
        return progressMapper.selectList(w);
    }

    @Transactional
    public DsteTaskProgress saveProgress(DsteTaskProgress p) {
        calcProgress(p);
        if (p.getId() != null) {
            p.setUpdateTime(LocalDateTime.now());
            progressMapper.updateById(p);
        } else {
            p.setCreateTime(LocalDateTime.now());
            progressMapper.insert(p);
        }
        // 同步更新任务表进度
        if (p.getTaskId() != null && p.getActualProgress() != null) {
            DsteKeyTask task = baseMapper.selectById(p.getTaskId());
            if (task != null) {
                task.setProgress(p.getActualProgress());
                baseMapper.updateById(task);
            }
        }
        return p;
    }

    private void calcProgress(DsteTaskProgress p) {
        // 进度偏差 = 实际 - 计划
        if (p.getActualProgress() != null && p.getPlannedProgress() != null) {
            p.setProgressDeviation(p.getActualProgress().subtract(p.getPlannedProgress()));
        }
        // 进度状态判定
        if (p.getProgressDeviation() != null) {
            BigDecimal dev = p.getProgressDeviation();
            if (dev.compareTo(BigDecimal.ZERO) >= 0) p.setProgressStatus("正常");
            else if (dev.compareTo(new BigDecimal("-10")) >= 0) p.setProgressStatus("预警");
            else if (dev.compareTo(new BigDecimal("-30")) >= 0) p.setProgressStatus("滞后");
            else p.setProgressStatus("严重滞后");
        }
    }

    public boolean updateProgress(DsteTaskProgress p) {
        calcProgress(p);
        p.setUpdateTime(LocalDateTime.now());
        return progressMapper.updateById(p) > 0;
    }

    public boolean deleteProgress(Long id) {
        return progressMapper.deleteById(id) > 0;
    }

    // ==================== KPI实时监控 ====================
    public List<DsteKpiMonitor> listKpiMonitors(Long kpiId) {
        LambdaQueryWrapper<DsteKpiMonitor> w = new LambdaQueryWrapper<>();
        if (kpiId != null) w.eq(DsteKpiMonitor::getKpiId, kpiId);
        return kpiMonitorMapper.selectList(w);
    }

    @Transactional
    public DsteKpiMonitor saveKpiMonitor(DsteKpiMonitor m) {
        calcKpiMonitor(m);
        if (m.getId() != null) {
            m.setUpdateTime(LocalDateTime.now());
            kpiMonitorMapper.updateById(m);
            return m;
        }
        m.setCreateTime(LocalDateTime.now());
        kpiMonitorMapper.insert(m);
        return m;
    }

    private void calcKpiMonitor(DsteKpiMonitor m) {
        // 达成率 = 实际/目标×100%
        if (m.getActualValue() != null && m.getTargetValue() != null
                && m.getTargetValue().compareTo(BigDecimal.ZERO) != 0) {
            m.setAchievementRate(m.getActualValue()
                    .divide(m.getTargetValue(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            // 偏差率 = (实际-目标)/目标
            m.setDeviationRate(m.getActualValue().subtract(m.getTargetValue())
                    .divide(m.getTargetValue(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        }
        // KPI状态: 优秀/达标/预警/不达标 (红绿灯)
        if (m.getAchievementRate() != null) {
            BigDecimal rate = m.getAchievementRate();
            if (rate.compareTo(new BigDecimal("110")) >= 0) m.setKpiStatus("优秀");
            else if (rate.compareTo(new BigDecimal("100")) >= 0) m.setKpiStatus("达标");
            else if (rate.compareTo(new BigDecimal("95")) >= 0) m.setKpiStatus("预警");
            else m.setKpiStatus("不达标");
        }
    }

    public boolean updateKpiMonitor(DsteKpiMonitor m) {
        calcKpiMonitor(m);
        m.setUpdateTime(LocalDateTime.now());
        return kpiMonitorMapper.updateById(m) > 0;
    }

    public boolean deleteKpiMonitor(Long id) {
        return kpiMonitorMapper.deleteById(id) > 0;
    }

    // ==================== 风险预警 ====================
    public List<DsteRiskWarning> listRisks() {
        return riskMapper.selectList(null);
    }

    @Transactional
    public DsteRiskWarning saveRisk(DsteRiskWarning r) {
        if (r.getId() != null) {
            r.setUpdateTime(LocalDateTime.now());
            riskMapper.updateById(r);
            return r;
        }
        r.setCreateTime(LocalDateTime.now());
        r.setCloseStatus(r.getCloseStatus() == null ? "未处置" : r.getCloseStatus());
        riskMapper.insert(r);
        return r;
    }

    public boolean updateRisk(DsteRiskWarning r) {
        r.setUpdateTime(LocalDateTime.now());
        return riskMapper.updateById(r) > 0;
    }

    public boolean deleteRisk(Long id) {
        return riskMapper.deleteById(id) > 0;
    }

    // ==================== 执行督办 ====================
    public List<DsteSupervision> listSupervisions() {
        return supervisionMapper.selectList(null);
    }

    @Transactional
    public DsteSupervision saveSupervision(DsteSupervision s) {
        if (s.getId() != null) {
            s.setUpdateTime(LocalDateTime.now());
            supervisionMapper.updateById(s);
            return s;
        }
        s.setCreateTime(LocalDateTime.now());
        s.setInitiateTime(LocalDateTime.now());
        s.setCloseStatus(s.getCloseStatus() == null ? "待反馈" : s.getCloseStatus());
        supervisionMapper.insert(s);
        return s;
    }

    public boolean updateSupervision(DsteSupervision s) {
        s.setUpdateTime(LocalDateTime.now());
        return supervisionMapper.updateById(s) > 0;
    }

    public boolean deleteSupervision(Long id) {
        return supervisionMapper.deleteById(id) > 0;
    }

    // ==================== 阶段性复盘 ====================
    public List<DsteReview> listReviews() {
        return reviewMapper.selectList(null);
    }

    @Transactional
    public DsteReview saveReview(DsteReview r) {
        if (r.getId() != null) {
            r.setUpdateTime(LocalDateTime.now());
            reviewMapper.updateById(r);
            return r;
        }
        r.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(r);
        return r;
    }

    public boolean updateReview(DsteReview r) {
        r.setUpdateTime(LocalDateTime.now());
        return reviewMapper.updateById(r) > 0;
    }

    public boolean deleteReview(Long id) {
        return reviewMapper.deleteById(id) > 0;
    }

    // ==================== 战略迭代 ====================
    public List<DsteStrategicIteration> listIterations() {
        return iterationMapper.selectList(null);
    }

    @Transactional
    public DsteStrategicIteration saveIteration(DsteStrategicIteration i) {
        if (i.getId() != null) {
            i.setUpdateTime(LocalDateTime.now());
            iterationMapper.updateById(i);
            return i;
        }
        i.setCreateTime(LocalDateTime.now());
        iterationMapper.insert(i);
        return i;
    }

    public boolean updateIteration(DsteStrategicIteration i) {
        i.setUpdateTime(LocalDateTime.now());
        return iterationMapper.updateById(i) > 0;
    }

    public boolean deleteIteration(Long id) {
        return iterationMapper.deleteById(id) > 0;
    }

    // 综合执行台账汇总
    public Map<String, Object> execLedgerSummary() {
        Map<String, Object> r = new HashMap<>();
        List<DsteKeyTask> tasks = baseMapper.selectList(null);
        r.put("taskTotal", tasks.size());
        r.put("taskCompleted", tasks.stream().filter(t -> "已完成".equals(t.getTaskStatus())).count());
        r.put("taskOverdue", tasks.stream().filter(t -> "逾期".equals(t.getTaskStatus())).count());
        r.put("riskOpen", riskMapper.selectList(null).stream().filter(rk -> !"已闭环".equals(rk.getCloseStatus())).count());
        r.put("supervisionOpen", supervisionMapper.selectList(null).stream().filter(s -> !"已销号".equals(s.getCloseStatus())).count());
        // 计划完成率 = ∑(子任务权重×进度)
        BigDecimal completionRate = tasks.stream()
                .filter(t -> t.getTaskWeight() != null && t.getProgress() != null)
                .map(t -> t.getTaskWeight().multiply(t.getProgress()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        r.put("planCompletionRate", completionRate.setScale(2, RoundingMode.HALF_UP));
        return r;
    }
}
