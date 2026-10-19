package com.example.projectmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmgt.entity.PmProject;
import com.example.projectmgt.entity.PmProjectClosure;
import com.example.projectmgt.mapper.PmProjectClosureMapper;
import com.example.projectmgt.mapper.PmProjectMapper;
import com.example.projectmgt.service.PmProjectClosureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PmProjectClosureServiceImpl implements PmProjectClosureService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private PmProjectClosureMapper closureMapper;

    @Autowired
    private PmProjectMapper projectMapper;

    @Override
    public List<PmProjectClosure> list() {
        return closureMapper.selectList(null);
    }

    @Override
    public IPage<PmProjectClosure> page(Page<PmProjectClosure> page, String status, Long projectId) {
        LambdaQueryWrapper<PmProjectClosure> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PmProjectClosure::getStatus, status);
        }
        if (projectId != null) {
            wrapper.eq(PmProjectClosure::getProjectId, projectId);
        }
        wrapper.orderByDesc(PmProjectClosure::getCreatedTime);
        return closureMapper.selectPage(page, wrapper);
    }

    @Override
    public PmProjectClosure getById(Long id) {
        return closureMapper.selectById(id);
    }

    @Override
    @Transactional
    public PmProjectClosure create(PmProjectClosure closure) {
        PmProject project = projectMapper.selectById(closure.getProjectId());
        if (project == null) {
            throw new IllegalArgumentException("关联项目不存在: " + closure.getProjectId());
        }
        if (!StringUtils.hasText(closure.getClosureNo())) {
            closure.setClosureNo("CLO" + LocalDateTime.now().format(NO_FMT)
                    + String.format("%02d", ThreadLocalRandom.current().nextInt(100)));
        }
        closure.setProjectNo(project.getProjectNo());
        closure.setStatus(PmProjectClosure.STATUS_PENDING);
        closure.setArchiveStatus(PmProject.ARCHIVE_UNARCHIVED);
        LocalDateTime now = LocalDateTime.now();
        closure.setCreatedTime(now);
        closure.setUpdatedTime(now);
        closure.setDeleted(0);
        closure.setVersion(1L);
        closureMapper.insert(closure);
        return closure;
    }

    @Override
    @Transactional
    public PmProjectClosure update(PmProjectClosure closure) {
        PmProjectClosure existing = closureMapper.selectById(closure.getId());
        if (existing == null) {
            throw new IllegalArgumentException("结项记录不存在: " + closure.getId());
        }
        if (!PmProjectClosure.STATUS_PENDING.equals(existing.getStatus())
                && !PmProjectClosure.STATUS_REJECTED.equals(existing.getStatus())) {
            throw new IllegalStateException("仅待提交/被驳回的结项记录可编辑，当前状态: " + existing.getStatus());
        }
        closure.setUpdatedTime(LocalDateTime.now());
        closureMapper.updateById(closure);
        return closureMapper.selectById(closure.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        closureMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        closureMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional
    public PmProjectClosure submit(Long id) {
        PmProjectClosure closure = mustGet(id);
        if (!PmProjectClosure.STATUS_PENDING.equals(closure.getStatus())
                && !PmProjectClosure.STATUS_REJECTED.equals(closure.getStatus())) {
            throw new IllegalStateException("仅待提交/被驳回的结项记录可提交评审，当前状态: " + closure.getStatus());
        }
        closure.setStatus(PmProjectClosure.STATUS_REVIEWING);
        closure.setUpdatedTime(LocalDateTime.now());
        closureMapper.updateById(closure);
        return closure;
    }

    @Override
    @Transactional
    public PmProjectClosure review(Long id, boolean passed, String opinion) {
        PmProjectClosure closure = mustGet(id);
        if (!PmProjectClosure.STATUS_REVIEWING.equals(closure.getStatus())) {
            throw new IllegalStateException("仅评审中(reviewing)的结项记录可审批，当前状态: " + closure.getStatus());
        }
        closure.setStatus(passed ? PmProjectClosure.STATUS_PASSED : PmProjectClosure.STATUS_REJECTED);
        closure.setOpinion(opinion);
        closure.setUpdatedTime(LocalDateTime.now());
        closureMapper.updateById(closure);
        return closure;
    }

    @Override
    @Transactional
    public PmProjectClosure archive(Long id) {
        PmProjectClosure closure = mustGet(id);
        if (!PmProjectClosure.STATUS_PASSED.equals(closure.getStatus())) {
            throw new IllegalStateException("仅评审通过(passed)的结项记录可归档，当前状态: " + closure.getStatus());
        }
        if (!Boolean.TRUE.equals(closure.getTaskClosedFlag())
                || !Boolean.TRUE.equals(closure.getExpenseSettledFlag())
                || !Boolean.TRUE.equals(closure.getContractSettledFlag())
                || !Boolean.TRUE.equals(closure.getBudgetExecutedFlag())) {
            throw new IllegalStateException("任务关闭/费用结算/合同结算/预算执行四项前置条件必须全部完成才能归档");
        }
        PmProject project = projectMapper.selectById(closure.getProjectId());
        if (project == null) {
            throw new IllegalArgumentException("关联项目不存在: " + closure.getProjectId());
        }
        if (!PmProject.STATUS_COMPLETED.equals(project.getStatus())) {
            project.setStatus(PmProject.STATUS_COMPLETED);
        }
        project.setArchiveStatus(PmProject.ARCHIVE_ARCHIVED);
        project.setArchiveReason("结项归档：" + (StringUtils.hasText(closure.getOpinion()) ? closure.getOpinion() : "评审通过"));
        project.setArchiveTime(LocalDate.now());
        project.setUpdatedTime(LocalDateTime.now());
        projectMapper.updateById(project);

        closure.setStatus(PmProjectClosure.STATUS_ARCHIVED);
        closure.setArchiveStatus(PmProject.ARCHIVE_ARCHIVED);
        closure.setUpdatedTime(LocalDateTime.now());
        closureMapper.updateById(closure);
        return closure;
    }

    private PmProjectClosure mustGet(Long id) {
        PmProjectClosure closure = closureMapper.selectById(id);
        if (closure == null) {
            throw new IllegalArgumentException("结项记录不存在: " + id);
        }
        return closure;
    }
}
