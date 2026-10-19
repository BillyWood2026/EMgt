package com.example.projectmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmgt.entity.PmWbsTask;
import com.example.projectmgt.mapper.PmWbsTaskMapper;
import com.example.projectmgt.service.PmWbsTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PmWbsTaskServiceImpl implements PmWbsTaskService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private PmWbsTaskMapper wbsTaskMapper;

    @Override
    public List<PmWbsTask> list() {
        return wbsTaskMapper.selectList(null);
    }

    @Override
    public IPage<PmWbsTask> page(Page<PmWbsTask> page, String status, Long projectId) {
        LambdaQueryWrapper<PmWbsTask> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PmWbsTask::getStatus, status);
        }
        if (projectId != null) {
            wrapper.eq(PmWbsTask::getProjectId, projectId);
        }
        wrapper.orderByAsc(PmWbsTask::getWbsCode);
        return wbsTaskMapper.selectPage(page, wrapper);
    }

    @Override
    public PmWbsTask getById(Long id) {
        return wbsTaskMapper.selectById(id);
    }

    @Override
    @Transactional
    public PmWbsTask create(PmWbsTask task) {
        if (!StringUtils.hasText(task.getTaskNo())) {
            task.setTaskNo(generateTaskNo());
        }
        validatePredecessors(task);
        validateWeight(task);
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedTime(now);
        task.setUpdatedTime(now);
        task.setDeleted(0);
        task.setVersion(1L);
        wbsTaskMapper.insert(task);
        return task;
    }

    @Override
    @Transactional
    public PmWbsTask update(PmWbsTask task) {
        PmWbsTask existing = wbsTaskMapper.selectById(task.getId());
        if (existing == null) {
            throw new IllegalArgumentException("WBS任务不存在: " + task.getId());
        }
        validatePredecessors(task);
        validateWeight(task);
        task.setUpdatedTime(LocalDateTime.now());
        wbsTaskMapper.updateById(task);
        return wbsTaskMapper.selectById(task.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        wbsTaskMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        wbsTaskMapper.deleteBatchIds(ids);
    }

    @Override
    public Map<String, Object> weightSummary(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId 不能为空");
        }
        List<PmWbsTask> tasks = wbsTaskMapper.selectList(
                new LambdaQueryWrapper<PmWbsTask>().eq(PmWbsTask::getProjectId, projectId));
        BigDecimal total = BigDecimal.ZERO;
        for (PmWbsTask t : tasks) {
            if (t.getWeight() != null) {
                total = total.add(t.getWeight());
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", projectId);
        result.put("count", tasks.size());
        result.put("totalWeight", total.setScale(2, RoundingMode.HALF_UP));
        result.put("valid", total.compareTo(new BigDecimal("100.00")) == 0);
        return result;
    }

    /** 权重范围 0~100 */
    private void validateWeight(PmWbsTask task) {
        if (task.getWeight() == null) {
            return;
        }
        BigDecimal w = task.getWeight();
        if (w.compareTo(BigDecimal.ZERO) < 0 || w.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("WBS权重必须在0~100之间: " + w);
        }
    }

    /** 前后置依赖任务必须存在且同项目，不允许依赖自身 */
    private void validatePredecessors(PmWbsTask task) {
        validateIdList(task.getPredecessorTasks(), task, "前置任务");
        validateIdList(task.getSuccessorTasks(), task, "后置任务");
    }

    private void validateIdList(String idList, PmWbsTask task, String label) {
        if (!StringUtils.hasText(idList)) {
            return;
        }
        String[] tokens = idList.split("[,，;；\\s]+");
        for (String token : tokens) {
            String t = token.trim();
            if (t.isEmpty()) {
                continue;
            }
            long refId;
            try {
                refId = Long.parseLong(t);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(label + "ID格式非法: " + t);
            }
            if (task.getId() != null && refId == task.getId()) {
                throw new IllegalArgumentException(label + "不能引用任务自身: " + t);
            }
            PmWbsTask ref = wbsTaskMapper.selectById(refId);
            if (ref == null) {
                throw new IllegalArgumentException(label + "不存在: " + t);
            }
            if (task.getProjectId() != null && ref.getProjectId() != null
                    && !task.getProjectId().equals(ref.getProjectId())) {
                throw new IllegalArgumentException(label + "必须与当前任务属于同一项目: " + t);
            }
        }
    }

    private String generateTaskNo() {
        return "WBS" + LocalDateTime.now().format(NO_FMT)
                + String.format("%02d", ThreadLocalRandom.current().nextInt(100));
    }
}
