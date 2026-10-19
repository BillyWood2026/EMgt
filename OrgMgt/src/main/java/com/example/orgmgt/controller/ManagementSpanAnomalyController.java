package com.example.orgmgt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.orgmgt.entity.ManagementSpanAnomaly;
import com.example.orgmgt.service.ManagementSpanAnomalyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Management Span Anomaly Controller
 * REST API for management quota anomaly tracking
 */
@Tag(name = "Management Span Anomaly Management")
@RestController
@RequestMapping("/api/management-span-anomaly")
public class ManagementSpanAnomalyController {
    
    @Autowired
    private ManagementSpanAnomalyService anomalyService;
    
    @Operation(summary = "Query all anomalies (standard list endpoint)")
    @GetMapping("/list")
    public ResponseResult<List<ManagementSpanAnomaly>> list() {
        return ResponseResult.success(anomalyService.list());
    }

    @Operation(summary = "Update anomaly")
    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody ManagementSpanAnomaly anomaly) {
        anomaly.setId(id);
        return ResponseResult.success(anomalyService.updateById(anomaly));
    }

    @Operation(summary = "Delete anomaly (logical)")
    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(anomalyService.removeById(id));
    }

    @Operation(summary = "Query anomalies with pagination")
    @GetMapping("/page")
    public ResponseResult<Page<ManagementSpanAnomaly>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String warningLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long orgId) {
        Page<ManagementSpanAnomaly> page = new Page<>(pageNum, pageSize);
        return ResponseResult.success(page);
    }
    
    @Operation(summary = "Query all unresolved anomalies")
    @GetMapping("/unresolved")
    public ResponseResult<List<ManagementSpanAnomaly>> getUnresolved() {
        return ResponseResult.success(anomalyService.getUnresolved());
    }
    
    @Operation(summary = "Query anomalies by warning level")
    @GetMapping("/level/{warningLevel}")
    public ResponseResult<List<ManagementSpanAnomaly>> getByWarningLevel(@PathVariable String warningLevel) {
        return ResponseResult.success(anomalyService.getByWarningLevel(warningLevel));
    }
    
    @Operation(summary = "Get anomaly by ID")
    @GetMapping("/{id}")
    public ResponseResult<ManagementSpanAnomaly> getById(@PathVariable Long id) {
        return ResponseResult.success(anomalyService.getById(id));
    }
    
    @Operation(summary = "Create new anomaly record")
    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody ManagementSpanAnomaly anomaly) {
        return ResponseResult.success(anomalyService.createAnomaly(anomaly));
    }
    
    @Operation(summary = "Update anomaly status")
    @PutMapping("/{id}/status")
    public ResponseResult<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String remark) {
        return ResponseResult.success(anomalyService.updateStatus(id, status, remark));
    }
    
    @Operation(summary = "Resolve anomaly")
    @PutMapping("/{id}/resolve")
    public ResponseResult<Boolean> resolve(
            @PathVariable Long id,
            @RequestParam String rectificationDesc) {
        return ResponseResult.success(anomalyService.resolveAnomaly(id, rectificationDesc));
    }
    
    @Operation(summary = "Apply for exemption")
    @PutMapping("/{id}/exemption")
    public ResponseResult<Boolean> applyExemption(
            @PathVariable Long id,
            @RequestParam String exemptionReason,
            @RequestParam String approvalNo) {
        return ResponseResult.success(anomalyService.applyExemption(id, exemptionReason, approvalNo));
    }
    
    @Operation(summary = "Resolve anomaly (JSON body compatible)")
    @PostMapping("/{id}/resolve")
    public ResponseResult<Boolean> resolvePost(
            @PathVariable Long id,
            @RequestParam(required = false) String rectificationDesc,
            @RequestBody(required = false) Map<String, Object> body) {
        String desc = firstNonBlank(rectificationDesc,
                body == null ? null : str(body.get("rectificationDesc")),
                body == null ? null : str(body.get("reason")),
                body == null ? null : str(body.get("remark")));
        if (desc == null) {
            desc = "已整改";
        }
        return ResponseResult.success(anomalyService.resolveAnomaly(id, desc));
    }

    @Operation(summary = "Apply for exemption (JSON body compatible)")
    @PostMapping("/{id}/exempt")
    public ResponseResult<Boolean> exemptPost(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String approvalNo,
            @RequestBody(required = false) Map<String, Object> body) {
        String exemptionReason = firstNonBlank(reason,
                body == null ? null : str(body.get("reason")),
                body == null ? null : str(body.get("exemptionReason")),
                body == null ? null : str(body.get("rectificationDesc")));
        if (exemptionReason == null) {
            exemptionReason = "特殊情况豁免";
        }
        String no = firstNonBlank(approvalNo, body == null ? null : str(body.get("approvalNo")));
        if (no == null) {
            no = "EX-" + System.currentTimeMillis();
        }
        return ResponseResult.success(anomalyService.applyExemption(id, exemptionReason, no));
    }

    @Operation(summary = "Batch resolve anomalies")
    @PostMapping("/batch-resolve")
    @SuppressWarnings("unchecked")
    public ResponseResult<Boolean> batchResolve(@RequestBody(required = false) Map<String, Object> body) {
        if (body == null) {
            return ResponseResult.success(false);
        }
        Object idsObj = body.get("ids");
        String desc = firstNonBlank(str(body.get("rectificationDesc")), str(body.get("reason")));
        if (desc == null) {
            desc = "批量整改";
        }
        boolean all = true;
        if (idsObj instanceof List) {
            for (Object o : (List<Object>) idsObj) {
                if (o != null) {
                    all = anomalyService.resolveAnomaly(Long.valueOf(String.valueOf(o)), desc) && all;
                }
            }
        }
        return ResponseResult.success(all);
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v;
            }
        }
        return null;
    }

    @Operation(summary = "Get anomaly statistics")
    @GetMapping("/stats")
    public ResponseResult<Map<String, Object>> getStats() {
        return ResponseResult.success(anomalyService.getAnomalyStats());
    }
    
    @Operation(summary = "Validate position and create anomaly if violated")
    @PostMapping("/validate")
    public ResponseResult<Boolean> validateAndCreate(
            @RequestParam Long positionId,
            @RequestParam String positionLevel,
            @RequestParam Integer currentSpan) {
        return ResponseResult.success(anomalyService.validateAndCreateAnomaly(positionId, positionLevel, currentSpan));
    }
}
