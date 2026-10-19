package com.example.attendance.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.attendance.dto.PageResult;
import com.example.attendance.entity.ClockRecord;
import com.example.attendance.entity.FillCardRequest;
import com.example.attendance.service.ClockRecordService;
import com.example.attendance.service.FillCardRequestService;
import com.example.common.dto.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 日常打卡 Controller
 * 对齐 Daily.vue：
 *  GET  /api/attendance/clock-in                    分页查询打卡记录
 *  POST /api/attendance/clock-in/fill-card-request  补卡申请（null body + params 或 JSON body 均可）
 *  GET  /api/attendance/daily/list                  健康检查别名，返回全量数组
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/attendance")
public class AttendanceClockController {

    @Autowired
    private ClockRecordService clockRecordService;

    @Autowired
    private FillCardRequestService fillCardRequestService;

    /**
     * 打卡记录分页查询。
     * 查询参数：pageNum / pageSize / employeeName / department / status(normal|late|early|missing)
     *          / startDate / endDate
     */
    @GetMapping("/clock-in")
    public PageResult<ClockRecord> listClockIn(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Page<ClockRecord> page = new Page<>(pageNum, pageSize);
        var wrapper = Wrappers.<ClockRecord>lambdaQuery();
        if (StringUtils.hasText(employeeName)) {
            wrapper.like(ClockRecord::getEmployeeName, employeeName.trim());
        }
        if (StringUtils.hasText(department)) {
            wrapper.eq(ClockRecord::getDepartment, department.trim());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ClockRecord::getRiskFlag, status.trim().toUpperCase());
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(ClockRecord::getRecordDate, LocalDate.parse(startDate.trim()));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(ClockRecord::getRecordDate, LocalDate.parse(endDate.trim()));
        }
        wrapper.orderByDesc(ClockRecord::getRecordDate)
               .orderByDesc(ClockRecord::getId);
        return PageResult.of(clockRecordService.page(page, wrapper));
    }

    /**
     * 补卡申请。前端以 axios params 方式提交（请求体为 null），
     * 同时兼容直接提交 JSON body。
     */
    @PostMapping("/clock-in/fill-card-request")
    public ResponseResult<FillCardRequest> fillCardRequest(
            @RequestParam(required = false) String requestNo,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String missingDate,
            @RequestParam(required = false) String shouldCheckInTime,
            @RequestParam(required = false) String shouldCheckOutTime,
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) FillCardRequest body) {

        FillCardRequest request = body != null ? body : new FillCardRequest();

        if (!StringUtils.hasText(request.getRequestNo())) {
            request.setRequestNo(StringUtils.hasText(requestNo)
                    ? requestNo.trim() : "BK-" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(request.getEmployeeId()) && StringUtils.hasText(employeeId)) {
            request.setEmployeeId(employeeId.trim());
        }
        if (!StringUtils.hasText(request.getEmployeeName()) && StringUtils.hasText(employeeName)) {
            request.setEmployeeName(employeeName.trim());
        }
        if (request.getMissingDate() == null && StringUtils.hasText(missingDate)) {
            request.setMissingDate(LocalDate.parse(missingDate.trim()));
        }
        if (!StringUtils.hasText(request.getShouldCheckInTime()) && StringUtils.hasText(shouldCheckInTime)) {
            request.setShouldCheckInTime(shouldCheckInTime.trim());
        }
        if (!StringUtils.hasText(request.getShouldCheckOutTime()) && StringUtils.hasText(shouldCheckOutTime)) {
            request.setShouldCheckOutTime(shouldCheckOutTime.trim());
        }
        if (!StringUtils.hasText(request.getReason()) && StringUtils.hasText(reason)) {
            request.setReason(reason.trim());
        }
        if (!StringUtils.hasText(request.getStatus())) {
            request.setStatus("PENDING");
        }

        fillCardRequestService.save(request);
        return ResponseResult.success(request);
    }

    /**
     * 健康检查 / 别名：日常打卡全量列表
     */
    @GetMapping("/daily/list")
    public ResponseResult<List<ClockRecord>> dailyList() {
        return ResponseResult.success(
                clockRecordService.list(
                        Wrappers.<ClockRecord>lambdaQuery()
                                .orderByDesc(ClockRecord::getRecordDate)
                                .orderByDesc(ClockRecord::getId)));
    }

    // ---- 标准 CRUD：打卡记录 ----

    @GetMapping("/clock-in/list")
    public ResponseResult<List<ClockRecord>> clockInListAll() {
        return ResponseResult.success(
                clockRecordService.list(
                        Wrappers.<ClockRecord>lambdaQuery()
                                .orderByDesc(ClockRecord::getRecordDate)
                                .orderByDesc(ClockRecord::getId)));
    }

    @GetMapping("/clock-in/{id}")
    public ResponseResult<ClockRecord> getClockIn(@PathVariable Long id) {
        return ResponseResult.success(clockRecordService.getById(id));
    }

    @PostMapping("/clock-in")
    public ResponseResult<ClockRecord> createClockIn(@RequestBody ClockRecord record) {
        clockRecordService.save(record);
        return ResponseResult.success(record);
    }

    @PutMapping("/clock-in/{id}")
    public ResponseResult<Boolean> updateClockIn(@PathVariable Long id, @RequestBody ClockRecord record) {
        record.setId(id);
        return ResponseResult.success(clockRecordService.updateById(record));
    }

    @DeleteMapping("/clock-in/{id}")
    public ResponseResult<Boolean> deleteClockIn(@PathVariable Long id) {
        return ResponseResult.success(clockRecordService.removeById(id));
    }
}
