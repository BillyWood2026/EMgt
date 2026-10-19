package com.example.projectmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmgt.entity.PmProject;
import com.example.projectmgt.entity.PmWbsTask;
import com.example.projectmgt.mapper.PmProjectMapper;
import com.example.projectmgt.mapper.PmWbsTaskMapper;
import com.example.projectmgt.service.PmProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class PmProjectServiceImpl implements PmProjectService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ObjectMapper JSON = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Autowired
    private PmProjectMapper projectMapper;

    @Autowired
    private PmWbsTaskMapper wbsTaskMapper;

    @Override
    public List<PmProject> list() {
        return projectMapper.selectList(null);
    }

    @Override
    public IPage<PmProject> page(Page<PmProject> page, String status, String type, String name) {
        LambdaQueryWrapper<PmProject> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PmProject::getStatus, status);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(PmProject::getType, type);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(PmProject::getName, name);
        }
        wrapper.orderByDesc(PmProject::getCreatedTime);
        return projectMapper.selectPage(page, wrapper);
    }

    @Override
    public PmProject getById(Long id) {
        return projectMapper.selectById(id);
    }

    @Override
    @Transactional
    public PmProject create(PmProject project) {
        if (!StringUtils.hasText(project.getProjectNo())) {
            project.setProjectNo(generateProjectNo());
        }
        if (!StringUtils.hasText(project.getStatus())) {
            project.setStatus(PmProject.STATUS_PLANNING);
        }
        if (project.getMonthlyLockFlag() == null) {
            project.setMonthlyLockFlag(false);
        }
        if (!StringUtils.hasText(project.getArchiveStatus())) {
            project.setArchiveStatus(PmProject.ARCHIVE_UNARCHIVED);
        }
        LocalDateTime now = LocalDateTime.now();
        project.setCreatedTime(now);
        project.setUpdatedTime(now);
        project.setDeleted(0);
        project.setVersion(1L);
        applyBudgetRate(project);
        projectMapper.insert(project);
        return project;
    }

    @Override
    @Transactional
    public PmProject update(PmProject project) {
        PmProject existing = mustGet(project.getId());
        if (Boolean.TRUE.equals(existing.getMonthlyLockFlag())
                && (project.getTotalBudget() != null || project.getUsedBudget() != null
                    || project.getContractAmount() != null)) {
            throw new IllegalStateException("项目已月结锁定，禁止修改预算/合同金额，请先解锁");
        }
        if (PmProject.STATUS_ARCHIVED.equals(existing.getStatus())) {
            throw new IllegalStateException("项目已归档，禁止编辑");
        }
        // 以库中现值参与重算，避免 PUT 部分字段时被置空
        if (project.getTotalBudget() == null) {
            project.setTotalBudget(existing.getTotalBudget());
        }
        if (project.getUsedBudget() == null) {
            project.setUsedBudget(existing.getUsedBudget());
        }
        applyBudgetRate(project);
        project.setUpdatedTime(LocalDateTime.now());
        projectMapper.updateById(project);
        return projectMapper.selectById(project.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        projectMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        projectMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional
    public PmProject recalcBudget(Long id) {
        PmProject project = mustGet(id);
        applyBudgetRate(project);
        project.setUpdatedTime(LocalDateTime.now());
        projectMapper.updateById(project);
        return project;
    }

    @Override
    @Transactional
    public PmProject start(Long id) {
        return transit(id, PmProject.STATUS_PLANNING, PmProject.STATUS_ONGOING);
    }

    @Override
    @Transactional
    public PmProject suspend(Long id) {
        return transit(id, PmProject.STATUS_ONGOING, PmProject.STATUS_PAUSED);
    }

    @Override
    @Transactional
    public PmProject resume(Long id) {
        return transit(id, PmProject.STATUS_PAUSED, PmProject.STATUS_ONGOING);
    }

    @Override
    @Transactional
    public PmProject complete(Long id) {
        return transit(id, PmProject.STATUS_ONGOING, PmProject.STATUS_COMPLETED);
    }

    @Override
    @Transactional
    public PmProject setMonthlyLock(Long id, boolean locked) {
        PmProject project = mustGet(id);
        project.setMonthlyLockFlag(locked);
        project.setUpdatedTime(LocalDateTime.now());
        projectMapper.updateById(project);
        return project;
    }

    @Override
    @Transactional
    public PmProject freezeBaseline(Long id) {
        PmProject project = mustGet(id);
        List<PmWbsTask> tasks = wbsTaskMapper.selectList(
                new LambdaQueryWrapper<PmWbsTask>().eq(PmWbsTask::getProjectId, id));
        if (tasks.isEmpty()) {
            throw new IllegalStateException("项目尚无WBS任务，无法冻结基线");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (PmWbsTask t : tasks) {
            if (t.getWeight() != null) {
                total = total.add(t.getWeight());
            }
        }
        if (total.compareTo(new BigDecimal("100.00")) != 0) {
            throw new IllegalStateException("WBS权重合计必须等于100，当前合计: "
                    + total.setScale(2, RoundingMode.HALF_UP));
        }
        project.setBaselineVersion(bumpBaseline(project.getBaselineVersion()));
        try {
            project.setSnapshotJson(JSON.writeValueAsString(tasks));
        } catch (Exception e) {
            throw new IllegalStateException("基线快照序列化失败: " + e.getMessage());
        }
        project.setUpdatedTime(LocalDateTime.now());
        projectMapper.updateById(project);
        return project;
    }

    private PmProject transit(Long id, String expected, String target) {
        PmProject project = mustGet(id);
        if (!expected.equals(project.getStatus())) {
            throw new IllegalStateException("非法状态流转: " + project.getStatus() + " -> " + target
                    + "，仅 " + expected + " 状态可执行该操作");
        }
        project.setStatus(target);
        project.setUpdatedTime(LocalDateTime.now());
        projectMapper.updateById(project);
        return project;
    }

    private void applyBudgetRate(PmProject project) {
        BigDecimal total = project.getTotalBudget();
        BigDecimal used = project.getUsedBudget();
        if (total != null && total.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal base = used == null ? BigDecimal.ZERO : used;
            if (base.compareTo(total) > 0) {
                throw new IllegalStateException("已使用预算不能大于总预算");
            }
            project.setBudgetUsageRate(base.multiply(new BigDecimal("100"))
                    .divide(total, 2, RoundingMode.HALF_UP));
        } else if (total != null && total.compareTo(BigDecimal.ZERO) == 0) {
            project.setBudgetUsageRate(BigDecimal.ZERO);
        }
    }

    private String bumpBaseline(String current) {
        if (!StringUtils.hasText(current)) {
            return "V1.0";
        }
        String num = current.toLowerCase().startsWith("v") ? current.substring(1) : current;
        String[] parts = num.split("\\.");
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return "V" + major + "." + (minor + 1);
        } catch (NumberFormatException e) {
            return current + ".1";
        }
    }

    private String generateProjectNo() {
        return "PM" + LocalDateTime.now().format(NO_FMT)
                + String.format("%02d", ThreadLocalRandom.current().nextInt(100));
    }

    private PmProject mustGet(Long id) {
        PmProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new IllegalArgumentException("项目不存在: " + id);
        }
        return project;
    }
}
