package com.example.salary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.salary.entity.SalaryItem;
import com.example.salary.mapper.SalaryItemMapper;
import com.example.salary.service.SalaryItemService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 薪资项目 / 薪资政策 Service 实现
 */
@Service
public class SalaryItemServiceImpl
        extends ServiceImpl<SalaryItemMapper, SalaryItem>
        implements SalaryItemService {

    @Override
    public IPage<SalaryItem> pageList(long page, long size, String name, String type) {
        LambdaQueryWrapper<SalaryItem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(SalaryItem::getName, name);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(SalaryItem::getType, type);
        }
        wrapper.orderByAsc(SalaryItem::getId);
        return this.page(new Page<>(page, Math.min(size, 500L)), wrapper);
    }
}
