package com.example.attendance.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.attendance.dto.PageResult;
import com.example.attendance.entity.AttendanceRequest;
import com.example.attendance.service.AttendanceRequestService;
import com.example.common.dto.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

/**
 * 加班管理 Controller
 * 对齐 Overtime.vue：
 *  GET  /api/attendance/requests                        分页查询（requestType=OVERTIME）
 *  POST /api/attendance/requests/overtime               新建加班申请
 *  PUT  /api/attendance/requests/{overtimeNo}/approve   审批通过（路径为业务单号 requestNo）
 *  PUT  /api/attendance/requests/{overtimeNo}/cancel    撤销
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/attendance/requests")
public class AttendanceRequestController {

    @Autowired
    private AttendanceRequestService attendanceRequestService;

    /**
     * 加班单分页查询。
     * 查询参数：pageNum / pageSize / requestType(默认按前端传 OVERTIME)
     *          / employeeName / department / overtimeType / compensation / approvalStatus
     */
    @GetMapping
    public PageResult<AttendanceRequest> listRequests(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String requestType,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String overtimeType,
            @RequestParam(required = false) String compensation,
            @RequestParam(required = false) String approvalStatus) {

        Page<AttendanceRequest> page = new Page<>(pageNum, pageSize);
        var wrapper = Wrappers.<AttendanceRequest>lambdaQuery();
        if (StringUtils.hasText(requestType)) {
            wrapper.eq(AttendanceRequest::getRequestType, requestType.trim());
        }
        if (StringUtils.hasText(employeeName)) {
            wrapper.like(AttendanceRequest::getEmployeeName, employeeName.trim());
        }
        if (StringUtils.hasText(department)) {
            wrapper.eq(AttendanceRequest::getDepartment, department.trim());
        }
        if (StringUtils.hasText(overtimeType)) {
            wrapper.eq(AttendanceRequest::getOvertimeType, overtimeType.trim());
        }
        if (StringUtils.hasText(compensation)) {
            wrapper.eq(AttendanceRequest::getCompensation, compensation.trim());
        }
        if (StringUtils.hasText(approvalStatus)) {
            wrapper.eq(AttendanceRequest::getApprovalStatus, approvalStatus.trim().toUpperCase());
        }
        wrapper.orderByDesc(AttendanceRequest::getId);
        return PageResult.of(attendanceRequestService.page(page, wrapper));
    }

    /**
     * 新建加班申请。自动生成 requestNo、补全默认状态并按起止时间计算时长。
     */
    @PostMapping("/overtime")
    public ResponseResult<AttendanceRequest> createOvertime(@RequestBody AttendanceRequest request) {
        if (!StringUtils.hasText(request.getRequestNo())) {
            request.setRequestNo("OT-" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(request.getRequestType())) {
            request.setRequestType("OVERTIME");
        }
        if (!StringUtils.hasText(request.getApprovalStatus())) {
            request.setApprovalStatus("PENDING");
        }
        if (request.getDurationHours() == null
                && request.getStartDate() != null && request.getEndDate() != null
                && !request.getEndDate().isBefore(request.getStartDate())) {
            long minutes = Duration.between(request.getStartDate(), request.getEndDate()).toMinutes();
            request.setDurationHours(BigDecimal.valueOf(minutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
        }
        attendanceRequestService.save(request);
        return ResponseResult.success(request);
    }

    /**
     * 审批通过：按业务单号 overtimeNo(requestNo) 更新状态为 APPROVED
     */
    @PutMapping("/{overtimeNo}/approve")
    public ResponseResult<Void> approve(@PathVariable String overtimeNo) {
        attendanceRequestService.lambdaUpdate()
                .eq(AttendanceRequest::getRequestNo, overtimeNo)
                .set(AttendanceRequest::getApprovalStatus, "APPROVED")
                .update();
        return ResponseResult.success();
    }

    /**
     * 撤销：按业务单号 overtimeNo(requestNo) 更新状态为 CANCELLED
     */
    @PutMapping("/{overtimeNo}/cancel")
    public ResponseResult<Void> cancel(@PathVariable String overtimeNo) {
        attendanceRequestService.lambdaUpdate()
                .eq(AttendanceRequest::getRequestNo, overtimeNo)
                .set(AttendanceRequest::getApprovalStatus, "CANCELLED")
                .update();
        return ResponseResult.success();
    }

    // ---- 标准 CRUD ----

    @GetMapping("/list")
    public ResponseResult<java.util.List<AttendanceRequest>> listAll() {
        return ResponseResult.success(
                attendanceRequestService.list(
                        Wrappers.<AttendanceRequest>lambdaQuery().orderByDesc(AttendanceRequest::getId)));
    }

    @GetMapping("/{id}")
    public ResponseResult<AttendanceRequest> getById(@PathVariable Long id) {
        return ResponseResult.success(attendanceRequestService.getById(id));
    }

    @PostMapping
    public ResponseResult<AttendanceRequest> create(@RequestBody AttendanceRequest request) {
        attendanceRequestService.save(request);
        return ResponseResult.success(request);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody AttendanceRequest request) {
        request.setId(id);
        return ResponseResult.success(attendanceRequestService.updateById(request));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(attendanceRequestService.removeById(id));
    }
}
