package com.example.holiday.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.holiday.dto.PageResult;
import com.example.holiday.entity.HolidayBalance;
import com.example.holiday.entity.LeaveRequest;
import com.example.holiday.service.HolidayBalanceService;
import com.example.holiday.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * 请假管理 Controller
 * 对齐 Leave.vue：
 *  GET    /api/holiday/request/list            分页查询
 *  POST   /api/holiday/request                 新建请假（自动算天数 + 余额超额拦截）
 *  DELETE /api/holiday/request/{id}            删除
 *  POST   /api/holiday/request/{id}/approve    审批通过（核扣余额，幂等）
 *  POST   /api/holiday/request/{id}/reject     审批驳回（不核扣）
 * 同时兼容前端实际调用的 /approve/{id}、/reject/{id} 路径。
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/holiday/request")
public class LeaveRequestController {

    /** 受假期余额额度管控的假别（personal 事假等无额度假别不在此列）。 */
    private static final Set<String> CONTROLLED_TYPES =
            Set.of("annual", "timeoff", "sick", "marriage", "maternity", "compassionate");

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private HolidayBalanceService holidayBalanceService;

    /**
     * 请假单分页查询。
     * 查询参数：pageNum / pageSize / requestStatus(PENDING/APPROVED/REJECTED/CANCELLED)
     *          / employeeName / departmentId / leaveTypeCode
     */
    @GetMapping("/list")
    public PageResult<LeaveRequest> listRequests(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String requestStatus,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String leaveTypeCode) {

        Page<LeaveRequest> page = new Page<>(pageNum, pageSize);
        var wrapper = Wrappers.<LeaveRequest>lambdaQuery();
        if (StringUtils.hasText(requestStatus)) {
            wrapper.eq(LeaveRequest::getRequestStatus, requestStatus.trim().toUpperCase());
        }
        if (StringUtils.hasText(employeeName)) {
            wrapper.like(LeaveRequest::getEmployeeName, employeeName.trim());
        }
        if (StringUtils.hasText(departmentId)) {
            wrapper.eq(LeaveRequest::getDepartmentId, departmentId.trim());
        }
        if (StringUtils.hasText(leaveTypeCode)) {
            wrapper.eq(LeaveRequest::getLeaveTypeCode, leaveTypeCode.trim());
        }
        wrapper.orderByDesc(LeaveRequest::getId);
        return PageResult.of(leaveRequestService.page(page, wrapper));
    }

    /**
     * 新建请假申请。自动生成 requestNo，默认状态 PENDING；
     * 未提供 totalDays 时按起止日期（含首尾）计算；
     * 对额度受控假别校验余额，超额直接拒绝（业务码 400，请假单不落库）。
     */
    @PostMapping
    public ResponseResult<LeaveRequest> createRequest(@RequestBody LeaveRequest request) {
        if (!StringUtils.hasText(request.getRequestNo())) {
            request.setRequestNo("LR-" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(request.getRequestStatus())) {
            request.setRequestStatus("PENDING");
        }
        fillTotalDays(request);

        String over = checkBalance(request);
        if (over != null) {
            return ResponseResult.error(400, over);
        }
        leaveRequestService.save(request);
        return ResponseResult.success(request);
    }

    /**
     * 删除请假单（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        leaveRequestService.removeById(id);
        return ResponseResult.success();
    }

    /**
     * 审批通过：仅当原状态为 PENDING 时核扣一次余额（重复审批幂等，不多扣）。
     */
    @PostMapping({"/{id}/approve", "/approve/{id}"})
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Void> approve(@PathVariable Long id) {
        LeaveRequest leave = leaveRequestService.getById(id);
        if (leave == null) {
            return ResponseResult.error(404, "请假单不存在");
        }
        // 非待审批状态（已通过/已驳回/已撤销）直接幂等返回，不重复核扣。
        if (!"PENDING".equalsIgnoreCase(leave.getRequestStatus())) {
            return ResponseResult.success();
        }
        fillTotalDays(leave);
        HolidayBalance balance = locateBalance(leave);
        BigDecimal days = nz(leave.getTotalDays());
        if (balance != null && CONTROLLED_TYPES.contains(norm(leave.getLeaveTypeCode()))) {
            BigDecimal remaining = nz(balance.getAvailableBalance()).subtract(nz(balance.getUsedBalance()));
            if (remaining.compareTo(days) < 0) {
                return ResponseResult.error(400,
                        "假期余额不足：剩余 " + remaining.stripTrailingZeros().toPlainString()
                                + " 天，本次申请 " + days.stripTrailingZeros().toPlainString() + " 天");
            }
            holidayBalanceService.lambdaUpdate()
                    .eq(HolidayBalance::getId, balance.getId())
                    .setSql("used_balance = used_balance + " + days.toPlainString())
                    .update();
        }
        leaveRequestService.lambdaUpdate()
                .eq(LeaveRequest::getId, id)
                .set(LeaveRequest::getRequestStatus, "APPROVED")
                .update();
        return ResponseResult.success();
    }

    /**
     * 审批驳回（PENDING -> REJECTED，不核扣余额）。
     */
    @PostMapping({"/{id}/reject", "/reject/{id}"})
    public ResponseResult<Void> reject(@PathVariable Long id) {
        leaveRequestService.lambdaUpdate()
                .eq(LeaveRequest::getId, id)
                .set(LeaveRequest::getRequestStatus, "REJECTED")
                .update();
        return ResponseResult.success();
    }

    // ---- 内部工具 ----

    /** totalDays 缺失时按 (endDate - startDate) + 1（含首尾）计算。 */
    private void fillTotalDays(LeaveRequest request) {
        if ((request.getTotalDays() == null
                || request.getTotalDays().compareTo(BigDecimal.ZERO) <= 0)
                && request.getStartDate() != null && request.getEndDate() != null) {
            long d = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
            if (d > 0) {
                request.setTotalDays(BigDecimal.valueOf(d));
            }
        }
    }

    /** 超额返回错误文案，否则 null。无余额档案的假别/员工不拦截。 */
    private String checkBalance(LeaveRequest request) {
        if (!CONTROLLED_TYPES.contains(norm(request.getLeaveTypeCode()))) {
            return null;
        }
        HolidayBalance balance = locateBalance(request);
        if (balance == null) {
            return null;
        }
        BigDecimal days = nz(request.getTotalDays());
        BigDecimal remaining = nz(balance.getAvailableBalance()).subtract(nz(balance.getUsedBalance()));
        if (remaining.compareTo(days) < 0) {
            return "假期余额不足：剩余 " + remaining.stripTrailingZeros().toPlainString()
                    + " 天，本次申请 " + days.stripTrailingZeros().toPlainString() + " 天";
        }
        return null;
    }

    private HolidayBalance locateBalance(LeaveRequest request) {
        if (!StringUtils.hasText(request.getEmployeeName())
                || !StringUtils.hasText(request.getLeaveTypeCode())) {
            return null;
        }
        Integer year = request.getStartDate() != null
                ? request.getStartDate().getYear()
                : java.time.Year.now().getValue();
        return holidayBalanceService.lambdaQuery()
                .eq(HolidayBalance::getEmployeeName, request.getEmployeeName().trim())
                .eq(HolidayBalance::getLeaveTypeCode, norm(request.getLeaveTypeCode()))
                .eq(HolidayBalance::getBalanceYear, year)
                .eq(HolidayBalance::getBalanceStatus, "ACTIVE")
                .orderByDesc(HolidayBalance::getId)
                .last("LIMIT 1")
                .one();
    }

    private String norm(String code) {
        return code == null ? "" : code.trim().toLowerCase();
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
