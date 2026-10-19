package com.example.processmgt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.processmgt.entity.ProcessApproval;
import com.example.processmgt.service.ProcessApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 流程审批 Controller
 */
@RestController
@RequestMapping("/api/process/approval")
public class ProcessApprovalController {

    @Autowired
    private ProcessApprovalService processApprovalService;

    /**
     * 分页查询审批记录(全部)
     */
    @GetMapping("/list")
    public ResponseResult<IPage<ProcessApproval>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String approver,
            @RequestParam(required = false) String applicant) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessApproval> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ProcessApproval::getStatus, status);
        }
        if (approver != null && !approver.isEmpty()) {
            wrapper.eq(ProcessApproval::getApprover, approver);
        }
        if (applicant != null && !applicant.isEmpty()) {
            wrapper.eq(ProcessApproval::getApplicant, applicant);
        }
        wrapper.orderByDesc(ProcessApproval::getId);
        return ResponseResult.success(
                processApprovalService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize), wrapper));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<ProcessApproval> getById(@PathVariable Long id) {
        return ResponseResult.success(processApprovalService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<ProcessApproval> create(@RequestBody ProcessApproval processApproval) {
        processApprovalService.save(processApproval);
        return ResponseResult.success(processApproval);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ResponseResult<ProcessApproval> update(@PathVariable Long id,
                                                   @RequestBody ProcessApproval processApproval) {
        processApproval.setId(id);
        processApprovalService.updateById(processApproval);
        return ResponseResult.success(processApproval);
    }

    /**
     * 删除(逻辑删除)
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        processApprovalService.removeById(id);
        return ResponseResult.success();
    }

    /**
     * 待审批列表
     */
    @GetMapping("/pending")
    public ResponseResult<IPage<ProcessApproval>> pending(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String approver) {
        return ResponseResult.success(
                processApprovalService.pendingList(pageNum, pageSize, approver));
    }

    /**
     * 我发起的
     */
    @GetMapping("/initiated")
    public ResponseResult<IPage<ProcessApproval>> initiated(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String applicant) {
        return ResponseResult.success(
                processApprovalService.initiatedList(pageNum, pageSize, applicant));
    }

    /**
     * 已完成的
     */
    @GetMapping("/completed")
    public ResponseResult<IPage<ProcessApproval>> completed(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String approver) {
        return ResponseResult.success(
                processApprovalService.completedList(pageNum, pageSize, approver));
    }

    /**
     * 审批通过
     */
    @PutMapping("/{id}/approve")
    public ResponseResult<Void> approve(@PathVariable Long id,
                                         @RequestBody(required = false) Map<String, String> body) {
        String comment = body == null ? null : body.get("comment");
        processApprovalService.approve(id, comment);
        return ResponseResult.success();
    }

    /**
     * 审批驳回
     */
    @PutMapping("/{id}/reject")
    public ResponseResult<Void> reject(@PathVariable Long id,
                                        @RequestBody(required = false) Map<String, String> body) {
        String comment = body == null ? null : body.get("comment");
        processApprovalService.reject(id, comment);
        return ResponseResult.success();
    }
}
