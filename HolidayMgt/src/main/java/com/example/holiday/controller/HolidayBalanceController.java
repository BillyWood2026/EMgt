package com.example.holiday.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.holiday.dto.PageResult;
import com.example.holiday.entity.HolidayBalance;
import com.example.holiday.service.HolidayBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 假期余额 Controller
 * 对齐 Balance.vue：
 *  GET  /api/holiday/balance/list    分页查询
 *  POST /api/holiday/balance/adjust  新建/调整余额（携带 id 则更新，否则新增）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/holiday/balance")
public class HolidayBalanceController {

    @Autowired
    private HolidayBalanceService holidayBalanceService;

    /**
     * 假期余额分页查询。
     * 查询参数：pageNum / pageSize / balanceYear / employeeName / leaveTypeCode
     */
    @GetMapping({"", "/list"})
    public PageResult<HolidayBalance> listBalance(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer balanceYear,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String leaveTypeCode) {

        Page<HolidayBalance> page = new Page<>(pageNum, pageSize);
        var wrapper = Wrappers.<HolidayBalance>lambdaQuery();
        if (balanceYear != null) {
            wrapper.eq(HolidayBalance::getBalanceYear, balanceYear);
        }
        if (StringUtils.hasText(employeeName)) {
            wrapper.like(HolidayBalance::getEmployeeName, employeeName.trim());
        }
        if (StringUtils.hasText(department)) {
            wrapper.eq(HolidayBalance::getDepartment, department.trim());
        }
        if (StringUtils.hasText(leaveTypeCode)) {
            wrapper.eq(HolidayBalance::getLeaveTypeCode, leaveTypeCode.trim());
        }
        wrapper.orderByDesc(HolidayBalance::getBalanceYear)
               .orderByDesc(HolidayBalance::getId);
        return PageResult.of(holidayBalanceService.page(page, wrapper));
    }

    /**
     * 调整假期余额：有 id 执行更新，无 id 执行新增。
     */
    @PostMapping("/adjust")
    public ResponseResult<HolidayBalance> adjust(@RequestBody HolidayBalance balance) {
        if (!StringUtils.hasText(balance.getBalanceStatus())) {
            balance.setBalanceStatus("ACTIVE");
        }
        if (balance.getId() != null) {
            holidayBalanceService.updateById(balance);
        } else {
            holidayBalanceService.save(balance);
        }
        return ResponseResult.success(balance);
    }
}
