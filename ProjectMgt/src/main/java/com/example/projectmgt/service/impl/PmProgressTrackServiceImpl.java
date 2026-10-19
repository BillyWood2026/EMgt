package com.example.projectmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmgt.entity.PmProgressTrack;
import com.example.projectmgt.entity.PmProject;
import com.example.projectmgt.entity.PmWbsTask;
import com.example.projectmgt.mapper.PmProgressTrackMapper;
import com.example.projectmgt.mapper.PmProjectMapper;
import com.example.projectmgt.mapper.PmWbsTaskMapper;
import com.example.projectmgt.service.PmProgressTrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PmProgressTrackServiceImpl implements PmProgressTrackService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private PmProgressTrackMapper progressTrackMapper;

    @Autowired
    private PmProjectMapper projectMapper;

    @Autowired
    private PmWbsTaskMapper wbsTaskMapper;

    @Override
    public List<PmProgressTrack> list() {
        return progressTrackMapper.selectList(null);
    }

    @Override
    public IPage<PmProgressTrack> page(Page<PmProgressTrack> page, String lagStatus, Long projectId) {
        LambdaQueryWrapper<PmProgressTrack> wrapper = new LambdaQueryWrapper<>();
        if (lagStatus != null && !lagStatus.isEmpty()) {
            wrapper.eq(PmProgressTrack::getLagStatus, lagStatus);
        }
        if (projectId != null) {
            wrapper.eq(PmProgressTrack::getProjectId, projectId);
        }
        wrapper.orderByDesc(PmProgressTrack::getCreatedTime);
        return progressTrackMapper.selectPage(page, wrapper);
    }

    @Override
    public PmProgressTrack getById(Long id) {
        return progressTrackMapper.selectById(id);
    }

    @Override
    @Transactional
    public PmProgressTrack create(PmProgressTrack track) {
        PmProject project = mustGetProject(track.getProjectId());
        if (Boolean.TRUE.equals(project.getMonthlyLockFlag())) {
            throw new IllegalStateException("项目已月结锁定，禁止录入进度，请先解锁");
        }
        if (!StringUtils.hasText(track.getTrackNo())) {
            track.setTrackNo(generateTrackNo());
        }
        track.setProjectNo(project.getProjectNo());
        calculate(track);
        LocalDateTime now = LocalDateTime.now();
        track.setCreatedTime(now);
        track.setUpdatedTime(now);
        track.setDeleted(0);
        track.setVersion(1L);
        progressTrackMapper.insert(track);

        syncWbsProgress(track);
        aggregateProjectBudget(project.getId());
        return progressTrackMapper.selectById(track.getId());
    }

    @Override
    @Transactional
    public PmProgressTrack update(PmProgressTrack track) {
        PmProgressTrack existing = progressTrackMapper.selectById(track.getId());
        if (existing == null) {
            throw new IllegalArgumentException("进度跟踪记录不存在: " + track.getId());
        }
        PmProject project = mustGetProject(existing.getProjectId());
        if (Boolean.TRUE.equals(project.getMonthlyLockFlag())) {
            throw new IllegalStateException("项目已月结锁定，禁止修改进度，请先解锁");
        }
        calculate(track);
        track.setUpdatedTime(LocalDateTime.now());
        progressTrackMapper.updateById(track);

        syncWbsProgress(track.getWbsTaskId() != null ? track : existing);
        aggregateProjectBudget(existing.getProjectId());
        return progressTrackMapper.selectById(track.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        PmProgressTrack existing = progressTrackMapper.selectById(id);
        progressTrackMapper.deleteById(id);
        if (existing != null && existing.getProjectId() != null) {
            aggregateProjectBudget(existing.getProjectId());
        }
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            deleteById(id);
        }
    }

    /**
     * 完成率 = 实际进度 / 计划进度 * 100；
     * 进度偏差 = 实际 - 计划；偏差率 = 偏差 / 计划 * 100；
     * 滞后状态：>0 ahead，=0 normal，<0 lagging
     */
    private void calculate(PmProgressTrack track) {
        Integer plan = track.getPlanProgress();
        Integer actual = track.getActualProgress();
        if (plan != null && actual != null && plan > 0) {
            BigDecimal planBd = new BigDecimal(plan);
            BigDecimal actualBd = new BigDecimal(actual);
            track.setProgressCompletionRate(actualBd.multiply(new BigDecimal("100"))
                    .divide(planBd, 2, RoundingMode.HALF_UP));
            int deviation = actual - plan;
            track.setProgressDeviation(deviation);
            track.setDeviationRate(new BigDecimal(deviation).multiply(new BigDecimal("100"))
                    .divide(planBd, 2, RoundingMode.HALF_UP));
            if (deviation > 0) {
                track.setLagStatus("ahead");
            } else if (deviation < 0) {
                track.setLagStatus("lagging");
            } else {
                track.setLagStatus("normal");
            }
        } else if (actual != null) {
            track.setProgressCompletionRate(new BigDecimal(actual).setScale(2, RoundingMode.HALF_UP));
        }
    }

    /** 进度回写 WBS 任务 */
    private void syncWbsProgress(PmProgressTrack track) {
        if (track.getWbsTaskId() == null || track.getActualProgress() == null) {
            return;
        }
        PmWbsTask task = wbsTaskMapper.selectById(track.getWbsTaskId());
        if (task != null) {
            task.setProgress(track.getActualProgress());
            task.setUpdatedTime(LocalDateTime.now());
            wbsTaskMapper.updateById(task);
        }
    }

    /** 汇总项目已用预算(各跟踪单 used_amount 合计)并重算使用率 */
    private void aggregateProjectBudget(Long projectId) {
        PmProject project = projectMapper.selectById(projectId);
        if (project == null) {
            return;
        }
        List<PmProgressTrack> tracks = progressTrackMapper.selectList(
                new LambdaQueryWrapper<PmProgressTrack>().eq(PmProgressTrack::getProjectId, projectId));
        BigDecimal used = BigDecimal.ZERO;
        for (PmProgressTrack t : tracks) {
            if (t.getUsedAmount() != null) {
                used = used.add(t.getUsedAmount());
            }
        }
        project.setUsedBudget(used);
        if (project.getTotalBudget() != null && project.getTotalBudget().compareTo(BigDecimal.ZERO) > 0) {
            project.setBudgetUsageRate(used.multiply(new BigDecimal("100"))
                    .divide(project.getTotalBudget(), 2, RoundingMode.HALF_UP));
        }
        project.setUpdatedTime(LocalDateTime.now());
        projectMapper.updateById(project);
    }

    private PmProject mustGetProject(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId 不能为空");
        }
        PmProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("项目不存在: " + projectId);
        }
        return project;
    }

    private String generateTrackNo() {
        return "TRK" + LocalDateTime.now().format(NO_FMT)
                + String.format("%02d", ThreadLocalRandom.current().nextInt(100));
    }
}
