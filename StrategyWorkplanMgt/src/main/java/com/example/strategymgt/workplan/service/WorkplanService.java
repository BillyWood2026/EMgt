package com.example.strategymgt.workplan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.strategymgt.workplan.entity.*;
import com.example.strategymgt.workplan.mapper.*;
import com.example.strategymgt.common.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WorkplanService extends ServiceImpl<WorkplanMainMapper, WorkplanMain> {

    private final WorkplanTemplateMapper templateMapper;
    private final WorkplanWbsTaskMapper wbsMapper;
    private final WorkplanApprovalMapper approvalMapper;
    private final WorkplanProgressTrackMapper trackMapper;
    private final WorkplanSummaryMapper summaryMapper;
    private final WorkplanScoreMapper scoreMapper;
    private final WorkplanLedgerMapper ledgerMapper;

    public WorkplanService(WorkplanTemplateMapper templateMapper, WorkplanWbsTaskMapper wbsMapper,
                           WorkplanApprovalMapper approvalMapper, WorkplanProgressTrackMapper trackMapper,
                           WorkplanSummaryMapper summaryMapper, WorkplanScoreMapper scoreMapper,
                           WorkplanLedgerMapper ledgerMapper) {
        this.templateMapper = templateMapper;
        this.wbsMapper = wbsMapper;
        this.approvalMapper = approvalMapper;
        this.trackMapper = trackMapper;
        this.summaryMapper = summaryMapper;
        this.scoreMapper = scoreMapper;
        this.ledgerMapper = ledgerMapper;
    }

    // ==================== 模板配置 ====================
    public List<WorkplanTemplate> listTemplates() { return templateMapper.selectList(null); }

    @Transactional
    public WorkplanTemplate saveTemplate(WorkplanTemplate t) {
        if (t.getId() != null) {
            t.setUpdateTime(LocalDateTime.now());
            templateMapper.updateById(t);
            return t;
        }
        t.setCreateTime(LocalDateTime.now());
        templateMapper.insert(t);
        return t;
    }

    public boolean updateTemplate(WorkplanTemplate t) {
        t.setUpdateTime(LocalDateTime.now());
        return templateMapper.updateById(t) > 0;
    }

    public boolean deleteTemplate(Long id) { return templateMapper.deleteById(id) > 0; }

    // ==================== 计划主表 ====================
    public List<WorkplanMain> listPlans(Integer year, String status) {
        LambdaQueryWrapper<WorkplanMain> w = new LambdaQueryWrapper<>();
        if (year != null) w.eq(WorkplanMain::getPlanYear, year);
        if (status != null) w.eq(WorkplanMain::getPlanStatus, status);
        return baseMapper.selectList(w);
    }

    @Transactional
    public WorkplanMain savePlan(WorkplanMain p) {
        if (p.getId() != null) {
            p.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(p);
            return p;
        }
        p.setCreateTime(LocalDateTime.now());
        p.setPlanStatus(p.getPlanStatus() == null ? "草稿" : p.getPlanStatus());
        baseMapper.insert(p);
        return p;
    }

    public boolean updatePlan(WorkplanMain p) {
        p.setUpdateTime(LocalDateTime.now());
        return baseMapper.updateById(p) > 0;
    }

    public boolean deletePlan(Long id) { return baseMapper.deleteById(id) > 0; }

    // 状态机流转: 草稿→待审批→执行中→已完成/逾期未完成/已终止→已归档
    @Transactional
    public WorkplanMain changeStatus(Long id, String targetStatus) {
        WorkplanMain plan = baseMapper.selectById(id);
        if (plan == null) throw new BizException("计划不存在");
        if (plan.getLockStatus() != null && plan.getLockStatus() == 1) {
            throw new BizException("计划已月结锁定，不可修改");
        }
        String cur = plan.getPlanStatus();
        boolean valid = switch (targetStatus) {
            case "待审批" -> "草稿".equals(cur);
            case "执行中" -> "待审批".equals(cur);
            case "已完成" -> "执行中".equals(cur);
            case "逾期未完成" -> "执行中".equals(cur);
            case "已终止" -> "草稿".equals(cur) || "待审批".equals(cur) || "执行中".equals(cur);
            case "已归档" -> "已完成".equals(cur) || "逾期未完成".equals(cur) || "已终止".equals(cur);
            default -> false;
        };
        if (!valid) throw new BizException("状态流转不合法: " + cur + " → " + targetStatus);
        plan.setPlanStatus(targetStatus);
        plan.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(plan);
        return plan;
    }

    // 月结锁定
    @Transactional
    public WorkplanMain lockPlan(Long id) {
        WorkplanMain plan = baseMapper.selectById(id);
        if (plan == null) throw new BizException("计划不存在");
        plan.setLockStatus(1);
        plan.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(plan);
        return plan;
    }

    // ==================== WBS任务 ====================
    public List<WorkplanWbsTask> listWbsTasks(Long planId) {
        return wbsMapper.selectList(new LambdaQueryWrapper<WorkplanWbsTask>()
                .eq(WorkplanWbsTask::getPlanId, planId).orderByAsc(WorkplanWbsTask::getTaskLevel));
    }

    @Transactional
    public WorkplanWbsTask saveWbsTask(WorkplanWbsTask t) {
        if (t.getId() != null) {
            t.setUpdateTime(LocalDateTime.now());
            wbsMapper.updateById(t);
        } else {
            t.setCreateTime(LocalDateTime.now());
            t.setTaskStatus(t.getTaskStatus() == null ? "未启动" : t.getTaskStatus());
            wbsMapper.insert(t);
        }
        // 更新计划的WBS权重合计
        updatePlanTotalWeight(t.getPlanId());
        return t;
    }

    public boolean updateWbsTask(WorkplanWbsTask t) {
        t.setUpdateTime(LocalDateTime.now());
        boolean ok = wbsMapper.updateById(t) > 0;
        if (t.getPlanId() != null) updatePlanTotalWeight(t.getPlanId());
        return ok;
    }

    public boolean deleteWbsTask(Long id) {
        WorkplanWbsTask t = wbsMapper.selectById(id);
        boolean ok = wbsMapper.deleteById(id) > 0;
        if (t != null && t.getPlanId() != null) updatePlanTotalWeight(t.getPlanId());
        return ok;
    }

    // WBS权重合计校验: 一级任务权重必须=100%
    public Map<String, Object> checkWbsWeight(Long planId) {
        List<WorkplanWbsTask> tasks = listWbsTasks(planId);
        // 只统计一级任务权重
        BigDecimal total = tasks.stream().filter(t -> t.getTaskLevel() != null && t.getTaskLevel() == 1)
                .map(t -> t.getTaskWeight() == null ? BigDecimal.ZERO : t.getTaskWeight())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> r = new HashMap<>();
        r.put("totalWeight", total);
        r.put("isValid", total.compareTo(new BigDecimal("100")) == 0);
        return r;
    }

    private void updatePlanTotalWeight(Long planId) {
        if (planId == null) return;
        Map<String, Object> check = checkWbsWeight(planId);
        WorkplanMain plan = baseMapper.selectById(planId);
        if (plan != null) {
            plan.setTotalWeight((BigDecimal) check.get("totalWeight"));
            baseMapper.updateById(plan);
        }
    }

    // 完成任务: 前置任务未完成则后置不可完成
    @Transactional
    public WorkplanWbsTask completeTask(Long taskId) {
        WorkplanWbsTask task = wbsMapper.selectById(taskId);
        if (task == null) throw new BizException("任务不存在");
        // 检查前置任务
        if (task.getDependencyIds() != null && !task.getDependencyIds().isEmpty()) {
            String[] deps = task.getDependencyIds().split(",");
            for (String dep : deps) {
                String depId = dep.trim();
                if (depId.isEmpty()) continue;
                WorkplanWbsTask depTask = wbsMapper.selectById(Long.parseLong(depId));
                if (depTask != null && !"已完成".equals(depTask.getTaskStatus())) {
                    throw new BizException("前置任务[" + depTask.getTaskName() + "]未完成，不可完成当前任务");
                }
            }
        }
        task.setTaskStatus("已完成");
        task.setProgress(new BigDecimal("100"));
        task.setUpdateTime(LocalDateTime.now());
        wbsMapper.updateById(task);
        return task;
    }

    // ==================== 审批 ====================
    public List<WorkplanApproval> listApprovals(Long planId) {
        LambdaQueryWrapper<WorkplanApproval> w = new LambdaQueryWrapper<>();
        if (planId != null) w.eq(WorkplanApproval::getPlanId, planId);
        return approvalMapper.selectList(w);
    }

    @Transactional
    public WorkplanApproval saveApproval(WorkplanApproval a) {
        boolean isNew = a.getId() == null;
        if (isNew) {
            a.setCreateTime(LocalDateTime.now());
            a.setApprovalTime(LocalDateTime.now());
            approvalMapper.insert(a);
        } else {
            a.setUpdateTime(LocalDateTime.now());
            approvalMapper.updateById(a);
        }
        // 新登记审批通过/驳回时同步计划状态（编辑已有审批记录不重复触发流转）
        if (isNew) {
            WorkplanMain plan = baseMapper.selectById(a.getPlanId());
            if (plan != null) {
                if ("已通过".equals(a.getApprovalStatus())) {
                    plan.setPlanStatus("执行中");
                } else if ("已驳回".equals(a.getApprovalStatus())) {
                    plan.setPlanStatus("草稿");
                }
                plan.setApprovalId(a.getId());
                plan.setUpdateTime(LocalDateTime.now());
                baseMapper.updateById(plan);
            }
        }
        return a;
    }

    public boolean updateApproval(WorkplanApproval a) {
        a.setUpdateTime(LocalDateTime.now());
        return approvalMapper.updateById(a) > 0;
    }

    public boolean deleteApproval(Long id) { return approvalMapper.deleteById(id) > 0; }

    // ==================== 执行跟踪 ====================
    public List<WorkplanProgressTrack> listTracks(Long planId) {
        LambdaQueryWrapper<WorkplanProgressTrack> w = new LambdaQueryWrapper<>();
        if (planId != null) w.eq(WorkplanProgressTrack::getPlanId, planId);
        return trackMapper.selectList(w);
    }

    @Transactional
    public WorkplanProgressTrack saveTrack(WorkplanProgressTrack t) {
        if (t.getActualProgress() != null && t.getPlannedProgress() != null) {
            t.setDeviation(t.getActualProgress().subtract(t.getPlannedProgress()));
        }
        if (t.getId() != null) {
            t.setUpdateTime(LocalDateTime.now());
            trackMapper.updateById(t);
        } else {
            t.setReportTime(LocalDateTime.now());
            t.setCreateTime(LocalDateTime.now());
            trackMapper.insert(t);
        }
        // 同步任务进度
        if (t.getTaskId() != null && t.getActualProgress() != null) {
            WorkplanWbsTask task = wbsMapper.selectById(t.getTaskId());
            if (task != null) {
                task.setProgress(t.getActualProgress());
                wbsMapper.updateById(task);
            }
        }
        return t;
    }

    public boolean updateTrack(WorkplanProgressTrack t) {
        if (t.getActualProgress() != null && t.getPlannedProgress() != null) {
            t.setDeviation(t.getActualProgress().subtract(t.getPlannedProgress()));
        }
        t.setUpdateTime(LocalDateTime.now());
        return trackMapper.updateById(t) > 0;
    }

    public boolean deleteTrack(Long id) { return trackMapper.deleteById(id) > 0; }

    // ==================== 总结复盘 ====================
    public List<WorkplanSummary> listSummaries(Long planId) {
        LambdaQueryWrapper<WorkplanSummary> w = new LambdaQueryWrapper<>();
        if (planId != null) w.eq(WorkplanSummary::getPlanId, planId);
        return summaryMapper.selectList(w);
    }

    @Transactional
    public WorkplanSummary saveSummary(WorkplanSummary s) {
        if (s.getId() != null) {
            s.setUpdateTime(LocalDateTime.now());
            summaryMapper.updateById(s);
            return s;
        }
        s.setCreateTime(LocalDateTime.now());
        summaryMapper.insert(s);
        return s;
    }

    public boolean updateSummary(WorkplanSummary s) {
        s.setUpdateTime(LocalDateTime.now());
        return summaryMapper.updateById(s) > 0;
    }

    public boolean deleteSummary(Long id) { return summaryMapper.deleteById(id) > 0; }

    // ==================== 考核打分 ====================
    public List<WorkplanScore> listScores(Long planId) {
        LambdaQueryWrapper<WorkplanScore> w = new LambdaQueryWrapper<>();
        if (planId != null) w.eq(WorkplanScore::getPlanId, planId);
        return scoreMapper.selectList(w);
    }

    @Transactional
    public WorkplanScore saveScore(WorkplanScore s) {
        if (s.getId() != null) {
            s.setUpdateTime(LocalDateTime.now());
            scoreMapper.updateById(s);
            return s;
        }
        s.setScoreTime(LocalDateTime.now());
        s.setCreateTime(LocalDateTime.now());
        scoreMapper.insert(s);
        return s;
    }

    public boolean updateScore(WorkplanScore s) {
        s.setUpdateTime(LocalDateTime.now());
        return scoreMapper.updateById(s) > 0;
    }

    public boolean deleteScore(Long id) { return scoreMapper.deleteById(id) > 0; }

    // 考核综合得分 = ∑(得分项×权重)
    public BigDecimal calcTotalScore(Long planId) {
        List<WorkplanScore> scores = listScores(planId);
        return scores.stream()
                .filter(s -> s.getScoreValue() != null && s.getScoreWeight() != null)
                .map(s -> s.getScoreValue().multiply(s.getScoreWeight()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== 台账统计 ====================
    public List<WorkplanLedger> listLedgers() { return ledgerMapper.selectList(null); }

    // 计划完成率 = ∑(一级任务权重×进度)，与WBS权重校验口径一致
    public BigDecimal calcPlanCompletionRate(Long planId) {
        List<WorkplanWbsTask> tasks = listWbsTasks(planId);
        return tasks.stream()
                .filter(t -> t.getTaskLevel() != null && t.getTaskLevel() == 1)
                .filter(t -> t.getTaskWeight() != null && t.getProgress() != null)
                .map(t -> t.getTaskWeight().multiply(t.getProgress()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public WorkplanLedger generateLedger(Long planId, String period) {
        List<WorkplanWbsTask> tasks = listWbsTasks(planId);
        WorkplanLedger ledger = new WorkplanLedger();
        ledger.setPlanId(planId);
        ledger.setStatPeriod(period);
        ledger.setTotalTasks(tasks.size());
        ledger.setCompletedTasks((int) tasks.stream().filter(t -> "已完成".equals(t.getTaskStatus())).count());
        ledger.setOverdueTasks((int) tasks.stream().filter(t -> "已逾期".equals(t.getTaskStatus())).count());
        ledger.setCompletionRate(calcPlanCompletionRate(planId));
        ledger.setAvgScore(calcTotalScore(planId));
        ledger.setArchiveStatus("未归档");
        ledger.setCreateTime(LocalDateTime.now());
        ledgerMapper.insert(ledger);
        return ledger;
    }

    public boolean updateLedger(WorkplanLedger l) {
        l.setUpdateTime(LocalDateTime.now());
        return ledgerMapper.updateById(l) > 0;
    }

    public boolean deleteLedger(Long id) { return ledgerMapper.deleteById(id) > 0; }
}
