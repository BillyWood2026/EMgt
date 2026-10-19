package com.example.processmgt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.processmgt.entity.ProcessInstance;

/**
 * 流程实例 Service
 */
public interface ProcessInstanceService extends IService<ProcessInstance> {

    /**
     * 分页查询流程实例
     */
    IPage<ProcessInstance> pageList(Integer pageNum, Integer pageSize, String keyword,
                                     String status, String applicant);

    /**
     * 发起流程
     */
    ProcessInstance startProcess(ProcessInstance instance);

    /**
     * 取消流程
     */
    void cancel(Long id);

    /**
     * 执行流程监控流转：推进节点(processing)，complete=true 时办结(approved)
     */
    ProcessInstance execute(Long id, String node, boolean complete);
}
