package com.example.salary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.salary.entity.SalaryMatrixPackage;
import com.example.salary.mapper.SalaryMatrixPackageMapper;
import com.example.salary.service.SalaryMatrixPackageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 矩阵包 / 岗位薪资等级 Service 实现
 */
@Service
public class SalaryMatrixPackageServiceImpl
        extends ServiceImpl<SalaryMatrixPackageMapper, SalaryMatrixPackage>
        implements SalaryMatrixPackageService {

    @Override
    public IPage<SalaryMatrixPackage> pageList(long page, long size, String keyword) {
        LambdaQueryWrapper<SalaryMatrixPackage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SalaryMatrixPackage::getLevel, keyword)
                    .or().like(SalaryMatrixPackage::getLevelName, keyword)
                    .or().like(SalaryMatrixPackage::getDescription, keyword);
        }
        wrapper.orderByAsc(SalaryMatrixPackage::getId);
        return this.page(new Page<>(page, Math.min(size, 500L)), wrapper);
    }
}
