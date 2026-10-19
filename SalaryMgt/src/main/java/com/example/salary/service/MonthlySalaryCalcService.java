package com.example.salary.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.salary.entity.MonthlySalaryCalc;

import java.util.List;

/**
 * 月度薪资核算 Service
 */
public interface MonthlySalaryCalcService extends IService<MonthlySalaryCalc> {

    /**
     * 分页查询月度核算记录，支持月份 / 状态 / 部门过滤
     */
    IPage<MonthlySalaryCalc> pageList(long page, long size, String month, String status, String department);

    /**
     * 按员工薪资数据生成指定月份的核算记录（已存在的员工不重复生成）
     *
     * @param month 核算月份，格式 yyyy-MM
     * @return 该月份全部核算记录
     */
    List<MonthlySalaryCalc> calculate(String month);
}
