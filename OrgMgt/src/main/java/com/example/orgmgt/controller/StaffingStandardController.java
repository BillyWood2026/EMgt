package com.example.orgmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.orgmgt.entity.StaffingStandard;
import com.example.orgmgt.service.StaffingStandardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Staffing Standard Controller
 * REST API for staffing standard management
 */
@Tag(name = "Staffing Standard Management")
@RestController
@RequestMapping("/api/staffing-standard")
public class StaffingStandardController {
    
    @Autowired
    private StaffingStandardService staffingStandardService;
    
    @Operation(summary = "Query all staffing standards")
    @GetMapping("/list")
    public ResponseResult<List<StaffingStandard>> list() {
        return ResponseResult.success(staffingStandardService.list());
    }

    @Operation(summary = "Query staffing standards with pagination")
    @GetMapping("/page")
    public ResponseResult<Page<StaffingStandard>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String positionLevel,
            @RequestParam(required = false) String staffingType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long orgId) {
        int pNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int pSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        LambdaQueryWrapper<StaffingStandard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(positionLevel != null && !positionLevel.trim().isEmpty(),
                StaffingStandard::getPositionLevel, positionLevel);
        wrapper.eq(staffingType != null && !staffingType.trim().isEmpty(),
                StaffingStandard::getStaffingType, staffingType);
        wrapper.eq(status != null && !status.trim().isEmpty(),
                StaffingStandard::getStatus, status);
        wrapper.eq(orgId != null, StaffingStandard::getOrgId, orgId);
        // 当前模块未注册 MyBatis-Plus 分页拦截器，service.page() 的 total 恒为 0；
        // 这里先 count 再显式 LIMIT，保证前端依赖的 records/total 均正确
        long total = staffingStandardService.count(wrapper);
        wrapper.orderByDesc(StaffingStandard::getId);
        wrapper.last("LIMIT " + (pNum - 1) * pSize + "," + pSize);
        Page<StaffingStandard> page = new Page<>(pNum, pSize, total);
        page.setRecords(staffingStandardService.list(wrapper));
        return ResponseResult.success(page);
    }
    
    @Operation(summary = "Query all active staffing standards")
    @GetMapping("/active")
    public ResponseResult<List<StaffingStandard>> getActiveStandards() {
        return ResponseResult.success(staffingStandardService.getActiveStandards());
    }
    
    @Operation(summary = "Query staffing standards by organization")
    @GetMapping("/org/{orgId}")
    public ResponseResult<List<StaffingStandard>> getByOrgId(@PathVariable Long orgId) {
        return ResponseResult.success(staffingStandardService.getByOrgId(orgId));
    }
    
    @Operation(summary = "Query staffing standards by position level")
    @GetMapping("/level/{positionLevel}")
    public ResponseResult<List<StaffingStandard>> getByPositionLevel(@PathVariable String positionLevel) {
        return ResponseResult.success(staffingStandardService.getByPositionLevel(positionLevel));
    }
    
    @Operation(summary = "Get staffing standard by ID")
    @GetMapping("/{id}")
    public ResponseResult<StaffingStandard> getById(@PathVariable Long id) {
        return ResponseResult.success(staffingStandardService.getById(id));
    }
    
    @Operation(summary = "Create new staffing standard")
    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody StaffingStandard standard) {
        return ResponseResult.success(staffingStandardService.createStandard(standard));
    }
    
    @Operation(summary = "Update staffing standard")
    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody StaffingStandard standard) {
        standard.setId(id);
        return ResponseResult.success(staffingStandardService.updateStandard(standard));
    }
    
    @Operation(summary = "Delete staffing standard")
    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(staffingStandardService.removeById(id));
    }
    
    @Operation(summary = "Get standard quota for position")
    @GetMapping("/quota")
    public ResponseResult<Integer> getStandardQuota(
            @RequestParam Long orgId,
            @RequestParam String positionLevel) {
        return ResponseResult.success(staffingStandardService.getStandardQuota(orgId, positionLevel));
    }
    
    @Operation(summary = "Deactivate expired flexible standards")
    @PostMapping("/deactivate-expired")
    public ResponseResult<Integer> deactivateExpired() {
        return ResponseResult.success(staffingStandardService.deactivateExpiredStandards());
    }
}
