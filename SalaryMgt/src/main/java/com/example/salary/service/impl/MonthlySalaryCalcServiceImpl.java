package com.example.salary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.salary.entity.EmployeeSalary;
import com.example.salary.entity.MonthlySalaryCalc;
import com.example.salary.mapper.EmployeeSalaryMapper;
import com.example.salary.mapper.MonthlySalaryCalcMapper;
import com.example.salary.service.MonthlySalaryCalcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 月度薪资核算 Service 实现
 */
@Service
public class MonthlySalaryCalcServiceImpl
        extends ServiceImpl<MonthlySalaryCalcMapper, MonthlySalaryCalc>
        implements MonthlySalaryCalcService {

    private static final DateTimeFormatter NO_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private EmployeeSalaryMapper employeeSalaryMapper;

    @Override
    public IPage<MonthlySalaryCalc> pageList(long page, long size, String month, String status, String department) {
        LambdaQueryWrapper<MonthlySalaryCalc> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(month)) {
            wrapper.eq(MonthlySalaryCalc::getMonth, month);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonthlySalaryCalc::getStatus, status);
        }
        if (StringUtils.hasText(department)) {
            wrapper.like(MonthlySalaryCalc::getDepartment, department);
        }
        wrapper.orderByAsc(MonthlySalaryCalc::getId);
        return this.page(new Page<>(page, Math.min(size, 500L)), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MonthlySalaryCalc> calculate(String month) {
        String targetMonth = StringUtils.hasText(month) ? month.trim() : "2026-06";
        LocalDateTime now = LocalDateTime.now();
        String calcTime = now.format(NO_TIME);
        String monthPrefix = "CALC-" + targetMonth.replace("-", "") + "-";

        List<EmployeeSalary> employees = employeeSalaryMapper.selectList(
                new LambdaQueryWrapper<EmployeeSalary>().orderByAsc(EmployeeSalary::getId));

        List<MonthlySalaryCalc> existing = this.list(
                new LambdaQueryWrapper<MonthlySalaryCalc>().eq(MonthlySalaryCalc::getMonth, targetMonth));

        List<MonthlySalaryCalc> generated = new ArrayList<>();
        int seq = existing.size();
        for (EmployeeSalary emp : employees) {
            boolean duplicated = existing.stream().anyMatch(
                    c -> emp.getEmployeeName() != null
                            && emp.getEmployeeName().equals(c.getEmployeeName()));
            if (duplicated) {
                continue;
            }
            seq++;

            BigDecimal basic = nz(emp.getBasicSalary());
            BigDecimal performance = nz(emp.getPerformanceSalary());
            BigDecimal allowance = nz(emp.getAllowance());
            BigDecimal bonus = BigDecimal.ZERO;
            BigDecimal gross = basic.add(performance).add(allowance).add(bonus);
            BigDecimal deduction = BigDecimal.ZERO;
            BigDecimal net = gross.subtract(deduction);

            MonthlySalaryCalc calc = new MonthlySalaryCalc();
            calc.setCalcNo(monthPrefix + String.format("%03d", seq));
            calc.setMonth(targetMonth);
            calc.setEmployeeName(emp.getEmployeeName());
            calc.setDepartment(emp.getDepartment());
            calc.setPosition(emp.getPosition());
            calc.setBasicSalary(basic);
            calc.setPerformanceSalary(performance);
            calc.setBonus(bonus);
            calc.setAllowance(allowance);
            calc.setGrossSalary(gross);
            calc.setDeduction(deduction);
            calc.setNetSalary(net);
            calc.setStatus("待核算");
            calc.setCalcTime(calcTime);
            calc.setDeleted(0);
            calc.setVersion(1);
            calc.setCreateTime(now);
            calc.setUpdateTime(now);
            generated.add(calc);
        }
        if (!generated.isEmpty()) {
            this.saveBatch(generated);
        }

        return this.list(new LambdaQueryWrapper<MonthlySalaryCalc>()
                .eq(MonthlySalaryCalc::getMonth, targetMonth)
                .orderByAsc(MonthlySalaryCalc::getId));
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
