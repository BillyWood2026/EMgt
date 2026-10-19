package com.example.processmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.processmgt.entity.ProcessApproval;
import com.example.processmgt.entity.ProcessDefinition;
import com.example.processmgt.entity.ProcessInstance;
import com.example.processmgt.mapper.ProcessApprovalMapper;
import com.example.processmgt.mapper.ProcessDefinitionMapper;
import com.example.processmgt.service.ProcessApprovalService;
import com.example.processmgt.service.ProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 流程审批 Service 实现
 */
@Service
public class ProcessApprovalServiceImpl extends ServiceImpl<ProcessApprovalMapper, ProcessApproval>
        implements ProcessApprovalService {

    @Autowired
    @Lazy
    private ProcessInstanceService processInstanceService;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Override
    public IPage<ProcessApproval> pendingList(Integer pageNum, Integer pageSize, String approver) {
        Page<ProcessApproval> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProcessApproval> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessApproval::getStatus, ProcessApproval.STATUS_PENDING);
        if (StringUtils.hasText(approver)) {
            wrapper.eq(ProcessApproval::getApprover, approver);
        }
        wrapper.orderByAsc(ProcessApproval::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public IPage<ProcessApproval> initiatedList(Integer pageNum, Integer pageSize, String applicant) {
        Page<ProcessApproval> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProcessApproval> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(applicant)) {
            wrapper.eq(ProcessApproval::getApplicant, applicant);
        }
        wrapper.orderByDesc(ProcessApproval::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public IPage<ProcessApproval> completedList(Integer pageNum, Integer pageSize, String approver) {
        Page<ProcessApproval> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProcessApproval> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ProcessApproval::getStatus,
                ProcessApproval.STATUS_APPROVED, ProcessApproval.STATUS_REJECTED);
        if (StringUtils.hasText(approver)) {
            wrapper.eq(ProcessApproval::getApprover, approver);
        }
        wrapper.orderByDesc(ProcessApproval::getApproveTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional
    public void approve(Long id, String comment) {
        ProcessApproval approval = this.getById(id);
        if (approval == null) {
            throw new IllegalArgumentException("审批记录不存在");
        }
        if (!ProcessApproval.STATUS_PENDING.equals(approval.getStatus())) {
            throw new IllegalStateException("当前审批记录已处理，不可重复审批");
        }
        approval.setStatus(ProcessApproval.STATUS_APPROVED);
        approval.setComment(comment);
        approval.setApproveTime(LocalDateTime.now());
        approval.setUpdateTime(LocalDateTime.now());
        this.updateById(approval);

        // 同步更新流程实例状态为审批通过
        if (approval.getInstanceId() != null) {
            ProcessInstance instance = processInstanceService.getById(approval.getInstanceId());
            if (instance != null && ProcessInstance.STATUS_PENDING.equals(instance.getStatus())) {
                instance.setStatus(ProcessInstance.STATUS_APPROVED);
                instance.setFinishTime(LocalDateTime.now());
                instance.setUpdateTime(LocalDateTime.now());
                processInstanceService.updateById(instance);
            }
        }

        // 同步流程定义评审状态(pending -> approved)
        if (approval.getProcessDefId() != null) {
            ProcessDefinition def = processDefinitionMapper.selectById(approval.getProcessDefId());
            if (def != null && ProcessDefinition.STATUS_PENDING.equals(def.getStatus())) {
                def.setStatus(ProcessDefinition.STATUS_APPROVED);
                def.setUpdateTime(LocalDateTime.now());
                processDefinitionMapper.updateById(def);
            }
        }
    }

    @Override
    @Transactional
    public void reject(Long id, String comment) {
        ProcessApproval approval = this.getById(id);
        if (approval == null) {
            throw new IllegalArgumentException("审批记录不存在");
        }
        if (!ProcessApproval.STATUS_PENDING.equals(approval.getStatus())) {
            throw new IllegalStateException("当前审批记录已处理，不可重复审批");
        }
        approval.setStatus(ProcessApproval.STATUS_REJECTED);
        approval.setComment(comment);
        approval.setApproveTime(LocalDateTime.now());
        approval.setUpdateTime(LocalDateTime.now());
        this.updateById(approval);

        // 同步更新流程实例状态为驳回
        if (approval.getInstanceId() != null) {
            ProcessInstance instance = processInstanceService.getById(approval.getInstanceId());
            if (instance != null && ProcessInstance.STATUS_PENDING.equals(instance.getStatus())) {
                instance.setStatus(ProcessInstance.STATUS_REJECTED);
                instance.setFinishTime(LocalDateTime.now());
                instance.setUpdateTime(LocalDateTime.now());
                processInstanceService.updateById(instance);
            }
        }

        // 同步流程定义评审状态(pending -> rejected)
        if (approval.getProcessDefId() != null) {
            ProcessDefinition def = processDefinitionMapper.selectById(approval.getProcessDefId());
            if (def != null && ProcessDefinition.STATUS_PENDING.equals(def.getStatus())) {
                def.setStatus("rejected");
                def.setUpdateTime(LocalDateTime.now());
                processDefinitionMapper.updateById(def);
            }
        }
    }
}
