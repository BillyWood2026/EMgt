package com.example.processmgt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.processmgt.entity.ProcessApproval;

/**
 * 流程审批 Service
 */
public interface ProcessApprovalService extends IService<ProcessApproval> {

    /**
     * 待审批列表(按审批人查询 pending 状态)
     */
    IPage<ProcessApproval> pendingList(Integer pageNum, Integer pageSize, String approver);

    /**
     * 我发起的(按申请人查询)
     */
    IPage<ProcessApproval> initiatedList(Integer pageNum, Integer pageSize, String applicant);

    /**
     * 已完成的(按审批人查询 approved/rejected 状态)
     */
    IPage<ProcessApproval> completedList(Integer pageNum, Integer pageSize, String approver);

    /**
     * 审批通过
     */
    void approve(Long id, String comment);

    /**
     * 审批驳回
     */
    void reject(Long id, String comment);
}
