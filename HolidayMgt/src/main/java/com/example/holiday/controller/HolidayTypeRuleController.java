package com.example.holiday.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.holiday.dto.PageResult;
import com.example.holiday.entity.HolidayTypeRule;
import com.example.holiday.service.HolidayTypeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 假期政策 Controller
 * 对齐 Policy.vue：
 *  GET  /api/holiday/type-rule/type/list  分页查询
 *  POST /api/holiday/type-rule/type       新建政策
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/holiday/type-rule/type")
public class HolidayTypeRuleController {

    @Autowired
    private HolidayTypeRuleService holidayTypeRuleService;

    /**
     * 假期政策分页查询。
     * 查询参数：pageNum / pageSize / leaveTypeName / leaveTypeCode / status / applyTo
     */
    @GetMapping("/list")
    public PageResult<HolidayTypeRule> listRules(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String leaveTypeName,
            @RequestParam(required = false) String leaveTypeCode,
            @RequestParam(required = false) String applyTo,
            @RequestParam(required = false) String status) {

        Page<HolidayTypeRule> page = new Page<>(pageNum, pageSize);
        var wrapper = Wrappers.<HolidayTypeRule>lambdaQuery();
        if (StringUtils.hasText(leaveTypeName)) {
            wrapper.like(HolidayTypeRule::getLeaveTypeName, leaveTypeName.trim());
        }
        if (StringUtils.hasText(leaveTypeCode)) {
            wrapper.eq(HolidayTypeRule::getLeaveTypeCode, leaveTypeCode.trim());
        }
        if (StringUtils.hasText(applyTo)) {
            wrapper.and(w -> w.eq(HolidayTypeRule::getApplicableOrgLevel, applyTo.trim())
                    .or().eq(HolidayTypeRule::getLeaveCategory, applyTo.trim()));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(HolidayTypeRule::getStatus, status.trim().toUpperCase());
        }
        wrapper.orderByDesc(HolidayTypeRule::getId);
        return PageResult.of(holidayTypeRuleService.page(page, wrapper));
    }

    /**
     * 新建假期政策。补全默认状态、适用层级等字段。
     */
    @PostMapping
    public ResponseResult<HolidayTypeRule> createRule(@RequestBody HolidayTypeRule rule) {
        rule.setId(null); // 新建一律由数据库自增，防止客户端携带 id 导致主键冲突
        if (!StringUtils.hasText(rule.getStatus())) {
            rule.setStatus("ACTIVE");
        }
        if (!StringUtils.hasText(rule.getApplicableOrgLevel()) && StringUtils.hasText(rule.getLeaveCategory())) {
            rule.setApplicableOrgLevel(rule.getLeaveCategory().trim());
        }
        if (rule.getMaxConsecutiveDays() == null) {
            rule.setMaxConsecutiveDays(0);
        }
        if (rule.getSalaryPaidFlag() == null) {
            rule.setSalaryPaidFlag(Boolean.TRUE);
        }
        holidayTypeRuleService.save(rule);
        return ResponseResult.success(rule);
    }

    /**
     * 更新假期政策（编辑保存，Policy.vue 编辑走 PUT）。
     */
    @PutMapping("/{id}")
    public ResponseResult<HolidayTypeRule> updateRule(@PathVariable Long id, @RequestBody HolidayTypeRule rule) {
        rule.setId(id);
        if (!StringUtils.hasText(rule.getStatus())) {
            rule.setStatus("ACTIVE");
        }
        if (!StringUtils.hasText(rule.getApplicableOrgLevel()) && StringUtils.hasText(rule.getLeaveCategory())) {
            rule.setApplicableOrgLevel(rule.getLeaveCategory().trim());
        }
        holidayTypeRuleService.updateById(rule);
        return ResponseResult.success(rule);
    }

    /**
     * 删除假期政策（逻辑删除）。
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> deleteRule(@PathVariable Long id) {
        return ResponseResult.success(holidayTypeRuleService.removeById(id));
    }
}
