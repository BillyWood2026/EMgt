package com.example.orgmgt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.orgmgt.entity.ManagementSpanStandard;
import com.example.orgmgt.service.ManagementSpanStandardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Management Span Standard Controller
 * REST API for management span control (Huawei-style)
 */
@Tag(name = "Management Span Standard Management")
@RestController
@RequestMapping("/api/management-span")
public class ManagementSpanController {
    
    @Autowired
    private ManagementSpanStandardService managementSpanStandardService;
    
    @Operation(summary = "Query all management span standards (standard list endpoint)")
    @GetMapping("/list")
    public ResponseResult<List<ManagementSpanStandard>> list() {
        return ResponseResult.success(managementSpanStandardService.list());
    }

    @Operation(summary = "Get management span standard by ID")
    @GetMapping("/{id}")
    public ResponseResult<ManagementSpanStandard> getById(@PathVariable Long id) {
        return ResponseResult.success(managementSpanStandardService.getById(id));
    }

    @Operation(summary = "Create management span standard")
    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody ManagementSpanStandard standard) {
        return ResponseResult.success(managementSpanStandardService.save(standard));
    }

    @Operation(summary = "Delete management span standard (logical)")
    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(managementSpanStandardService.removeById(id));
    }

    @Operation(summary = "Query all management span standards")
    @GetMapping("/all")
    public ResponseResult<List<ManagementSpanStandard>> getAllStandards() {
        return ResponseResult.success(managementSpanStandardService.list());
    }
    
    @Operation(summary = "Query management span standards with pagination")
    @GetMapping("/page")
    public ResponseResult<Page<ManagementSpanStandard>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ManagementSpanStandard> page = new Page<>(pageNum, pageSize);
        return ResponseResult.success(page);
    }
    
    @Operation(summary = "Get management span standard by position level")
    @GetMapping("/level/{positionLevel}")
    public ResponseResult<ManagementSpanStandard> getByPositionLevel(@PathVariable String positionLevel) {
        return ResponseResult.success(managementSpanStandardService.getByPositionLevel(positionLevel));
    }
    
    @Operation(summary = "Initialize default management span standards")
    @PostMapping("/initialize")
    public ResponseResult<Boolean> initializeDefaults() {
        managementSpanStandardService.initializeDefaultStandards();
        return ResponseResult.success(true);
    }
    
    @Operation(summary = "Validate all management span standards")
    @PostMapping("/validate")
    public ResponseResult<Map<String, Object>> validateAll() {
        List<ManagementSpanStandard> standards = managementSpanStandardService.list();
        List<String> violations = new ArrayList<>();
        List<Map<String, Object>> results = new ArrayList<>();
        for (ManagementSpanStandard s : standards) {
            Map<String, Object> r = new HashMap<>();
            r.put("positionLevel", s.getPositionLevel());
            String message = "OK";
            boolean valid = true;
            Integer min = s.getMinSpan();
            Integer std = s.getStandardSpan();
            Integer max = s.getMaxSpan();
            if (min == null || std == null || max == null) {
                valid = false;
                message = "幅度参数不完整";
            } else if (!(min <= std && std <= max)) {
                valid = false;
                message = "幅度参数不满足 min<=std<=max: " + min + "/" + std + "/" + max;
            }
            if (!valid) {
                violations.add(s.getPositionLevel() + ": " + message);
            }
            r.put("valid", valid);
            r.put("message", message);
            results.add(r);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("checked", standards.size());
        data.put("passed", violations.isEmpty());
        data.put("violations", violations);
        data.put("results", results);
        return ResponseResult.success(data);
    }

    @Operation(summary = "Validate span against standard")
    @GetMapping("/validate/span")
    public ResponseResult<String> validateSpan(
            @RequestParam String positionLevel,
            @RequestParam Integer currentSpan) {
        return ResponseResult.success(managementSpanStandardService.validateSpan(positionLevel, currentSpan));
    }
    
    @Operation(summary = "Validate org count against standard")
    @GetMapping("/validate/org-count")
    public ResponseResult<String> validateOrgCount(
            @RequestParam String positionLevel,
            @RequestParam Integer orgCount) {
        return ResponseResult.success(managementSpanStandardService.validateOrgCount(positionLevel, orgCount));
    }
    
    @Operation(summary = "Get management span status")
    @GetMapping("/status")
    public ResponseResult<String> getSpanStatus(
            @RequestParam String positionLevel,
            @RequestParam(required = false) Integer currentSpan,
            @RequestParam(required = false) Integer orgCount) {
        return ResponseResult.success(managementSpanStandardService.getSpanStatus(positionLevel, currentSpan, orgCount));
    }
    
    @Operation(summary = "Check if position creation is allowed")
    @GetMapping("/check-creation")
    public ResponseResult<Boolean> checkPositionCreation(
            @RequestParam String positionLevel,
            @RequestParam Integer currentSpan) {
        return ResponseResult.success(managementSpanStandardService.isPositionCreationAllowed(positionLevel, currentSpan));
    }
    
    @Operation(summary = "Update management span standard")
    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody ManagementSpanStandard standard) {
        standard.setId(id);
        return ResponseResult.success(managementSpanStandardService.updateById(standard));
    }
}
