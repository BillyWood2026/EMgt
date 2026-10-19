package com.example.auditmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditmgt.common.ResponseResult;
import com.example.auditmgt.entity.AuditPlan;
import com.example.auditmgt.mapper.AuditPlanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit/plan")
@CrossOrigin(origins = "*")
public class AuditPlanController {

    @Autowired
    private AuditPlanMapper planMapper;

    @GetMapping("/list")
    public ResponseResult<Page<AuditPlan>> list(@RequestParam(required = false) String name,
                                                @RequestParam(required = false) String type,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<AuditPlan> qw = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            qw.like(AuditPlan::getName, name);
        }
        if (type != null && !type.isEmpty()) {
            qw.eq(AuditPlan::getType, type);
        }
        if (status != null && !status.isEmpty()) {
            qw.eq(AuditPlan::getStatus, status);
        }
        qw.orderByDesc(AuditPlan::getId);
        return ResponseResult.success(planMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/{id}")
    public ResponseResult<AuditPlan> getById(@PathVariable Long id) {
        return ResponseResult.success(planMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<AuditPlan> create(@RequestBody AuditPlan plan) {
        if (plan.getPlanNo() == null || plan.getPlanNo().isEmpty()) {
            plan.setPlanNo("AP" + System.currentTimeMillis());
        }
        if (plan.getStatus() == null || plan.getStatus().isEmpty()) {
            plan.setStatus("planning");
        }
        if (plan.getType() == null) {
            plan.setType("internal");
        }
        plan.setId(null);
        plan.setDeleted(null);
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.insert(plan);
        return ResponseResult.success(plan);
    }

    @PutMapping("/{id}")
    public ResponseResult<AuditPlan> update(@PathVariable Long id, @RequestBody AuditPlan plan) {
        plan.setId(id);
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.updateById(plan);
        return ResponseResult.success(planMapper.selectById(id));
    }

    @PutMapping("/{id}/execute")
    public ResponseResult<AuditPlan> execute(@PathVariable Long id) {
        AuditPlan plan = planMapper.selectById(id);
        if (plan == null) {
            return ResponseResult.error("审计计划不存在");
        }
        if (!"planning".equals(plan.getStatus())) {
            return ResponseResult.error("当前状态[" + plan.getStatus() + "]不允许下发执行，仅计划中(planning)可执行");
        }
        plan.setStatus("executing");
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.updateById(plan);
        return ResponseResult.success(plan);
    }

    @PutMapping("/{id}/complete")
    public ResponseResult<AuditPlan> complete(@PathVariable Long id) {
        AuditPlan plan = planMapper.selectById(id);
        if (plan == null) {
            return ResponseResult.error("审计计划不存在");
        }
        if (!"executing".equals(plan.getStatus())) {
            return ResponseResult.error("当前状态[" + plan.getStatus() + "]不允许完结，仅执行中(executing)可完结");
        }
        plan.setStatus("completed");
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.updateById(plan);
        return ResponseResult.success(plan);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(planMapper.deleteById(id) > 0);
    }
}
