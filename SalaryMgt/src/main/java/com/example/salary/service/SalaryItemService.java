package com.example.salary.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.salary.entity.SalaryItem;

/**
 * 薪资项目 / 薪资政策 Service
 */
public interface SalaryItemService extends IService<SalaryItem> {

    /**
     * 分页查询薪资项目
     */
    IPage<SalaryItem> pageList(long page, long size, String name, String type);
}
