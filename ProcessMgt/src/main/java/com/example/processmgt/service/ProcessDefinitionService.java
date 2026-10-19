package com.example.processmgt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.processmgt.entity.ProcessDefinition;

/**
 * 流程定义 Service
 */
public interface ProcessDefinitionService extends IService<ProcessDefinition> {

    /**
     * 分页查询流程定义
     */
    IPage<ProcessDefinition> pageList(Integer pageNum, Integer pageSize, String keyword,
                                       String level, String status, String category);

    /**
     * 废止流程
     */
    void deprecate(Long id);

    /**
     * 归档流程
     */
    void archive(Long id);

    /**
     * 提交评审(draft/rejected -> pending)，并生成评审审批记录
     */
    ProcessDefinition submitReview(Long id);

    /**
     * 评审审批通过(pending -> approved)
     */
    ProcessDefinition approve(Long id, String comment);

    /**
     * 评审驳回(pending -> rejected)
     */
    ProcessDefinition reject(Long id, String comment);

    /**
     * 发布生效(approved -> active)，发布后纳入流程架构
     */
    ProcessDefinition publish(Long id);

    /**
     * 变更：基于已发布流程派生新版本草稿，原版本保持active
     */
    ProcessDefinition newVersion(Long id);
}
