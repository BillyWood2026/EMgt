package com.example.processmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.processmgt.entity.ProcessTemplate;
import com.example.processmgt.mapper.ProcessTemplateMapper;
import com.example.processmgt.service.ProcessTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 流程模板 Service 实现
 */
@Service
public class ProcessTemplateServiceImpl extends ServiceImpl<ProcessTemplateMapper, ProcessTemplate>
        implements ProcessTemplateService {

    @Override
    public IPage<ProcessTemplate> pageList(Integer pageNum, Integer pageSize, String keyword,
                                            String category, String status) {
        Page<ProcessTemplate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProcessTemplate> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ProcessTemplate::getName, keyword)
                    .or().like(ProcessTemplate::getDescription, keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(ProcessTemplate::getCategory, category);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ProcessTemplate::getStatus, status);
        }
        wrapper.orderByDesc(ProcessTemplate::getUpdateTime);
        return this.page(page, wrapper);
    }
}
