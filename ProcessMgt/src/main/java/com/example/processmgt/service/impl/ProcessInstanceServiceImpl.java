package com.example.processmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.processmgt.entity.ProcessInstance;
import com.example.processmgt.mapper.ProcessInstanceMapper;
import com.example.processmgt.service.ProcessInstanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 流程实例 Service 实现
 */
@Service
public class ProcessInstanceServiceImpl extends ServiceImpl<ProcessInstanceMapper, ProcessInstance>
        implements ProcessInstanceService {

    @Override
    public IPage<ProcessInstance> pageList(Integer pageNum, Integer pageSize, String keyword,
                                            String status, String applicant) {
        Page<ProcessInstance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProcessInstance> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ProcessInstance::getProcessName, keyword)
                    .or().like(ProcessInstance::getProcessCode, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ProcessInstance::getStatus, status);
        }
        if (StringUtils.hasText(applicant)) {
            wrapper.eq(ProcessInstance::getApplicant, applicant);
        }
        wrapper.orderByDesc(ProcessInstance::getSubmitTime);
        return this.page(page, wrapper);
    }

    @Override
    public ProcessInstance startProcess(ProcessInstance instance) {
        if (instance.getStatus() == null) {
            instance.setStatus(ProcessInstance.STATUS_PENDING);
        }
        if (instance.getSubmitTime() == null) {
            instance.setSubmitTime(LocalDateTime.now());
        }
        if (!StringUtils.hasText(instance.getCurrentNode())) {
            instance.setCurrentNode("提交");
        }
        instance.setCreateTime(LocalDateTime.now());
        instance.setUpdateTime(LocalDateTime.now());
        this.save(instance);
        return instance;
    }

    @Override
    public void cancel(Long id) {
        ProcessInstance instance = this.getById(id);
        if (instance == null) {
            throw new IllegalArgumentException("流程实例不存在");
        }
        String status = instance.getStatus();
        if (ProcessInstance.STATUS_APPROVED.equals(status)
                || ProcessInstance.STATUS_REJECTED.equals(status)
                || ProcessInstance.STATUS_CANCELLED.equals(status)) {
            throw new IllegalStateException("已结束的流程不能取消，当前状态: " + status);
        }
        instance.setStatus(ProcessInstance.STATUS_CANCELLED);
        instance.setFinishTime(LocalDateTime.now());
        instance.setUpdateTime(LocalDateTime.now());
        this.updateById(instance);
    }

    @Override
    @Transactional
    public ProcessInstance execute(Long id, String node, boolean complete) {
        ProcessInstance instance = this.getById(id);
        if (instance == null) {
            throw new IllegalArgumentException("流程实例不存在");
        }
        String status = instance.getStatus();
        if (ProcessInstance.STATUS_APPROVED.equals(status)
                || ProcessInstance.STATUS_REJECTED.equals(status)
                || ProcessInstance.STATUS_CANCELLED.equals(status)) {
            throw new IllegalStateException("已结束的流程不能继续执行，当前状态: " + status);
        }
        if (StringUtils.hasText(node)) {
            instance.setCurrentNode(node);
        }
        LocalDateTime now = LocalDateTime.now();
        if (complete) {
            instance.setStatus(ProcessInstance.STATUS_APPROVED);
            instance.setFinishTime(now);
        } else {
            instance.setStatus(ProcessInstance.STATUS_PROCESSING);
        }
        instance.setUpdateTime(now);
        this.updateById(instance);
        return instance;
    }
}
