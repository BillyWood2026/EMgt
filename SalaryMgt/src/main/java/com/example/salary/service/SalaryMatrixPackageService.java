package com.example.salary.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.salary.entity.SalaryMatrixPackage;

/**
 * 矩阵包 / 岗位薪资等级 Service
 */
public interface SalaryMatrixPackageService extends IService<SalaryMatrixPackage> {

    /**
     * 分页查询薪资等级
     */
    IPage<SalaryMatrixPackage> pageList(long page, long size, String keyword);
}
