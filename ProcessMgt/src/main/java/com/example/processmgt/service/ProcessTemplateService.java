package com.example.processmgt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.processmgt.entity.ProcessTemplate;

/**
 * 流程模板 Service
 */
public interface ProcessTemplateService extends IService<ProcessTemplate> {

    /**
     * 分页查询流程模板
     */
    IPage<ProcessTemplate> pageList(Integer pageNum, Integer pageSize, String keyword,
                                     String category, String status);
}
