package com.example.salary.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.salary.entity.MonthlySalaryCalc;
import com.example.salary.service.MonthlySalaryCalcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 月度薪资核算 Controller（对应前端 Calculate.vue / History.vue）
 * base: /api/salary/monthly-calc
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/salary/monthly-calc")
public class MonthlySalaryCalcController {

    /** 前端下拉英文状态码 -> 页面展示 / 库中存储的中文状态 */
    private static final Map<String, String> STATUS_MAP = Map.of(
            "pending", "待核算",
            "calculating", "核算中",
            "pending_audit", "待审核",
            "audited", "已审核",
            "paid", "已发放"
    );

    @Autowired
    private MonthlySalaryCalcService monthlySalaryCalcService;

    /**
     * 列表（分页；支持 month / period、status、department 过滤，返回数组）
     */
    @GetMapping("/list")
    public ResponseResult<List<MonthlySalaryCalc>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "100") long size,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department) {
        String normalizedMonth = normalizeMonth(StringUtils.hasText(month) ? month : period);
        String normalizedStatus = normalizeStatus(status);
        IPage<MonthlySalaryCalc> result = monthlySalaryCalcService.pageList(
                page, size, normalizedMonth, normalizedStatus, department);
        return ResponseResult.success(result.getRecords());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<MonthlySalaryCalc> getById(@PathVariable Long id) {
        return ResponseResult.success(monthlySalaryCalcService.getById(id));
    }

    /**
     * 触发月度薪资核算：基于员工薪资生成核算记录
     */
    @PostMapping("/calculate")
    public ResponseResult<List<MonthlySalaryCalc>> calculate(
            @RequestBody(required = false) MonthlySalaryCalc body) {
        String month = body == null ? null : normalizeMonth(body.getMonth());
        List<MonthlySalaryCalc> records = monthlySalaryCalcService.calculate(month);
        return ResponseResult.success(records);
    }

    /**
     * 手工新增核算记录
     */
    @PostMapping
    public ResponseResult<MonthlySalaryCalc> create(@RequestBody MonthlySalaryCalc entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setId(null);
        entity.setDeleted(0);
        entity.setVersion(1);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("待核算");
        }
        if (!StringUtils.hasText(entity.getCalcTime())) {
            entity.setCalcTime(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        fillAmounts(entity);
        monthlySalaryCalcService.save(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 更新（id 在 URL 上）
     */
    @PutMapping("/{id}")
    public ResponseResult<MonthlySalaryCalc> update(@PathVariable Long id,
                                                    @RequestBody MonthlySalaryCalc entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        fillAmounts(entity);
        monthlySalaryCalcService.updateById(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 更新（id 在 body 中，兼容别名）
     */
    @PutMapping
    public ResponseResult<MonthlySalaryCalc> updateByBody(@RequestBody MonthlySalaryCalc entity) {
        entity.setUpdateTime(LocalDateTime.now());
        fillAmounts(entity);
        monthlySalaryCalcService.updateById(entity);
        return ResponseResult.success(entity);
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        monthlySalaryCalcService.removeById(id);
        return ResponseResult.success();
    }

    /**
     * 月份入参归一化：
     * "2026-06" 直接使用；ISO 日期（如 2026-05-31T16:00:00.000Z）取前 7 位。
     */
    private String normalizeMonth(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.length() >= 7 ? trimmed.substring(0, 7) : trimmed;
    }

    /**
     * 状态入参归一化：英文码转中文；中文原样返回。
     */
    private String normalizeStatus(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return STATUS_MAP.getOrDefault(raw.trim(), raw.trim());
    }

    private void fillAmounts(MonthlySalaryCalc entity) {
        BigDecimal basic = nz(entity.getBasicSalary());
        BigDecimal performance = nz(entity.getPerformanceSalary());
        BigDecimal bonus = nz(entity.getBonus());
        BigDecimal allowance = nz(entity.getAllowance());
        BigDecimal deduction = nz(entity.getDeduction());
        if (entity.getBasicSalary() == null) {
            entity.setBasicSalary(basic);
        }
        if (entity.getPerformanceSalary() == null) {
            entity.setPerformanceSalary(performance);
        }
        if (entity.getBonus() == null) {
            entity.setBonus(bonus);
        }
        if (entity.getAllowance() == null) {
            entity.setAllowance(allowance);
        }
        if (entity.getDeduction() == null) {
            entity.setDeduction(deduction);
        }
        if (entity.getGrossSalary() == null) {
            entity.setGrossSalary(basic.add(performance).add(bonus).add(allowance));
        }
        if (entity.getNetSalary() == null) {
            entity.setNetSalary(entity.getGrossSalary().subtract(deduction));
        }
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
